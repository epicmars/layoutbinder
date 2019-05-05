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

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import layoutbinder.runtime.ActivityLayoutBinder;
import layoutbinder.runtime.FragmentLayoutBinder;
import layoutbinder.runtime.LayoutBinding;
import layoutbinder.runtime.LayoutBindingFactory;

public class LayoutBinder {

    public static LayoutBinding bind(Activity activity) {
        LayoutBinding binding = createLayoutBinding(activity);
        if (binding == null) {
            return null;
        }
        ((ActivityLayoutBinder) binding).bind(activity);
        return binding;
    }

    public static LayoutBinding bind(
            Fragment fragment, LayoutInflater inflater, ViewGroup parent, boolean attachToParent) {
        LayoutBinding binding = createLayoutBinding(fragment);
        if (binding == null) {
            return null;
        }
        ((FragmentLayoutBinder) binding).bind(fragment, inflater, parent, attachToParent);
        return binding;
    }

    public static LayoutBinding bind(Fragment fragment, LayoutInflater inflater, ViewGroup parent) {
        LayoutBinding binding = createLayoutBinding(fragment);
        if (binding == null) {
            return null;
        }
        ((FragmentLayoutBinder) binding).bind(fragment, inflater, parent);
        return binding;
    }

    private static LayoutBinding createLayoutBinding(Object target) {
        LayoutBindingFactory factory = LayoutBindingFactoryMapper.get(target);
        if (null == factory) {
            return null;
        }
        LayoutBinding binding = factory.create();
        return binding;
    }
}
