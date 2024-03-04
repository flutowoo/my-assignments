package com.fluto.myassignment.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fluto.myassignment.R
import com.fluto.myassignment.databinding.ActivityMainCustomBinding
import com.sendbird.uikit.providers.FragmentProviders

class MainCustomActivity : AppCompatActivity() {
    private val binding: ActivityMainCustomBinding by lazy {
        ActivityMainCustomBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.fragment_container_view,
                    FragmentProviders.channelList.provide(Bundle())
                )
                .commitNow()
        }
    }
}