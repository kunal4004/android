package za.co.woolworths.financial.services.android.endlessaisle.utils

import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.wenum.Delivery


fun isEndlessAisleEnable() =
    AppConfigSingleton.endlessAisle?.isEndlessAisleEnabled

fun isEndlessAisleAvailable() =
     KotlinUtils.getDeliveryType()?.deliveryType == Delivery.STANDARD.type
             && KotlinUtils.getDeliveryType()?.deliveryType == Delivery.CNC.type
             && isEndlessAisleEnable() == true
