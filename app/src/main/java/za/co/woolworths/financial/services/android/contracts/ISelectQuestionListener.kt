package za.co.woolworths.financial.services.android.contracts

import za.co.woolworths.financial.services.android.models.dto.FAQDetail

interface ISelectQuestionListener {
    fun onQuestionSelected(faqDetail: FAQDetail?)
}