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
package layoutbinder;

import java.util.HashMap;

import layoutbinder.runtime.LayoutBindingFactory;

public class LayoutBindingFactoryMapper {

    public static HashMap<Class, LayoutBindingFactory> MAPPER = new HashMap<>();

    public static void checkMapper() {
        if (MAPPER.size() > 0) {
            return;
        }
        try {
            Class.forName("layoutbinder.LayoutBindingFactories");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void put(Class<?> targetClass, LayoutBindingFactory factory) {
        checkMapper();
        MAPPER.put(targetClass, factory);
    }

    public static LayoutBindingFactory get(Object target) {
        checkMapper();
        return MAPPER.get(target.getClass());
    }
}
