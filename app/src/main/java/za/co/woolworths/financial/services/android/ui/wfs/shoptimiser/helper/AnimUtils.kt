package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.helper

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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

/**
 * Composable function for creating an exit animation that shrinks vertically and fades out.
 * @param duration The duration of the animation in milliseconds (default is EXPAND_TRANSITION_DURATION).
 * @return An animation specification for exit transitions.
 */
@Composable
fun exitShrinkVerticallyFadeOutAnimation(duration: Int = EXPAND_TRANSITION_DURATION) = remember {
    shrinkVertically(
        shrinkTowards = Alignment.Top,
        animationSpec = tween(duration)
    ) + fadeOut(
        animationSpec = tween(duration)
    )
}

/**
 * Composable function for creating an enter animation that expands vertically and fades in.
 * This function defines an animation specification for enter transitions, which includes
 * vertical expansion from the top and fading in with an initial alpha of 0.3.
 * @param duration The duration of the animation in milliseconds (default is EXPAND_TRANSITION_DURATION).
 * @return An animation specification for enter transitions.
 */
@Composable
fun enterExpandVerticallyFadeInAnimation(duration: Int = EXPAND_TRANSITION_DURATION)  = remember {
    expandVertically(
        expandFrom = Alignment.Top,
        animationSpec = tween(duration)
    ) + fadeIn(
        initialAlpha = 0.3f,
        animationSpec = tween(duration)
    )
}

