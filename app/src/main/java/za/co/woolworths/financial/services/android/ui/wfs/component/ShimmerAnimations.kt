package za.co.woolworths.financial.services.android.ui.wfs.component

import androidx.compose.animation.core.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import za.co.woolworths.financial.services.android.ui.wfs.theme.ShimmerColorShades
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember

// Domain Layer
data class ShimmerAnimationSpec(
    val durationMillis: Int,
    val easing: Easing,
    val repeatMode: RepeatMode
)

interface ShimmerUseCase {
    @Composable
    fun animate(spec: ShimmerAnimationSpec): Brush
}

class ShimmerUseCaseImpl : ShimmerUseCase {
    @Composable
    override fun animate(spec: ShimmerAnimationSpec): Brush {
        val transition = rememberInfiniteTransition()


        val translateAnim = transition.animateFloat(
            initialValue = 0f,
            targetValue = 1000f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = spec.durationMillis,
                    easing = spec.easing
                ),
                repeatMode = spec.repeatMode
            )
        )

        val alpha by transition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = LinearEasing
                ),
                repeatMode = RepeatMode.Reverse
            )
        )

        val alphaColorShades = ShimmerColorShades.map { color ->
            color.copy(alpha = alpha)
        }

        return  Brush.linearGradient(
            colors = alphaColorShades,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )
    }
}

// Presentation Layer

@Composable
fun BrushShimmerEffect(content: @Composable (Brush) -> Unit) {
    val animationSpec = remember {
        ShimmerAnimationSpec(
            durationMillis = 300,
            easing = FastOutSlowInEasing,
            repeatMode = RepeatMode.Restart
        )
    }

    val shimmerUseCase = remember {
        ShimmerUseCaseImpl()
    }

    val brush = shimmerUseCase.animate(animationSpec)

    content(brush)
}

