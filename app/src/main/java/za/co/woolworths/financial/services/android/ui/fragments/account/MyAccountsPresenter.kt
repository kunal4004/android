package za.co.woolworths.financial.services.android.ui.fragments.account

import za.co.woolworths.financial.services.android.ui.fragments.account.apply_now.ViewApplicationStatusInterface

class MyAccountsPresenter(private val viewApplicationStatus: ViewApplicationStatusInterface) :
    ViewApplicationStatusInterface by viewApplicationStatus

