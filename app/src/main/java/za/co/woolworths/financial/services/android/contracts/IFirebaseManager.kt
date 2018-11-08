package za.co.woolworths.financial.services.android.contracts

import com.google.firebase.analytics.FirebaseAnalytics

interface IFirebaseManager {

    fun getAnalytics(): FirebaseAnalytics
}
