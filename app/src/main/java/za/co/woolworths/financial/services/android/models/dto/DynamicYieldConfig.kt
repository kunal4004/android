import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DynamicYieldConfig(
    var isDynamicYieldEnabled: Boolean? = false,
    val minimumSupportedAppBuildNumber: Int?
): Parcelable