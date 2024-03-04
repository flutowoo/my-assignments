package com.fluto.myassignment.ui

import android.os.Bundle
import com.fluto.myassignment.utils.MyPreference
import com.sendbird.android.params.UserUpdateParams
import com.sendbird.uikit.SendbirdUIKit
import com.sendbird.uikit.activities.ChannelListActivity

class MainActivity: ChannelListActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initData()


    }

    private fun initData() {
        val params = UserUpdateParams()
        params.nickname = MyPreference.nickname
        params.profileImageUrl = MyPreference.profileUrl
        SendbirdUIKit.updateUserInfo(params) { e ->
            e?.printStackTrace()
        }
    }
}