package za.co.woolworths.financial.services.android.presentation.common

/**
 * Created by Kunal Uttarwar on 21/09/23.
 */
sealed class ToolbarEvents {
    object OnBackPressed: ToolbarEvents()
    data class OnRightButtonClick(val buttonText: String): ToolbarEvents()
}