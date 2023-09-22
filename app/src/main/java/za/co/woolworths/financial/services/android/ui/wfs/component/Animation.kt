package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue

@Composable
fun rotationAnimation(): Float {
    val transition = rememberInfiniteTransition()
    val animatedValue = transition.animateValue(
        0f,
        1f,
        Float.VectorConverter,
        infiniteRepeatable(
            animation = tween(
                durationMillis = 1332, // 1 and 1/3 second
                easing = LinearEasing
            )
        )
    )
    return animatedValue.value * 360
}


@Composable
fun animateAlpha(isAnimating : Boolean): Float {
    val animateAlpha: Float by animateFloatAsState(
            targetValue = if (isAnimating) 1f else 0f,
            animationSpec = tween(
                    durationMillis = 2000,
                    easing = LinearEasing,
            )
    )
    return animateAlpha
}
