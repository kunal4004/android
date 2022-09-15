package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.view.View
import za.co.woolworths.financial.services.android.models.dto.shop.Banner

interface OnDashLandingNavigationListener {
    fun onDashLandingNavigationClicked(view: View?, item: Banner)
}