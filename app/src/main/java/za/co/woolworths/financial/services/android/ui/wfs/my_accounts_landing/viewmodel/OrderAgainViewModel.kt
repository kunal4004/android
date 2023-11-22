package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.domain.usecase.OrderAgainUC
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import javax.inject.Inject


@HiltViewModel
class OrderAgainViewModel @Inject constructor(
    val orderAgainUC: OrderAgainUC
): ViewModel() {

    init {
        callOrderAgainApi()
    }

    private fun callOrderAgainApi() {
        viewModelScope.launch {
            val plistId = KotlinUtils.extractPlistFromDeliveryDetails() ?: ""
            Log.e("nikesh", "PlistId >> $plistId")
            if(plistId.isEmpty()) {
                FirebaseManager.logException(Exception("Invalid plistId on Order Again Api."))
                return@launch
            }
            orderAgainUC(plistId).collectLatest {

            }
        }
    }

    fun onEvent() {

    }
}