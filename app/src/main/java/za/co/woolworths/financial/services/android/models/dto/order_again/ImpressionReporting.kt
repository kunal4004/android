package za.co.woolworths.financial.services.android.models.dto.order_again

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class ImpressionReporting(
    val isControl: Boolean? = null,
    val hasTargets: Boolean? = null,
    val tags: @RawValue List<Any>? = null,
    val experienceType: String? = null,
    val experienceName: String? = null,
    val experienceId: Int? = null,
    val experienceLabel: String? = null,
    val valiantLabel: String? = null,
    val controlAllocation: Int? = null
): Parcelable