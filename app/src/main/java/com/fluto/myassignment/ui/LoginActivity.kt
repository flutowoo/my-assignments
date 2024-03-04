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
                val userId = tiUserId.text.toString()
                val nickname = tiNickname.text.toString()

                if (userId.isNotEmpty() && nickname.isNotEmpty()) {
                    MyPreference.userId = userId
                    MyPreference.nickname = nickname
                    MyPreference.profileUrl =
                        "https://unsplash.com/ko/%EC%82%AC%EC%A7%84/%EC%B2%AD%EB%A1%9D%EC%83%89-%ED%8F%AD%EC%8A%A4-%EB%B0%94%EA%B2%90-%EB%B9%84%ED%8B%80%EC%9D%80-%EA%B0%88%EC%83%89-%EC%A7%91-%EA%B7%BC%EC%B2%98%EC%97%90-%EC%A3%BC%EC%B0%A8%EB%90%98%EC%97%88%EC%8A%B5%EB%8B%88%EB%8B%A4-xBRQfR2bqNI?utm_content=creditShareLink&utm_medium=referral&utm_source=unsplash"

                    Intent(this@LoginActivity, MainActivity::class.java).also {
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
    }
}
