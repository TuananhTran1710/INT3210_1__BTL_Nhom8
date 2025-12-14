package com.example.wink.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import kotlin.system.exitProcess

object AppIconManager {

    private val iconMap = mapOf(
        "default_logo" to "com.example.wink.MainActivityAliasDefault",
        "lost_streak" to "com.example.wink.MainActivityAliasLostStreak",
        "day3" to "com.example.wink.MainActivityAliasDay3",
        "day7" to "com.example.wink.MainActivityAliasDay7",
        "day14" to "com.example.wink.MainActivityAliasDay14",
        "day30_plus" to "com.example.wink.MainActivityAliasDay30_Plus",
        "day100" to "com.example.wink.MainActivityAliasDay100",
        "day200" to "com.example.wink.MainActivityAliasDay200",
        "day365" to "com.example.wink.MainActivityAliasDay365",
        "day500" to "com.example.wink.MainActivityAliasDay500",
        "day1000" to "com.example.wink.MainActivityAliasDay1000",
    )

    fun restartApp(context: Context, activeIconId: String) {
        val className = iconMap[activeIconId] ?: return

        val intent = Intent().apply {
            component = ComponentName(context.packageName, className)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val mgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        try {
            // Tăng thời gian lên 1 xíu (600ms) để đảm bảo process cũ chết hẳn
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 600, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        exitProcess(0)
    }

    fun changeAppIcon(context: Context, activeIconId: String) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        val activeClassName = iconMap[activeIconId] ?: return

        // 1. Tắt các alias KHÁC (Chỉ tắt nếu nó chưa bị tắt)
        iconMap.forEach { (id, className) ->
            if (id != activeIconId) {
                val componentName = ComponentName(packageName, className)
                // KIỂM TRA TRƯỚC KHI TẮT
                if (packageManager.getComponentEnabledSetting(componentName) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
                    disableComponent(packageManager, componentName)
                }
            }
        }

        // 2. Bật alias ĐANG CHỌN (Chỉ bật nếu nó chưa được bật)
        val activeComponent = ComponentName(packageName, activeClassName)
        // KIỂM TRA TRƯỚC KHI BẬT
        if (packageManager.getComponentEnabledSetting(activeComponent) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            enableComponent(packageManager, activeComponent)
        }
    }

    private fun enableComponent(pm: PackageManager, componentName: ComponentName) {
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
    }

    private fun disableComponent(pm: PackageManager, componentName: ComponentName) {
        pm.setComponentEnabledSetting(
            componentName,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
    }
}