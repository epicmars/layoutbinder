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
package com.androidpi.layoutbinder.sample;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import layoutbinder.LayoutBinder;

import layoutbinder.annotations.BindLayout;
import layoutbinder.runtime.LayoutBinding;

@BindLayout(R.layout.activity_example)
public class ExampleActivity extends AppCompatActivity {

    LayoutBinding layoutBinding;

    public static void start(Context context) {
        Intent starter = new Intent(context, ExampleActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        layoutBinding = LayoutBinder.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        layoutBinding.unbind();
    }
}
