package com.kontarawa.shelter.launcher.data

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

class InstalledAppsRepository(private val context: Context) {

    @SuppressLint("QueryPermissionsNeeded")
    suspend fun getLaunchableApps(): List<AppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val selfPackage = context.packageName

        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val leanbackIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)

        val packageNames = buildList {
            addAll(queryActivities(pm, launcherIntent).map { it.activityInfo.packageName })
            addAll(queryActivities(pm, leanbackIntent).map { it.activityInfo.packageName })
        }
            .asSequence()
            .filter { it != selfPackage }
            .distinct()
            .toList()

        packageNames
            .mapNotNull { pkg ->
                runCatching {
                    val appInfo = getApplicationInfo(pm, pkg)
                    AppItem(
                        label = pm.getApplicationLabel(appInfo).toString(),
                        packageName = pkg,
                        icon = pm.getApplicationIcon(appInfo),
                    )
                }.getOrNull()
            }
            .sortedBy { it.label.lowercase(Locale.ROOT) }
            .toList()
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun queryActivities(pm: PackageManager, intent: Intent): List<ResolveInfo> {
        return if (Build.VERSION.SDK_INT >= 33) {
            pm.queryIntentActivities(
                intent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(intent, PackageManager.MATCH_ALL)
        }
    }

    private fun getApplicationInfo(pm: PackageManager, packageName: String) =
        if (Build.VERSION.SDK_INT >= 33) {
            pm.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(0L)
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getApplicationInfo(packageName, 0)
        }
}
