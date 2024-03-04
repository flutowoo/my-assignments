package com.fluto.myassignment

import androidx.multidex.MultiDexApplication
import com.fluto.myassignment.R.string.APP_ID
import com.fluto.myassignment.utils.MyPreference
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.consts.ThreadReplySelectType
import com.sendbird.uikit.consts.TypingIndicatorType
import com.sendbird.uikit.interfaces.UserInfo
import com.sendbird.uikit.model.configurations.UIKitConfig

class Application: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        MyPreference.init(this)
        initUIKIt()
        initConfig()
    }

    private fun initUIKIt() {
        SendbirdUIKit.init(object : SendbirdUIKitAdapter {
            override fun getAppId(): String {
                return resources.getString(APP_ID) // Specify your Sendbird application ID.
            }

            override fun getAccessToken(): String {
                return ""
            }

            override fun getUserInfo(): UserInfo {
                return object : UserInfo {
                    override fun getUserId(): String {
                        return MyPreference.userId // Specify your user ID.
                    }

                    override fun getNickname(): String {
                        return MyPreference.nickname // Specify your user nickname.
                    }

                    override fun getProfileUrl(): String {
                        return MyPreference.profileUrl
                    }
                }
            }

            override fun getInitResultHandler(): InitResultHandler {
                return object : InitResultHandler {
                    override fun onMigrationStarted() {
                        ToastUtil.show(applicationContext, "Migration started")
                    }

                    override fun onInitFailed(e: SendbirdException) {
                        ToastUtil.show(applicationContext, "Initialization failed")
                    }

                    override fun onInitSucceed() {
                        ToastUtil.show(applicationContext, "Initialization succeeded")
                    }
                }
            }
        }, this)
    }

    private fun initConfig() {
        with(UIKitConfig) {
            common.enableUsingDefaultUserProfile = false
            groupChannelListConfig.apply {
                enableTypingIndicator = true
                enableMessageReceiptStatus = true
            }
            groupChannelConfig.apply {
                enableMention = true
                replyType = ReplyType.THREAD
                threadReplySelectType = ThreadReplySelectType.THREAD
                enableFeedback = true
                typingIndicatorTypes = setOf(TypingIndicatorType.BUBBLE)
            }
        }
    }
}