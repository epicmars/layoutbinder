package com.androidpi.layoutbinder.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.androidpi.layoutbinder.sample.databinding.ActivityKotlinDataBindingExampleBinding
import layoutbinder.LayoutBinder
import layoutbinder.annotations.BindLayout

class KotlinDataBindingActivity : AppCompatActivity() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, KotlinDataBindingActivity::class.java)
            context.startActivity(intent)
        }
    }

    @BindLayout(R.layout.activity_kotlin_data_binding_example)
    var binding: ActivityKotlinDataBindingExampleBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LayoutBinder.bind(this)
    }
}
