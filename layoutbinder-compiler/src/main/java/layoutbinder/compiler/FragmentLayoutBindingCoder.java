/*
 * Copyright 2018 yinpinjiu@gmail.com
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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;

import layoutbinder.annotations.BindLayout;

import static com.google.auto.common.MoreElements.getPackage;

public enum FragmentLayoutBindingCoder implements LayoutBindingCoder {
    INSTANCE;

    @Override
    public void code(Filer filer, BindingElements bindingElements) {
        final String packageName =
                getPackage(bindingElements.getTarget()).getQualifiedName().toString();
        final String className =
                ClassName.get(bindingElements.getTarget()).simpleName()
                        + Constants.CLASS_NAME_SUFFIX;

        generateLayoutBinding(filer, bindingElements, packageName, className);
        generateFactory(filer, packageName, className);
    }

    private void generateLayoutBinding(
            Filer filer,
            BindingElements bindingElements,
            String packageName,
            String generatedClassName) {
        final ClassName targetClassName = ClassName.get(bindingElements.getTarget());
        BindLayout bindLayout = bindingElements.getTarget().getAnnotation(BindLayout.class);
        if (bindLayout == null) {
            bindLayout = bindingElements.getViewDataBinding().getAnnotation(BindLayout.class);
        }

        MethodSpec constructor =
                MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC).build();

        TypeName dataBindingType = bindingElements.getViewDataBinding() == null ?
                TypeName.get(Object.class) : TypeName.get(bindingElements.getViewDataBinding().asType());

        TypeName abstractParameterized =
                ParameterizedTypeName.get(
                        ClassName.get("layoutbinder.runtime", "LayoutBinding"),
                        targetClassName,
                        dataBindingType);

        TypeName interfaceParameterized =
                ParameterizedTypeName.get(
                        ClassName.get("layoutbinder.runtime", "FragmentLayoutBinder"),
                        targetClassName);

        List<MethodSpec> bindingMethods =
                generateLayoutBindingMethods(targetClassName, bindLayout, bindingElements);

        TypeSpec typeSpec =
                TypeSpec.classBuilder(generatedClassName)
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .superclass(abstractParameterized)
                        .addSuperinterface(interfaceParameterized)
                        .addMethod(constructor)
                        .addMethods(bindingMethods)
                        .build();

        CodeUtils.write(filer, packageName, typeSpec);
    }

    private List<MethodSpec> generateLayoutBindingMethods(
            TypeName targetClassName, BindLayout bindLayout, BindingElements bindingElements) {
        if (bindingElements.getViewDataBinding() == null) {
            return generateDefaultMethods(targetClassName, bindLayout);
        }
        return generateDataBindingMethods(targetClassName, bindLayout, bindingElements);
    }

    private List<MethodSpec> generateDefaultMethods(
            TypeName targetClassName, BindLayout bindLayout) {
        MethodSpec bindMethod =
                MethodSpec.methodBuilder("bind")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(Override.class)
                        .addParameter(targetClassName, "target")
                        .addParameter(LayoutInflater.class, "inflater")
                        .addParameter(ViewGroup.class, "parent")
                        .addParameter(boolean.class, "attachToParent")
                        .addStatement("setTarget($L)", "target")
                        .addStatement("setLayoutRes($L)", bindLayout.value())
                        .addStatement(
                                "final View view = inflater.inflate($L, $L, $L)",
                                bindLayout.value(),
                                "parent",
                                "attachToParent")
                        .addStatement("setView($L)", "view")
                        .addStatement("return view")
                        .returns(View.class)
                        .build();
        return Collections.singletonList(bindMethod);
    }

    private List<MethodSpec> generateDataBindingMethods(
            TypeName targetClassName, BindLayout bindLayout, BindingElements bindingElements) {

        String viewDataBindingFieldName =
                bindingElements.getViewDataBinding().getSimpleName().toString();

        TypeVariableName bindingField =
                TypeVariableName.get(
                        viewDataBindingFieldName,
                        TypeName.get(bindingElements.getViewDataBinding().asType()));

        MethodSpec bindMethod =
                MethodSpec.methodBuilder("bind")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .addAnnotation(Override.class)
                        .addParameter(targetClassName, "target")
                        .addParameter(LayoutInflater.class, "inflater")
                        .addParameter(ViewGroup.class, "parent")
                        .addParameter(boolean.class, "attachToParent")
                        .addStatement("setTarget($L)", "target")
                        .addStatement("setLayoutRes($L)", bindLayout.value())
                        .addStatement(
                                "target.$T = $T.inflate(inflater, $L, $L, $L)",
                                bindingField,
                                ClassName.get("android.databinding", "DataBindingUtil"),
                                bindLayout.value(),
                                "parent",
                                "attachToParent")
                        .addStatement("setBinding(target.$T)", bindingField)
                        .addStatement("setView($T.getRoot())", bindingField)
                        .addStatement("return getView()")
                        .returns(View.class)
                        .build();
        List<MethodSpec> methodSpecList = new ArrayList<>();
        methodSpecList.add(bindMethod);
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
