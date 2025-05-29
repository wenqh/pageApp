package com.wqh.app // 替换为您的包名

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    var isBlacklisted: Boolean = false
)