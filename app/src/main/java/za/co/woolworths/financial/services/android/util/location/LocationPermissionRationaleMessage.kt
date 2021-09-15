package za.co.woolworths.financial.services.android.util.location

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LocationPermissionRationaleMessage(var rationaleTitle: String = "Location permission required",
                                              var rationaleMessage: String = "Location permission is required. Please grant permissions.",
                                              var rationaleYes: String = "Yes",
                                              var rationaleNo: String = "No") : Parcelable