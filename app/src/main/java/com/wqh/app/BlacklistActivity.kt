package com.wqh.app // 替换为您的包名

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BlacklistActivity : Activity() { // 或继承自 android.app.Activity

    private lateinit var listViewApps: ListView
    private lateinit var buttonSaveBlacklist: Button
    private lateinit var adapter: BlacklistAdapter
    private val appInfoList = mutableListOf<AppInfo>()
    private val tempBlacklistSelection = mutableSetOf<String>() // 用于暂存用户的选择

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blacklist)

        listViewApps = findViewById(R.id.listview_apps)
        buttonSaveBlacklist = findViewById(R.id.button_save_blacklist)

        adapter = BlacklistAdapter(this, appInfoList)
        listViewApps.adapter = adapter

        // 初始化时加载已保存的黑名单到临时选择中
        tempBlacklistSelection.addAll(BlacklistPersistence.getBlacklistedApps(this))
        loadInstalledApps()

        listViewApps.setOnItemClickListener { _, _, position, _ ->
            val clickedApp = appInfoList[position]
            clickedApp.isBlacklisted = !clickedApp.isBlacklisted // 更新UI状态

            // 更新临时选择
            if (clickedApp.isBlacklisted) {
                tempBlacklistSelection.add(clickedApp.packageName)
            } else {
                tempBlacklistSelection.remove(clickedApp.packageName)
            }
            adapter.notifyDataSetChanged() // 刷新列表项的 CheckBox 显示
        }

        buttonSaveBlacklist.setOnClickListener {
            saveBlacklist()
            Toast.makeText(this, "黑名单已保存", Toast.LENGTH_SHORT).show()
            finish() // 保存后关闭此 Activity
        }
    }

    private fun loadInstalledApps() {
        GlobalScope.launch(Dispatchers.IO) { // 使用协程在后台加载应用列表
            val pm = packageManager
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)
            val loadedApps = mutableListOf<AppInfo>()

            for (app in packages) {
                // 通常我们不希望系统应用和自身应用出现在黑名单选项中，除非特别需要
                // 这里我们只加载非系统应用 (或更新过的系统应用)
                if ((app.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || (app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
                    if (app.packageName == packageName) continue // 跳过自身应用

                    try {
                        val appName = pm.getApplicationLabel(app).toString()
                        val packageName = app.packageName
                        val appIcon = pm.getApplicationIcon(app)
                        // 从临时选择中恢复选中状态
                        val isBlacklisted = tempBlacklistSelection.contains(packageName)
                        loadedApps.add(AppInfo(packageName, appName, appIcon, isBlacklisted))
                    } catch (e: PackageManager.NameNotFoundException) {
                        Log.e("BlacklistActivity", "加载应用信息时出错: ${app.packageName}", e)
                    } catch (e: OutOfMemoryError) {
                        Log.e("BlacklistActivity", "加载应用图标时内存不足: ${app.packageName}", e)
                        // 可以考虑使用占位符图标
                    }
                }
            }
            // 按应用名称排序
            loadedApps.sortBy { it.appName.lowercase() }

            withContext(Dispatchers.Main) { // 切换回主线程更新UI
                appInfoList.clear()
                appInfoList.addAll(loadedApps)
                adapter.notifyDataSetChanged()
            }
        }
    }

    private fun saveBlacklist() {
        BlacklistPersistence.setBlacklistedApps(this, tempBlacklistSelection)
    }
}