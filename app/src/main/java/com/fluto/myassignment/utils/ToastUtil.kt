package com.fluto.myassignment.utils

import android.content.Context
import android.text.TextUtils
import android.widget.Toast

object ToastUtil {
    private var toast: Toast? = null

    fun show(context: Context?, message: String?) {
        context ?: return

        if(TextUtils.isEmpty(message)) return
        if(toast != null) {
            toast?.setText(message)
        } else {
            toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        }

        toast?.show()
    }
}