package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness

import android.os.CountDownTimer
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.CountDownTimerInterface
import java.util.concurrent.TimeUnit

class CountDownTimerImpl : CountDownTimerInterface {

    companion object {
        private const val COUNT_DOWN_TIMER_INTERVAL: Long = 1000
        private const val START_TIME_IN_MILLIS: Long = 1 * 15 * 1000
        private var mCountDownTimer: CountDownTimer? = null
        private var mTimerStatus = TimerStatus.STOPPED
        private var mTimeLeftInMillis = START_TIME_IN_MILLIS
    }

    private enum class TimerStatus {
        STARTED, STOPPED
    }

    override fun startTimer(onFinishResult: () -> Unit) {
        if (mCountDownTimer == null) {
            mCountDownTimer = object : CountDownTimer(mTimeLeftInMillis, COUNT_DOWN_TIMER_INTERVAL) {
                    override fun onTick(millisUntilFinished: Long) {
                        mTimeLeftInMillis = millisUntilFinished
                    }

                    override fun onFinish() {
                        resetTimer()
                        stopTimer()
                        onFinishResult()
                    }
                }
            mCountDownTimer?.start()

        }
        mTimerStatus = TimerStatus.STARTED
    }

    override fun stopTimer() {
        // changing the timer status to stopped
        mTimerStatus = TimerStatus.STOPPED
        mCountDownTimer?.cancel()
        mCountDownTimer = null
    }

    override fun startStopTimer(onFinish: () -> Unit) {
        when (mTimerStatus) {
            TimerStatus.STARTED -> stopTimer()
            TimerStatus.STOPPED -> startTimer { onFinish() }
        }
    }

    override fun resetTimer() {
        mTimeLeftInMillis = START_TIME_IN_MILLIS
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