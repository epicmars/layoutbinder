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

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

public class CodeUtils {

    public static void write(Filer filer, String packageName, TypeSpec typeSpec) {
        JavaFile javaFile = JavaFile.builder(packageName, typeSpec).indent("    ").build();
        try {
            javaFile.writeTo(filer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean hasAccessibleMethod(TypeElement typeElement, String methodName) {
        List<? extends Element> elements = typeElement.getEnclosedElements();
        for (Element e : elements) {
            if (e.getKind() == ElementKind.METHOD
                    && e.getSimpleName().contentEquals(methodName)
                    && (e.getModifiers().contains(Modifier.DEFAULT)
                            || e.getModifiers().contains(Modifier.PUBLIC))) {
                return true;
            }
        }
        return false;
    }
}
