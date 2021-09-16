package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract

interface CountDownTimerInterface {
    fun stopTimer()
    fun resetTimer()
    fun startTimer(onFinishResult: () -> Unit)
    fun startStopTimer(onFinish: () -> Unit)
    fun hmsTimeFormatter(milliSeconds: Long): String
}