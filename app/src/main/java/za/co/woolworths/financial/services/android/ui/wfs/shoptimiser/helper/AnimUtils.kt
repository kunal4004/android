package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper.ShopOptimiserConstant.ShopOptimiserConstant.EXPAND_TRANSITION_DURATION

/**
 * Calculates the rotation angle for a component based on the expansion state.
 * @param isExpanded Whether the component is in an expanded state or not.
 * @return The rotation angle in degrees.
 */

@Composable
fun rotateComponent(isExpanded: Boolean): Float {
    val transition = updateTransition(targetState = isExpanded, label = "transition")

    val arrowRotationDegree by transition.animateFloat(
        transitionSpec = { tween(durationMillis = EXPAND_TRANSITION_DURATION) },
        label = "rotationDegreeTransition"
    ) { if (it) 0f else 180f }

    return arrowRotationDegree
}
