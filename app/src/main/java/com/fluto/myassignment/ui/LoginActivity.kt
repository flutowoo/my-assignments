package com.fluto.myassignment.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fluto.myassignment.R
import com.fluto.myassignment.databinding.ActivityLoginBinding
import com.fluto.myassignment.ui.channellist.GroupChannelListActivity
import com.fluto.myassignment.utils.MyPreference
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.SendbirdChat

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
                val userId = binding.tiUserId.text.toString()
                val nickname = binding.tiNickname.text.toString()

                if (userId.isNotEmpty() && nickname.isNotEmpty()) {
                    MyPreference.userId = userId
                    MyPreference.nickname = nickname

                    SendbirdChat.connect(userId) { user, e ->
                        if (e != null) {
                            ToastUtil.show(this@LoginActivity, "$e")
                            return@connect
                        }
                        if (user != null) {
                            Intent(this@LoginActivity, GroupChannelListActivity::class.java).also {
                                startActivity(it)
                                finish()
                            }
                            startActivity(intent)
                            finish()
                        }
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
