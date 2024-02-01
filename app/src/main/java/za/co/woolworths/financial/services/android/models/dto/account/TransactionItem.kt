package za.co.woolworths.financial.services.android.models.dto.account

class TransactionItem(val amount: Float?, var date: String?, val description: String?, var month: String?, var headerCount: Int, var itemCount: Int) : Transaction()