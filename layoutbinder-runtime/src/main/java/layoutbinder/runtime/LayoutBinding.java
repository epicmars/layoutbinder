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
package layoutbinder.runtime;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.databinding.ViewDataBinding;

public abstract class LayoutBinding<T, VDB> implements LayoutUnbinder {

    protected T target;
    protected VDB binding;
    protected View view;
    protected @LayoutRes int layoutRes;

    public T getTarget() {
        return target;
    }

    public void setTarget(T target) {
        this.target = target;
    }

    public int getLayoutRes() {
        return layoutRes;
    }

    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
    }

    public void setView(View view) {
        this.view = view;
    }

    public void setBinding(VDB binding) {
        this.binding = binding;
    }

    public View getView() {
        return view;
    }

    public VDB getBinding() {
        return binding;
    }

    @Override
    public void unbind() {
        this.target = null;
        this.view = null;
        if (this.binding instanceof ViewDataBinding) {
            ((ViewDataBinding) this.binding).unbind();
            this.binding = null;
        }
    }
}
