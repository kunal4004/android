package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class EmailUsRequest(val preferredName:String?,val preferredEmail:String?,val enquiryType:String?,val emailBody:String?):Parcelable