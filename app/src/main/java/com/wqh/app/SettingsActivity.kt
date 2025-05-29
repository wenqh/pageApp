package com.wqh.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : Activity() {

    private lateinit var textAccessibilityStatus: TextView
    private lateinit var buttonOpenAccessibilitySettings: Button
    private lateinit var buttonManageBlacklist: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

//        textAccessibilityStatus = findViewById(R.id.text_accessibility_status)
        buttonOpenAccessibilitySettings = findViewById(R.id.button_open_accessibility_settings)
        buttonManageBlacklist = findViewById(R.id.button_manage_blacklist)

        buttonOpenAccessibilitySettings.setOnClickListener {
            openAccessibilitySettings() // 调用简化版的方法
        }

        buttonManageBlacklist.setOnClickListener {
            startActivity(Intent(this, BlacklistActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
//        updateAccessibilityStatusText()
    }

    // --- 简化版的 openAccessibilitySettings 方法 ---
    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            // 直接使用硬编码的字符串进行提示
            Toast.makeText(
                this,
                "请在无障碍设置列表中找到并启用 '按键翻页助手服务'", // 简化后的提示信息
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            // 仍然保留对 startActivity 可能失败的捕获
            Toast.makeText(this, "无法打开系统无障碍设置，请手动前往。", Toast.LENGTH_LONG).show()
            Log.e("SettingsActivity", "打开无障碍设置时出错", e)
        }
    }
    // --- 简化版方法结束 ---

    /*private fun updateAccessibilityStatusText() {
        if (isAccessibilityServiceEnabled()) {
            textAccessibilityStatus.text = "服务状态：已启用"
            textAccessibilityStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_green_dark))
        } else {
            textAccessibilityStatus.text = "服务状态：未启用 (点击上方按钮开启)"
            textAccessibilityStatus.setTextColor(ContextCompat.getColor(this, android.R.color.holo_red_dark))
        }
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager // 使用安全转换 as?
        if (accessibilityManager == null) {
            Log.e("SettingsActivity", "无法获取 AccessibilityManager 服务。")
            return false // 如果无法获取服务，则认为未启用
        }

        // 获取所有已启用的无障碍服务信息列表
        // FEEDBACK_ALL_MASK 是 AccessibilityManager 的一个常量
        val enabledServices = try {
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityManager.FEEDBACK_ALL_MASK)
        } catch (e: Exception) {
            Log.e("SettingsActivity", "获取已启用无障碍服务列表失败", e)
            return false // 获取列表失败也认为未启用
        }

        if (enabledServices == null) { // 有些系统可能在特定情况下返回 null
            Log.w("SettingsActivity", "getEnabledAccessibilityServiceList 返回 null。")
            return false
        }


        // 构建我们自己应用的无障碍服务的组件名称
        // 确保 KeyScrollService 是您的无障碍服务的正确类名，并且可以被解析
        val expectedComponentName: ComponentName
        try {
            // 确保 KeyScrollService::class.java 能被正确解析
            // 如果这里报错 "No value passed for parameter 'serviceClass'" 或类型不匹配
            // 意味着 KeyScrollService 这个类找不到或有问题
            expectedComponentName = ComponentName(this, KeyScrollService::class.java)
        } catch (e: Exception) {
            Log.e("SettingsActivity", "创建 expectedComponentName 失败，请检查 KeyScrollService 是否正确。", e)
            return false // 如果无法创建目标组件名，则无法比较
        }


        // 遍历所有已启用的服务
        for (enabledServiceInfo in enabledServices) {
            val resolveInfo = enabledServiceInfo.resolveInfo
            if (resolveInfo != null) {
                val serviceInfo = resolveInfo.serviceInfo
                if (serviceInfo != null && !TextUtils.isEmpty(serviceInfo.packageName) && !TextUtils.isEmpty(serviceInfo.name)) {
                    // 从 serviceInfo (类型是 android.content.pm.ServiceInfo) 的包名和类名
                    // 手动创建一个 ComponentName 对象
                    val actualComponentName = ComponentName(serviceInfo.packageName, serviceInfo.name)

                    if (actualComponentName == expectedComponentName) {
                        Log.i("SettingsActivity", "无障碍服务 (${expectedComponentName.shortClassName}) 已启用。")
                        return true
                    }
                } else {
                    Log.w("SettingsActivity", "在 enabledServices 中发现一个不完整的 ServiceInfo。")
                }
            }
        }

        Log.i("SettingsActivity", "无障碍服务 (${expectedComponentName.shortClassName}) 未启用。")
        return false
    }*/
}