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
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.TypeSpec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class Coders {

    private Filer filer;
    private Elements elements;
    private Types types;

    public Coders(Filer filer, Elements elements, Types types) {
        this.filer = filer;
        this.elements = elements;
        this.types = types;
    }

    private static Map<String, LayoutBindingCoder> CODERS =
            new LinkedHashMap<String, LayoutBindingCoder>() {
                {
                    put(Constants.ACTIVITY_TYPE_NAME, ActivityLayoutBindingCoder.INSTANCE);
                    put(Constants.FRAGMENT_TYPE_NAME, FragmentLayoutBindingCoder.INSTANCE);
                    put(Constants.SUPPORT_FRAGMENT_TYPE_NAME, FragmentLayoutBindingCoder.INSTANCE);
                }
            };

    public boolean isSubType(TypeElement typeElement, String className) {
        TypeMirror typeMirror = typeElement.asType();
        TypeMirror first = elements.getTypeElement(className).asType();
        return types.isSubtype(typeMirror, first);
    }

    public LayoutBindingCoder find(TypeElement typeElement) {
        if (isSubType(typeElement, Constants.ACTIVITY_TYPE_NAME)) {
            return ActivityLayoutBindingCoder.INSTANCE;
        } else if (isSubType(typeElement, Constants.FRAGMENT_TYPE_NAME)
                || isSubType(typeElement, Constants.SUPPORT_FRAGMENT_TYPE_NAME)) {
            return FragmentLayoutBindingCoder.INSTANCE;
        }
        return null;
    }

    public static void generateFactories(Filer filer, Set<BindingElements> bindingElementsSet) {

        CodeBlock.Builder initializeBlock = CodeBlock.builder();
        for (BindingElements bindingElements : bindingElementsSet) {
            initializeBlock.addStatement(
                    "LayoutBindingFactoryMapper.put($T.class, new $L())",
                    bindingElements.getTarget(),
                    bindingElements.getTarget().getQualifiedName()
                            + Constants.CLASS_NAME_SUFFIX
                            + "$Factory");
        }

        TypeSpec typeSpec =
                TypeSpec.classBuilder(ClassName.get("layoutbinder", "LayoutBindingFactories"))
                        .addModifiers(Modifier.PUBLIC)
                        .addStaticBlock(initializeBlock.build())
                        .build();

        CodeUtils.write(filer, "layoutbinder", typeSpec);
    }

    public void generateLayoutBinding(BindingElements bindingElements) {
        LayoutBindingCoder coder = find(bindingElements.getTarget());
        if (coder != null) {
            coder.code(filer, bindingElements);
        }
    }

    public void code(Filer filer, Set<BindingElements> bindingElementsSet) {
        if (bindingElementsSet == null || bindingElementsSet.isEmpty()) return;
        for (BindingElements bindingElements : bindingElementsSet) {
            generateLayoutBinding(bindingElements);
        }
        generateFactories(filer, bindingElementsSet);
    }
}
