package za.co.woolworths.financial.services.android.enhancedSubstitution.util

import android.os.Bundle
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery

fun triggerFirebaseEventForAddSubstitution(itemName: String?, itemId: String?, itemPrice: Float) {
    val viewItem = Bundle()
    viewItem.apply {
        putString(FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_ID, itemId)
        putString(FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_NAME, itemName)
        putFloat(FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_PRICE, itemPrice)
    }
    AnalyticsManager.logEvent(
        FirebaseManagerAnalyticsProperties.ADD_SUBSTITUTION,
        viewItem
    )
}

 fun triggerFirebaseEventForSubstitution(isbackButtonEvent:Boolean = false, selectionChoice: String= "") {
    val bundle = Bundle()
    if (isbackButtonEvent) {
        bundle.putString(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, FirebaseManagerAnalyticsProperties.PropertyValues.BACK)
    } else {
        bundle.putString(FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE, selectionChoice)
    }
    AnalyticsManager.logEvent(FirebaseManagerAnalyticsProperties.SUBSTITUTION, bundle)
}

fun isEnhanceSubstitutionFeatureEnable() =
    AppConfigSingleton.enhanceSubstitution?.isEnhancedSubstitutionEnable

fun isEnhanceSubstitutionFeatureAvailable() =
     KotlinUtils.getDeliveryType()?.deliveryType == Delivery.DASH.type && isEnhanceSubstitutionFeatureEnable() == true
