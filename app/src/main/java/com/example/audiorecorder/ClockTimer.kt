package com.example.audiorecorder

import androidx.lifecycle.LiveData
import java.util.*
import kotlin.concurrent.fixedRateTimer

data class ClockData(var hours: Int = 0, var mins: Int = 0, var seconds: Int = 0)
class ClockTimer(val period: Long = 1000) : LiveData<ClockData>() {
    private var seconds = 0
    private var timer: Timer? = null
    private val clockData = ClockData()
    var isActive = false

    var mLimit = -1
    var onLimitReached: () -> Unit = {}


    fun setLimit(seconds: Int, unit: () -> Unit) {
        mLimit = seconds
        onLimitReached = unit
    }

    fun getRecordedTime(): Int {
        return (clockData.hours * 3600) + (clockData.mins * 60) + clockData.seconds
    }

    fun start() {
        if (!isActive)
            timer = fixedRateTimer("timer", false, 0L, period) {
                if (!isActive)
                    isActive = true
                seconds += 1
                if (seconds == mLimit) {
                    onLimitReached()
                    cancel()
                }
                clockData.hours = seconds / 3600
                clockData.mins =
                    ((seconds % 3600) / 60)
                clockData.seconds = (seconds % 60)
                postValue(clockData)
            }
    }

    fun pause() {
        if (!isActive) return
        timer?.cancel()
        timer = null
        isActive = false
    }

    fun stop() {
        if (!isActive) return
        timer?.cancel()
        timer = null
        isActive = false
        reset()
    }

    fun reset() {
        seconds = 0
        clockData.seconds = 0
        clockData.mins = 0
        clockData.hours = 0
        postValue(clockData)
    }

}