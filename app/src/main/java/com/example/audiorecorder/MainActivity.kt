package com.example.audiorecorder

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "MainActivity"

class MainActivity : AppCompatActivity() {
    private lateinit var mediaRecorder: AudioRecorder
    private val permissionUtil: PermissionUtil by lazy { PermissionUtil(this) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        createMediaRecorder()
        permissionUtil.runWithPermission {
            mediaRecorder.start()
            GlobalScope.launch {
                delay(10000)
                mediaRecorder.stop()
                Log.d(TAG, "onCreate: stoped ${mediaRecorder.getFilePath()}")

            }
        }
    }

    private fun createMediaRecorder() {
        val createTempFile = File(cacheDir, "recording1/")
        if (!createTempFile.exists())
            createTempFile.mkdirs()
        mediaRecorder = AudioRecorder(createTempFile.absolutePath)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        permissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}