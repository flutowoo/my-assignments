package com.fluto.myassignment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.multidex.MultiDexApplication
import com.fluto.myassignment.R.string.APP_ID
import com.fluto.myassignment.utils.MyPreference
import com.fluto.myassignment.utils.ToastUtil
import com.fluto.myassignment.utils.changeValue
import com.sendbird.android.LogLevel
import com.sendbird.android.SendbirdChat
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.android.params.InitParams
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.consts.ReplyType
import com.sendbird.uikit.consts.ThreadReplySelectType
import com.sendbird.uikit.consts.TypingIndicatorType
import com.sendbird.uikit.interfaces.UserInfo
import com.sendbird.uikit.model.configurations.UIKitConfig

class Application : MultiDexApplication() {

    protected val initMutableLiveData: MutableLiveData<Boolean> =
        MutableLiveData<Boolean>().apply { changeValue(false) }
    val initLiveData: LiveData<Boolean>
        get() = initMutableLiveData

    override fun onCreate() {
        super.onCreate()

        MyPreference.init(this)
        sendbirdChatInit()
    }

    open fun sendbirdChatInit() {
        val initParams = InitParams(resources.getString(R.string.APP_ID), applicationContext, true)
        initParams.logLevel = LogLevel.ERROR
        SendbirdChat.init(
            initParams,
            object : InitResultHandler {
                override fun onInitFailed(e: SendbirdException) {
                    initMutableLiveData.changeValue(true)
                }

                override fun onMigrationStarted() {
                }

                override fun onInitSucceed() {
                    initMutableLiveData.changeValue(true)
                }
            })
    }

}