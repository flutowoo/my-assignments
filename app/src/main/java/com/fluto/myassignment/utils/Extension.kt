package com.fluto.myassignment.utils

import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun Long.toChatTime(): String {
    return if (this.equalDate(currentMillisecond())) {
        this.toTime()
    } else {
        this.toDate()
    }
}

fun Long.equalDate(compareMillisecond: Long): Boolean {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val date2 =
        Instant.ofEpochMilli(compareMillisecond).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("MMdd")

    return date.format(formatter) == date2.format(formatter)
}

fun Long.equalTime(compareMillisecond: Long): Boolean {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val date2 =
        Instant.ofEpochMilli(compareMillisecond).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("HHmm")

    return date.format(formatter) == date2.format(formatter)
}

fun Long.toTime(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("hh:mm a")
    return date.format(formatter)
}

fun Long.toDate(): String {
    val date = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDateTime()
    val formatter = DateTimeFormatter.ofPattern("dd MMMMM yyyy")
    return date.format(formatter)
}


fun currentMillisecond(): Long = System.currentTimeMillis()

fun <T> MutableLiveData<T>.changeValue(value: T){
    if (Looper.getMainLooper() == Looper.myLooper()){
        setValue(value)
    } else{
        postValue(value)
    }
}