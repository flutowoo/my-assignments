package com.fluto.myassignment.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fluto.myassignment.R
import com.fluto.myassignment.databinding.ActivityLoginBinding
import com.fluto.myassignment.utils.MyPreference
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.push.SendbirdPushHelper

class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
    }

    private fun initView() {
        binding.apply {
            btnLogin.setOnClickListener {
                goMainActivity(MainActivity::class.java)
            }
            btnLoginCustom.setOnClickListener {
                goMainActivity(MainCustomActivity::class.java)
            }
        }
    }

    private fun goMainActivity(clazz: Class<*>) {
        val userId = binding.tiUserId.text.toString()
        val nickname = binding.tiNickname.text.toString()

        if (userId.isNotEmpty() && nickname.isNotEmpty()) {
            MyPreference.userId = userId
            MyPreference.nickname = nickname

            Intent(this@LoginActivity, clazz).also {
                startActivity(it)
                finish()
            }

        } else {
            ToastUtil.show(
                this@LoginActivity,
                resources.getString(R.string.loing_empty_value)
            )
        }
    }
}
