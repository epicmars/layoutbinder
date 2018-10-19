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


import java.util.LinkedHashMap;
import java.util.Map;
import android.app.Activity;
import android.support.v4.app.Fragment;

import javax.lang.model.element.TypeElement;

public class Coders {

    private static Map<Class<?>, LayoutBindingCoder> coders = new LinkedHashMap<Class<?>, LayoutBindingCoder>() {
        {
            put(Activity.class, ActivityLayoutBindingCoder.INSTANCE);
            put(Fragment.class, FragmentLayoutBindingCoder.INSTANCE);
            put(android.app.Fragment.class, FragmentLayoutBindingCoder.INSTANCE);
        }
    };

    public static LayoutBindingCoder find(TypeElement typeElement) {
        String superClassName = typeElement.getSuperclass().toString();
        try {
            Class<?> clazz = Class.forName(superClassName);
            for (Class<?> c : coders.keySet()) {
                if (c.isAssignableFrom(clazz)) {
                    return coders.get(c);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return ActivityLayoutBindingCoder.INSTANCE;
    }
}
