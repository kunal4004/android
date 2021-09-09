package za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness

import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.CountDownTimerInterface
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.ScreenBrightnessInterface
import za.co.woolworths.financial.services.android.ui.fragments.wreward.brightness.contract.ShakeDetectorInterface

class ScreenBrightnessDelegate(
    private val screenBrightness: ScreenBrightnessInterface,
    private val countDownTimer: CountDownTimerInterface,
    private val shakeDetector: ShakeDetectorInterface
) : ScreenBrightnessInterface by screenBrightness,
    CountDownTimerInterface by countDownTimer,
        ShakeDetectorInterface by shakeDetector