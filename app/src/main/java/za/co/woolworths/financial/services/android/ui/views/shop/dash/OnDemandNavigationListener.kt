package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.view.View
import za.co.woolworths.financial.services.android.models.dto.RootCategory

interface OnDemandNavigationListener {
    fun onDemandNavigationClicked(view: View?, categoryItem: RootCategory)
}
