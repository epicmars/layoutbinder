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
package com.androidpi.layoutbinder.sample

import android.app.Activity
import android.os.Bundle
import com.androidpi.layoutbinder.sample.databinding.ActivityMainBinding
import layoutbinder.LayoutBinderActivity
import layoutbinder.annotations.BindLayout

class MainActivity : LayoutBinderActivity() {

    @BindLayout(R.layout.activity_main)
    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.handler = ViewHandler()
    }

    inner class ViewHandler {

        fun navToNormalExample() {
            NormalExampleActivity.start(getContext())
        }

        fun navToDataBindingExample() {
            DataBindingExampleActivity.start(getContext())
        }

        fun navToKotlinDataBindingExample() {
            KotlinDataBindingActivity.start(getContext())
        }

        fun navToCustomViewExample() {
            CustomViewActivity.start(getContext())
        }

        fun getContext() : Activity {
            return this@MainActivity
        }
    }
}
