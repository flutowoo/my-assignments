package com.fluto.myassignment.ui.channellist

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fluto.myassignment.databinding.ActivityGroupChannelListBinding

class GroupChannelListActivity: AppCompatActivity() {
    private val binding by lazy {
        ActivityGroupChannelListBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.toolbar.title = "Group Channel List"
        setSupportActionBar(binding.toolbar)
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainerView.id, GroupChannelListFragment())
            .commit()
    }


}