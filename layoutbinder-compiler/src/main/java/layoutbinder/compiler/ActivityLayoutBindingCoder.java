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

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import java.io.IOException;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import layoutbinder.annotations.BindLayout;

import static com.google.auto.common.MoreElements.getPackage;

public enum ActivityLayoutBindingCoder implements LayoutBindingCoder {

    INSTANCE;

    @Override
    public void code(Filer filer, TypeElement targetElement, BindLayout bindLayout) {
        final String packageName = getPackage(targetElement).getQualifiedName().toString();
        final String generatedClassName = ClassName.get(targetElement).simpleName() + Constants.CLASS_NAME_SUFFIX;

        generateLayoutBinding(filer, bindLayout, packageName, targetElement, generatedClassName);
        generateFactory(filer, packageName, generatedClassName);
    }

    private void generateLayoutBinding(Filer filer, BindLayout bindLayout, String packageName,
                                       TypeElement targetElement, String generatedClassName) {
        final ClassName targetClassName = ClassName.get(targetElement);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .build();

        MethodSpec bindMethod = MethodSpec.methodBuilder("bind")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addParameter(targetClassName, "target")
                .addStatement("setTarget($L)", "target")
                .addStatement("setLayoutRes($L)", bindLayout.value())
                .addStatement("target.setContentView($L)", bindLayout.value())
                .returns(void.class)
                .build();

        TypeName abstractParameterized = ParameterizedTypeName.get(
                ClassName.get("layoutbinder.runtime", "LayoutBinding"),
                targetClassName, TypeName.get(Object.class));

        TypeName interfaceParameterized = ParameterizedTypeName.get(
                ClassName.get("layoutbinder.runtime", "ActivityLayoutBinder"),
                targetClassName);

        TypeSpec typeSpec = TypeSpec.classBuilder(generatedClassName)
                .superclass(abstractParameterized)
                .addSuperinterface(interfaceParameterized)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(bindMethod)
                .build();
        CodeUtils.write(filer, packageName, typeSpec);
    }

    private void generateFactory(Filer filer, String packageName, String generatedClassName) {
        // Generate layout binding factory
        MethodSpec createMethod = MethodSpec.methodBuilder("create")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return new $L()", generatedClassName)
                .returns(TypeVariableName.get(generatedClassName))
                .build();

        TypeSpec typeSpec = TypeSpec.classBuilder(generatedClassName + "$Factory")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get("layoutbinder.runtime", "LayoutBindingFactory"))
                .addMethod(createMethod)
                .build();
        CodeUtils.write(filer, packageName, typeSpec);
    }


}
