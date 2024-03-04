package com.fluto.myassignment

import androidx.multidex.MultiDexApplication
import com.fluto.myassignment.R.string.APP_ID
import com.fluto.myassignment.utils.MyPreference
import com.fluto.myassignment.utils.ToastUtil
import com.sendbird.android.exception.SendbirdException
import com.sendbird.android.handler.InitResultHandler
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.adapter.SendbirdUIKitAdapter
import com.sendbird.uikit.interfaces.UserInfo

class Application: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        MyPreference.init(this)
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
                        // DB migration has started.
                        ToastUtil.show(applicationContext, "Migration started")
                    }

                    override fun onInitFailed(e: SendbirdException) {
                        // If DB migration fails, this method is called.
                        ToastUtil.show(applicationContext, "Initialization failed")
                    }

                    override fun onInitSucceed() {
                        // If DB migration is successful, this method is called and you can proceed to the next step.
                        // In the sample app, the `LiveData` class notifies you on the initialization progress
                        // And observes the `MutableLiveData<InitState> initState` value in `SplashActivity()`.
                        // If successful, the `LoginActivity` screen
                        // Or the `HomeActivity` screen will show.
                        ToastUtil.show(applicationContext, "Initialization succeeded")
                    }
                }
            }
        }, this)
    }
}