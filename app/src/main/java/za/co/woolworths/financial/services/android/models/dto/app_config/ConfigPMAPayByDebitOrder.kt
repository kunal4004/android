package za.co.woolworths.financial.services.android.models.dto.app_config // ktlint-disable package-name

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ConfigPMAPayByDebitOrder(
    var aboutDebitOrder: String,
    var contactToCallOnHowToSetDebitOrder: String,
    var breakDownOfDebitOrderAmount: String,
    var descriptionNoteOnRequirementsForSettingDebitOrder: String,
) : Parcelable
