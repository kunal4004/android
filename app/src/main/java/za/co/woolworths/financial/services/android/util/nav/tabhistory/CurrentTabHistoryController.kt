package za.co.woolworths.financial.services.android.util.nav.tabhistory

import android.os.Bundle

import za.co.woolworths.financial.services.android.util.nav.FragNavPopController
import za.co.woolworths.financial.services.android.util.nav.FragNavTransactionOptions

class CurrentTabHistoryController(fragNavPopController: FragNavPopController) : BaseFragNavTabHistoryController(fragNavPopController) {

    @Throws(UnsupportedOperationException::class)
    override fun popFragments(popDepth: Int,
                              transactionOptions: FragNavTransactionOptions?): Boolean {
        return fragNavPopController.tryPopFragments(popDepth, transactionOptions) > 0
    }

    override fun switchTab(index: Int) {}

    override fun onSaveInstanceState(outState: Bundle) {}

    override fun restoreFromBundle(savedInstanceState: Bundle?) {}
}
