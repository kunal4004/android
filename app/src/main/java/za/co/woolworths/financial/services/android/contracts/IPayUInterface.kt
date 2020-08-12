package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.AddCardResponse

interface IPayUInterface {
    fun onAddNewCardSuccess(token: AddCardResponse) {}
    fun onAddCardProgressStarted() {}
    fun onAddCardFailureHandler() {}
}