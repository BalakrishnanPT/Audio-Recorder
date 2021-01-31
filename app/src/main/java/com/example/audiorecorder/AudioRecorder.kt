package com.example.audiorecorder

import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.IntDef
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class AudioRecorder(private var fileDirectory: String) {
    private var singleFile = true
    private var recorder: MediaRecorder? = null
    private val files = ArrayList<String>()
    var audioFilePath: String? = null
        private set
    var isRecording = false
        private set

    @IntDef(
        State.IDLE,
        State.STARTED,
        State.PAUSED,
        State.RESET,
        State.RESUMED,
        State.COMPLETED
    )
    @kotlin.annotation.Retention(AnnotationRetention.SOURCE)
    internal annotation class State {
        companion object {
            const val IDLE = -1
            const val STARTED = 0
            const val PAUSED = 1
            const val RESUMED = 2
            const val RESET = 3
            const val COMPLETED = 4
        }
    }

     val recordStatus = MutableStateFlow(State.IDLE)


    fun start(): Boolean {
        if (recordStatus.value != State.RESUMED || recordStatus.value != State.RESET)
            recordStatus.value = State.STARTED
        prepareRecorder()
        try {
            recorder!!.prepare()
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
        if (recorder != null) {
            recorder!!.start()
            isRecording = true
            return true
        }
        return false
    }

    fun pause(): Boolean {
        recordStatus.value = State.PAUSED
        check(!(recorder == null || !isRecording)) { "[AudioRecorder] recorder is not recording!" }
        recorder!!.stop()
        recorder!!.release()
        recorder = null
        isRecording = false
        return true
    }

    fun resume(): Boolean {
        recordStatus.value = (State.RESUMED)
        check(!isRecording) { "[AudioRecorder] recorder is recording!" }
        singleFile = false
        newRecorder()
        return start()
    }


    fun stop(): Boolean {
        if (!isRecording) {
            return merge()
        }
        if (recorder == null) {
            return false
        }
        recorder!!.stop()
        recorder!!.release()
        recorder = null
        isRecording = false
        return merge()
    }

    fun reset(delay: Long = 0L) {
        clear(true, delay)
    }

    fun clear(isReset: Boolean, delay: Long = 0L) {
        if (recorder != null || isRecording) {
            if (recorder != null) {
                recorder!!.stop()
                recorder!!.release()
            }
            recorder = null
            isRecording = false
        }
        var i = 0
        val len = files.size
        while (i < len) {
            val file = File(files[i])
            file.delete()
            i++
        }
        files.clear()

        val file = log()

        if (isReset) {
            Handler(Looper.getMainLooper()).postDelayed({
                singleFile = true
                file.deleteRecursively()
                newRecorder()
            }, delay)
        }
    }

    private fun log(): File {
        val file = File(fileDirectory)

        Log.d(TAG, "logger : $files name" +
                file.listFiles()?.map {
                    it.nameWithoutExtension + " :::"
                }
        )
        return file
    }


    private val TAG = "AudioRecorder"

    private fun merge(): Boolean {

        // If never paused, just return the file
        if (singleFile) {
            audioFilePath = files[0]
            return true
        }

        // Merge files
        val mergedFilePath = fileDirectory + "output_" + Date().time + ".mp3"
        val file1 = File(mergedFilePath)
        if (!file1.exists())
            file1.createNewFile()
        try {
            val fos = FileOutputStream(mergedFilePath)
            var i = 0
            val len = files.size
            while (i < len) {
                val file = File(files[i])
                val fis = FileInputStream(file)

                // Skip file HeaderBuilder bytes,
                // amr file HeaderBuilder's length is 6 bytes
                if (i > 0) {
                    for (j in 0..5) {
                        fis.read()
                    }
                }
                val buffer = ByteArray(512)
                var count = 0
                while (fis.read(buffer).also { count = it } != -1) {
                    fos.write(buffer, 0, count)
                }
                fis.close()
                fos.flush()
//                file.delete()
                i++
            }
            fos.flush()
            fos.close()
            log()
            audioFilePath = mergedFilePath
            return true
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return false
    }

    private fun newRecorder() {
        recorder = MediaRecorder()
    }

    private fun prepareRecorder() {
        val directory = File(fileDirectory)
        Log.d(TAG, "prepareRecorder: $directory")
        if (!directory.exists()) {
            directory.mkdirs()
        }
        val filePath = directory.absolutePath + "/rec_" + Date().time + ".amr"
        val file = File(filePath)
        if (!file.exists())
            file.createNewFile()
        files.add(filePath)
        if (recorder == null) newRecorder()
        recorder!!.setOutputFile(filePath)
        recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder!!.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB)
        recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
    }

    init {
        if (!fileDirectory.endsWith("/")) {
            fileDirectory += "/"
        }
        newRecorder()
    }
}