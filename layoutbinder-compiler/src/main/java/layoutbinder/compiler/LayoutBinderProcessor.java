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

import com.google.auto.service.AutoService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;

import layoutbinder.annotations.BindLayout;

@AutoService(Processor.class)
public class LayoutBinderProcessor extends AbstractProcessor {

    private Filer filer;
    private Types types;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementSet = roundEnvironment.getElementsAnnotatedWith(BindLayout.class);
        Set<TypeElement> typeElements = new HashSet<>();
        for (Element element : elementSet) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = ((TypeElement) element);
                typeElements.add(typeElement);
                BindLayout bindLayout = typeElement.getAnnotation(BindLayout.class);
                LayoutBindingCoder coder = Coders.find(typeElement);
                if (coder != null) {
                    coder.code(filer, typeElement, bindLayout);
                }
            } else if (element instanceof VariableElement) {
                VariableElement variableElement = ((VariableElement) element);
            }
        }

        LayoutBindingFactoriesCoder.code(filer, typeElements);
        LayoutBinderCoder.code(filer);
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new LinkedHashSet<>(
                Arrays.asList(BindLayout.class.getCanonicalName())
        );
    }

    @Override
    public Set<String> getSupportedOptions() {
        return super.getSupportedOptions();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }
}
