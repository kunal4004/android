package za.co.woolworths.financial.services.android.ui.fragments.shop.domain

import za.co.woolworths.financial.services.android.models.dao.SessionDao.KEY
import za.co.woolworths.financial.services.android.ui.fragments.shop.domain.ShopLandingAutoNavigateChecker.Companion.SHOP_LANDING_VISITED
import za.co.woolworths.financial.services.android.util.Utils

interface ShopLandingAutoNavigateChecker {
    companion object {
        const val SHOP_LANDING_VISITED = "1"
    }
    fun isShopLandingVisited(): Boolean

    fun markShopLandingVisited()
}

class ShopLandingAutoNavigateCheckerImpl: ShopLandingAutoNavigateChecker {
    override fun isShopLandingVisited(): Boolean {
       return SHOP_LANDING_VISITED == Utils.getSessionDaoValue(KEY.SHOP_LANDING_VISITED)
    }

    override fun markShopLandingVisited() {
        Utils.sessionDaoSave(KEY.SHOP_LANDING_VISITED, SHOP_LANDING_VISITED)
    }
}