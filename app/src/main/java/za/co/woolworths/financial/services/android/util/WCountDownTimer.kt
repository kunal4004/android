package za.co.woolworths.financial.services.android.util

import android.os.CountDownTimer

class WCountDownTimer(millisInFuture: Long, countDownInterval: Long, var listener: TimerFinishListener) : CountDownTimer(millisInFuture, countDownInterval) {

    override fun onFinish() {
        listener.onTimerFinished()
    }

    override fun onTick(millisUntilFinished: Long) {
    }

    interface TimerFinishListener {
        fun onTimerFinished()
    }
}