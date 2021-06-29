package za.co.woolworths.financial.services.android.models.dto.bpi

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DefaultLabel(
    val howToClaim: Int = R.string.bpi_how_to_claim,
    val requiredDocuments: Int = R.string.requiredDocuments,
    val claimReasonTitle: Int = R.string.claimReasonTitle,
    val overviewTitle: Int = R.string.overviewTitle,
    val requiredForm: Int = R.string.requiredForm,
    val submitForm: Int = R.string.submitForm,
    val submitDescription: Int = R.string.submitDescription
) : Parcelable