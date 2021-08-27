package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness

import android.os.CountDownTimer
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.CountDownTimerInterface
import java.util.concurrent.TimeUnit

class CountDownTimerImpl : CountDownTimerInterface {

    private val countDownTimerInterval: Long = 1000
    private var timeCountInMilliSeconds: Long = 1 * 10 * 1000
    private var countDownTimer: CountDownTimer? = null
    private var timerStatus = TimerStatus.STOPPED

    private enum class TimerStatus {
        STARTED, STOPPED
    }

    override fun startCountDownTimer(onFinish: () -> Unit) {
        countDownTimer = object : CountDownTimer(timeCountInMilliSeconds, countDownTimerInterval) {
            override fun onTick(millisUntilFinished: Long) {}

            override fun onFinish() {
                // changing the timer status to stopped
                timerStatus = TimerStatus.STOPPED
                stopCountDownTimer()
                onFinish()
            }
        }

        countDownTimer?.start()
    }

    override fun startStopCountdownTimer(onFinish: () -> Unit) {
        if (timerStatus === TimerStatus.STOPPED) {
            // call to initialize the timer values
            setTimerValues()
            timerStatus = TimerStatus.STARTED
            // call to start the count down timer
            startCountDownTimer(onFinish)
        } else {
            // changing the timer status to stopped
            timerStatus = TimerStatus.STOPPED
            stopCountDownTimer()
        }
    }

    override fun setTimerValues() {
        val time = 1   //Time time in minutes
        // assigning values after converting to milliseconds
        timeCountInMilliSeconds = time * 10 * 1000.toLong()
    }

    override fun stopCountDownTimer() {
        countDownTimer?.cancel()
    }

    override fun refreshToken() {
        // changing the timer status to stopped
        timerStatus = TimerStatus.STOPPED
        stopCountDownTimer()
    }

    override fun hmsTimeFormatter(milliSeconds: Long): String {
        return String.format(
                "%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(milliSeconds),
                TimeUnit.MILLISECONDS.toMinutes(milliSeconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(
                                milliSeconds
                        )
                ),
                TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(
                                milliSeconds
                        )
                )
        )
    }
}