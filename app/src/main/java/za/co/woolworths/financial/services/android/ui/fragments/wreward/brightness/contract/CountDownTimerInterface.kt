package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract

interface CountDownTimerInterface {
    fun startCountDownTimer(onFinish: () -> Unit)
    fun startStopCountdownTimer(onFinish: () -> Unit)
    fun setTimerValues()
    fun stopCountDownTimer()
    fun refreshToken()
    fun hmsTimeFormatter(milliSeconds: Long): String
}