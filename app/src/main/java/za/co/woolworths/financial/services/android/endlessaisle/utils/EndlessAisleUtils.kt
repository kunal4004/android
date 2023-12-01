package za.co.woolworths.financial.services.android.endlessaisle.utils

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.KotlinUtils


fun isEndlessAisleEnable() =
    AppConfigSingleton.endlessAisle?.isEndlessAisleEnabled

fun isEndlessAisleAvailable() =
    (KotlinUtils.isDeliveryOptionStandard()
            || KotlinUtils.isDeliveryOptionClickAndCollect()) && isEndlessAisleEnable() == true

fun getBarcodeMessage() =
    AppConfigSingleton.endlessAisle?.barcodeMessage
