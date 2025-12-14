package com.example.wink.util // Hoặc package của bạn

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.example.wink.BuildConfig // Import BuildConfig để lấy Application ID

object AppIconManager {

    // Map giữa ID trong ViewModel và tên Class Alias trong Manifest
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

    fun changeAppIcon(context: Context, activeIconId: String) {
        val packageManager = context.packageManager
        val packageName = context.packageName

        // 1. Tắt tất cả các alias khác
        iconMap.forEach { (id, className) ->
            if (id != activeIconId) {
                disableComponent(packageManager, ComponentName(packageName, className))
            }
        }

        // 2. Bật alias được chọn
        val activeClassName = iconMap[activeIconId] ?: return
        enableComponent(packageManager, ComponentName(packageName, activeClassName))
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