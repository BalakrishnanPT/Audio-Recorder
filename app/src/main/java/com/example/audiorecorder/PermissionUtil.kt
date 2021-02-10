package com.example.audiorecorder

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionUtil(val activity: Activity) {

    lateinit var dothis: () -> Unit
    private val PERMISSIONS_REQUEST_CODE = 101

    fun runWithPermission(doSomething: () -> Unit) {
        val audioRecord = ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.RECORD_AUDIO
        )

        dothis = doSomething
        if (audioRecord != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(
                            Manifest.permission.RECORD_AUDIO
                    ),
                    PERMISSIONS_REQUEST_CODE
            )
        } else dothis()
    }

     fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    AlertDialog.Builder(activity)
                            .setTitle("Permission Denied")
                            .setMessage("Permission is denied, Please allow permissions from App Settings.")
                            .setPositiveButton("App Settings",
                                    DialogInterface.OnClickListener { dialogInterface, i ->
                                        // send to app settings if permission is denied permanently
                                        val intent = Intent()
                                        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                        val uri =
                                                Uri.fromParts("package", activity.packageName, null)
                                        intent.data = uri
                                        activity.startActivity(intent)
                                    })
                            .setNegativeButton("Cancel", null)
                            .show()
                } else dothis()
            }
        }
    }
}