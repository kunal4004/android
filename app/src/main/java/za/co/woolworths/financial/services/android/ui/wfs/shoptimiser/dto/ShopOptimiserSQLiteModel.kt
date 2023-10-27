package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Parcelable data class representing the Shop Optimiser SQLite Model.
 * @property accountCacheTimestamp The timestamp indicating the last update time for account data cache.
 * @property isDefaultPdpDisplayed Boolean flag indicating whether the default PDP (Product Detail Page) is displayed.
 */
@Parcelize
data class ShopOptimiserSQLiteModel(
    val accountCacheTimestamp: String? = null,
    val isDefaultPdpDisplayed: Boolean = true
) : Parcelable
