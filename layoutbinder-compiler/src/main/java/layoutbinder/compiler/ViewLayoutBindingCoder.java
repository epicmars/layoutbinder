/*
 * Copyright 2019 yinpinjiu@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package layoutbinder.compiler;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import layoutbinder.annotations.BindLayout;

import static com.google.auto.common.MoreElements.getPackage;
import static layoutbinder.compiler.Constants.ANDROID_DATABINDING_PACKAGE;
import static layoutbinder.compiler.Constants.ANDROID_VIEW_PACKAGE;

public enum ViewLayoutBindingCoder implements LayoutBindingCoder {
    INSTANCE;

    @Override
    public void code(Filer filer, BindingElements bindingElements) {
        final TypeElement targetElement = bindingElements.getTarget();
        final String packageName = getPackage(targetElement).getQualifiedName().toString();
        final String generatedClassName =
                ClassName.get(targetElement).simpleName() + Constants.CLASS_NAME_SUFFIX;

        generateLayoutBinding(filer, packageName, bindingElements, generatedClassName);
        generateFactory(filer, packageName, generatedClassName);
    }

    /**
     * Generate a layout binding class with provided annotation.
     * @param filer
     * @param packageName
     * @param bindingElements
     * @param generatedClassName
     */
    private void generateLayoutBinding(
            Filer filer,
            String packageName,
            BindingElements bindingElements,
            String generatedClassName) {
        final ClassName targetClassName = ClassName.get(bindingElements.getTarget());
        BindLayout bindLayout = bindingElements.getTarget().getAnnotation(BindLayout.class);
        if (bindLayout == null) {
            bindLayout = bindingElements.getViewDataBinding().getAnnotation(BindLayout.class);
        }

        MethodSpec constructor =
                MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build();

        List<MethodSpec> methodSpecList =
                generateLayoutBindingMethods(targetClassName, bindLayout, bindingElements);

        TypeName dataBindingType =
                bindingElements.getViewDataBinding() == null
                        ? TypeName.get(Object.class)
                        : TypeName.get(bindingElements.getViewDataBinding().asType());

        TypeName abstractParameterized =
                ParameterizedTypeName.get(
                        ClassName.get("layoutbinder.runtime", "LayoutBinding"),
                        targetClassName,
                        dataBindingType);

        TypeName interfaceParameterized =
                ParameterizedTypeName.get(
                        ClassName.get("layoutbinder.runtime", "ViewLayoutBinder"),
                        targetClassName);

        TypeSpec typeSpec =
                TypeSpec.classBuilder(generatedClassName)
                        .superclass(abstractParameterized)
                        .addSuperinterface(interfaceParameterized)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addMethod(constructor)
                        .addMethods(methodSpecList)
                        .build();
        CodeUtils.write(filer, packageName, typeSpec);
    }

    /**
     * Generate layout binding method that do the work。
     * @param targetClassName
     * @param bindLayout
     * @param bindingElements
     * @return
     */
    private List<MethodSpec> generateLayoutBindingMethods(
            TypeName targetClassName, BindLayout bindLayout, BindingElements bindingElements) {
        if (bindingElements.getViewDataBinding() == null) {
            return generateDefaultMethods(targetClassName, bindLayout);
        }
        return generateDataBindingMethods(targetClassName, bindLayout, bindingElements);
    }

    /**
     * If a data binding field is not present when binding a layout to a view,
     * the default binding method is generated.
     * @param targetClassName
     * @param bindLayout
     * @return
     */
    private List<MethodSpec> generateDefaultMethods(
            TypeName targetClassName, BindLayout bindLayout) {
        MethodSpec bindMethod =
                MethodSpec.methodBuilder("bind")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(targetClassName, "target")
                        .addStatement("this.target = $L", "target")
                        .addStatement("this.layoutRes = $L", bindLayout.value())
                        .addStatement("target.inflate(target.getContext(), $L, target)", bindLayout.value())
                        .returns(void.class)
                        .build();
        return Collections.singletonList(bindMethod);
    }

    /**
     * If data binding a data binding field is present when binding a layout to a view,
     * then generate a method that return a data binding.
     * @param targetClassName
     * @param bindLayout
     * @param bindingElements
     * @return
     */
    private List<MethodSpec> generateDataBindingMethods(
            TypeName targetClassName, BindLayout bindLayout, BindingElements bindingElements) {
        String viewDataBindingFieldName =
                bindingElements.getViewDataBinding().getSimpleName().toString();
        TypeVariableName bindingField =
                TypeVariableName.get(
                        viewDataBindingFieldName,
                        TypeName.get(bindingElements.getViewDataBinding().asType()));
        CodeBlock.Builder bindingAssignmentBuilder = CodeBlock.builder();

        Set<Modifier> modifiers = bindingElements.getViewDataBinding().getModifiers();

        CodeBlock binding = CodeBlock.of("this.binding = $T.inflate($T.from(target.getContext()), $L, target, $L)",
                ClassName.get(ANDROID_DATABINDING_PACKAGE, "DataBindingUtil"),
                ClassName.get(ANDROID_VIEW_PACKAGE, "LayoutInflater"),
                bindLayout.value(),
                bindLayout.attachToParent());
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.PROTECTED)) {
            String bindingFieldSetter =
                    "set"
                            + bindingField.name.substring(0, 1).toUpperCase()
                            + bindingField.name.substring(1);

            if (CodeUtils.hasAccessibleMethod(bindingElements.getTarget(), bindingFieldSetter)) {
                bindingAssignmentBuilder
                        .addStatement(binding)
                        .addStatement("target.$L(this.binding)", bindingFieldSetter)
                        .build();
            } else {
                throw new RuntimeException(
                        String.format(
                                "%s's field \"%s\" should has a public or package-private modifier,"
                                        + "otherwise a corresponding setter method should be presented "
                                        + "with a public or package-private modifier.",
                                targetClassName.toString(), bindingField.name));
            }
        } else {
            bindingAssignmentBuilder
                    .addStatement(binding)
                    .addStatement("target.$T = this.binding", bindingField)
                    .build();
        }

        MethodSpec bindMethod1 =
                MethodSpec.methodBuilder("bind")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addParameter(targetClassName, "target")
                        .addStatement("this.target = $L", "target")
                        .addStatement("this.layoutRes = $L", bindLayout.value())
                        .addCode(bindingAssignmentBuilder.build())
                        .addStatement("this.view = this.binding.getRoot()")
                        .returns(void.class)
                        .build();
        List<MethodSpec> methodSpecList = new ArrayList<>();
        methodSpecList.add(bindMethod1);
        return methodSpecList;
    }

    private void generateFactory(Filer filer, String packageName, String generatedClassName) {
        // Generate layout binding factory
        MethodSpec createMethod =
                MethodSpec.methodBuilder("create")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement("return new $L()", generatedClassName)
                        .returns(TypeVariableName.get(generatedClassName))
                        .build();

        TypeSpec typeSpec =
                TypeSpec.classBuilder(generatedClassName + "$Factory")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addSuperinterface(
                                ClassName.get("layoutbinder.runtime", "LayoutBindingFactory"))
                        .addMethod(createMethod)
                        .build();
        CodeUtils.write(filer, packageName, typeSpec);
    }
}
