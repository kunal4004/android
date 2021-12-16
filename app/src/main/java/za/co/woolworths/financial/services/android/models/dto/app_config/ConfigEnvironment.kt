package za.co.woolworths.financial.services.android.models.dto.app_config

import android.os.Parcelable
import com.google.gson.JsonElement
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigEnvironment(
    val base_url: String?,
    val ssoRedirectURI: String?,
    val stsURI: String?,
    val ssoRedirectURILogout: String?,
    val ssoUpdateDetailsRedirectUri: String?,
    val wwTodayURI: String?,
    val authenticVersionStamp: String? = "",
    val storeStockLocatorConfigStartRadius: Int?,
    val storeStockLocatorConfigEndRadius: Int?,
    val storeStockLocatorConfigFoodProducts: Boolean?,
    val storeStockLocatorConfigClothingProducts: Boolean?,
    val storeCardBlockReasons: JsonElement?,
    val emailSizeKB: Long = 0
) : Parcelable