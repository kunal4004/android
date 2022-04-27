package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.creditcard

import za.co.woolworths.financial.services.android.models.dto.Card
import javax.inject.Inject

interface ICreditCard {
    fun getCreditCardNumber(cards: ArrayList<Card>?): String?
}

class CreditCardImpl @Inject constructor() : ICreditCard {

    override fun getCreditCardNumber(cards: ArrayList<Card>?): String? {
        return cards?.takeIf { it.isNotEmpty() }?.let { it[0].absaCardToken }
    }

}