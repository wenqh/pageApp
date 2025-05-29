package com.wqh.app // 替换为您的包名

import android.content.Context
import android.content.SharedPreferences

object BlacklistPersistence {
    private const val PREFS_NAME = "blacklist_prefs"
    private const val KEY_BLACKLISTED_APPS = "blacklisted_apps"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun addAppToBlacklist(context: Context, packageName: String) {
        val prefs = getPreferences(context)
        val blacklist = getBlacklistedApps(context).toMutableSet()
        blacklist.add(packageName)
        prefs.edit().putStringSet(KEY_BLACKLISTED_APPS, blacklist).apply()
    }

    fun removeAppFromBlacklist(context: Context, packageName: String) {
        val prefs = getPreferences(context)
        val blacklist = getBlacklistedApps(context).toMutableSet()
        blacklist.remove(packageName)
        prefs.edit().putStringSet(KEY_BLACKLISTED_APPS, blacklist).apply()
    }

    fun isAppBlacklisted(context: Context, packageName: String): Boolean {
        return getBlacklistedApps(context).contains(packageName)
    }

    fun getBlacklistedApps(context: Context): Set<String> {
        val prefs = getPreferences(context)
        return prefs.getStringSet(KEY_BLACKLISTED_APPS, emptySet()) ?: emptySet()
    }

    fun setBlacklistedApps(context: Context, blacklistedApps: Set<String>) {
        val prefs = getPreferences(context)
        prefs.edit().putStringSet(KEY_BLACKLISTED_APPS, blacklistedApps).apply()
    }
}