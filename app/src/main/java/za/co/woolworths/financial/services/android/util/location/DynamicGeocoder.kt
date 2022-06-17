package za.co.woolworths.financial.services.android.util.location

import android.content.Context
import android.location.Geocoder
import com.huawei.hms.location.GetFromLocationRequest
import com.huawei.hms.location.LocationServices
import za.co.woolworths.financial.services.android.util.FirebaseManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.location.model.DynamicGeocoderAddress
import java.util.*

object DynamicGeocoder {
    fun getAddressFromLocation(context: Context?, latitude: Double?, longitude: Double?, callback: (DynamicGeocoderAddress?) -> Unit) {
        if (context != null && latitude != null && longitude != null) {
            try {
                when {
                    Utils.isGooglePlayServicesAvailable() -> {
                        val geoCoderService = Geocoder(context, Locale.getDefault())
                        val addresses = geoCoderService.getFromLocation(latitude, longitude, 1)
                        addresses.firstOrNull()?.let {
                            callback.invoke(
                                DynamicGeocoderAddress(
                                    addressLine = it.getAddressLine(0),
                                    street = null,
                                    city = it.locality,
                                    suburb = it.subLocality,
                                    state = it.adminArea,
                                    countryName = it.countryName,
                                    countryCode = it.countryCode,
                                    postcode = it.postalCode
                                )
                            )
                        } ?: kotlin.run {
                            callback.invoke(null)
                        }
                    }
                    Utils.isHuaweiMobileServicesAvailable() -> {
                        val geocoderService = LocationServices.getGeocoderService(context, Locale.getDefault())
                        geocoderService
                            .getFromLocation(GetFromLocationRequest(latitude, longitude, 1))
                            .addOnSuccessListener { addresses ->
                                addresses.firstOrNull()?.let {
                                    var addressLine = it.featureName
                                    if (addressLine.isNullOrEmpty()) {
                                        addressLine = arrayOf(
                                            it.street,
                                            it.city,
                                            it.county,
                                            it.state,
                                            it.countryName
                                        ).filter { item -> !item.isNullOrEmpty() }.joinToString(separator = ", ")
                                    }
                                    callback.invoke(
                                        DynamicGeocoderAddress(
                                            addressLine = addressLine,
                                            street = it.street,
                                            city = it.city,
                                            suburb = it.county,
                                            state = it.state,
                                            countryName = it.countryName,
                                            countryCode = it.countryCode,
                                            postcode = it.postalCode
                                        )
                                    )
                                } ?: kotlin.run {
                                    callback.invoke(null)
                                }
                            }
                            .addOnFailureListener {
                                FirebaseManager.logException(it)
                                callback.invoke(null)
                            }
                    }
                    else -> {
                        callback.invoke(null)
                    }
                }
            } catch (e: Exception) {
                FirebaseManager.logException(e)
                callback.invoke(null)
            }
        } else {
            callback.invoke(null)
        }
    }
}