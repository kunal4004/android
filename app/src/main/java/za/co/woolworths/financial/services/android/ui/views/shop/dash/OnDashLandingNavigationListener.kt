package za.co.woolworths.financial.services.android.ui.views.shop.dash

import android.view.View
import za.co.woolworths.financial.services.android.models.dto.shop.Banner
import za.co.woolworths.financial.services.android.ui.adapters.shop.dash.ProductCarouselItemViewHolder

interface OnDashLandingNavigationListener {
    fun onDashLandingNavigationClicked(
        position: Int,
        view: View?,
        item: Banner,
        headerText: String?
    )
    fun setProductCarousalItemViewHolder(viewHolder: ProductCarouselItemViewHolder)
}