package com.test.timeline

import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.content.pm.PackageManager





class PermissionUtils {

    /**
     * Requests the fine location permission. If a rationale with an additional explanation should
     * be shown to the user, displays a dialog that triggers the request.
     */

    fun requestPermission(activity: AppCompatActivity, requestId: Int,
                          permission: String, finishActivity: Boolean) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
            // Display a dialog with rationale.
            RationaleDialog().newInstance(requestId, finishActivity)
                    .show(activity.supportFragmentManager, "dialog")
        } else {
            // Location permission has not been granted yet, request it.
            ActivityCompat.requestPermissions(activity, arrayOf(permission), requestId)

        }
    }


    /**
     * Checks if the result contains a [PackageManager.PERMISSION_GRANTED] result for a
     * permission from a runtime permissions request.
     *
     * @see android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback
     */
    fun isPermissionGranted(grantPermissions: Array<String>, grantResults: IntArray,
                            permission: String): Boolean {
        for (i in grantPermissions.indices) {
            if (permission == grantPermissions[i]) {
                return grantResults[i] == PackageManager.PERMISSION_GRANTED
            }
        }
        return false
    }




}