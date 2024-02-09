package za.co.woolworths.financial.services.android.presentation.common.awarenessmodal

import android.os.Parcelable
import com.awfs.coordination.R
import kotlinx.parcelize.Parcelize

@Parcelize
data class AwarenessModalState(
    val state: String = "",
    val iconAwareness: Int = R.drawable.image_placeholder,
    val awarenessTitle: Int = R.string.empty,
    val awarenessDesc: Int = R.string.empty,
    val noteText: Int = R.string.empty,
    val confirmButton: Int = R.string.empty,
    val dismissButton: Int = R.string.empty,
    val isChecked: Boolean = false
): Parcelable
