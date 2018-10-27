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
import com.sun.source.util.Trees;

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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import layoutbinder.annotations.BindLayout;

@AutoService(Processor.class)
public class LayoutBinderProcessor extends AbstractProcessor {

    private Filer filer;
    private Types types;
    private Elements elements;
    private Messager messager;
    private Trees trees;

    private Coders coders;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        filer = processingEnvironment.getFiler();
        types = processingEnvironment.getTypeUtils();
        messager = processingEnvironment.getMessager();
        elements = processingEnvironment.getElementUtils();
        trees = Trees.instance(processingEnvironment);

        coders = new Coders(filer, elements, types);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elementSet =
                roundEnvironment.getElementsAnnotatedWith(BindLayout.class);
        Set<BindingElements> bindingElementsSet = new HashSet<>();
        for (Element element : elementSet) {
            if (element instanceof TypeElement) {
                TypeElement typeElement = ((TypeElement) element);
                BindingElements bindingElements = new BindingElements();
                bindingElements.setTarget(typeElement);
                bindingElementsSet.add(bindingElements);
            } else if (element instanceof VariableElement) {
                VariableElement variableElement = ((VariableElement) element);
                BindingElements bindingElements = new BindingElements();
                // If the enclosing element is wrong, an exception will occur.
                bindingElements.setTarget(((TypeElement) variableElement.getEnclosingElement()));
                bindingElements.setViewDataBinding(variableElement);
                bindingElementsSet.add(bindingElements);
            }
        }
        coders.code(filer, bindingElementsSet);
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new LinkedHashSet<>(Arrays.asList(BindLayout.class.getCanonicalName()));
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
