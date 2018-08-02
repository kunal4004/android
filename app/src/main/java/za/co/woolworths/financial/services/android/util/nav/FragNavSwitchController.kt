package za.co.woolworths.financial.services.android.util.nav

interface FragNavSwitchController {
    fun switchTab(@FragNavController.TabIndex index: Int, transactionOptions: FragNavTransactionOptions?)
}
