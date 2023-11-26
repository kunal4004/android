package za.co.woolworths.financial.services.android.shoppinglist.view

import android.annotation.SuppressLint
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

private const val MIN_DRAG = 6

/**
 * Creates revealable container with a swipe
 *
 * @param modifier to outer most composable
 * @param isRevealed - if the item is already swiped open
 * @param onExpand - callback for when the item is expanded
 * @param onCollapse - callback for when the item is collapsed
 * @param rowContent - content to display in the row
 * @param actionContent - content to display underneath the row to be revealed
 */
@SuppressLint("UnusedTransitionTargetStateParameter")
@Composable
fun SwipeToRevealView(
    modifier: Modifier = Modifier,
    rowOffsetInPx: Float = 0f,
    animationDurationInMillis: Int,
    isRevealed: Boolean,
    onExpand: () -> Unit,
    onCollapse: () -> Unit,
    rowContent: @Composable () -> Unit,
    actionContent: @Composable (RowScope.() -> Unit)
) {

    val transitionState = remember {
        MutableTransitionState(isRevealed).apply {
            targetState = !isRevealed
        }
    }
    val transition = updateTransition(transitionState, "cardTransition")

    val offsetTransition by transition.animateFloat(
        label = "offsetTransition",
        transitionSpec = { tween(durationMillis = animationDurationInMillis) },
        targetValueByState = { if (isRevealed) rowOffsetInPx else 0f },
    )

    val alphaTransition by transition.animateFloat(
        label = "alphaTransition",
        transitionSpec = { tween(durationMillis = animationDurationInMillis) },
        targetValueByState = { if (isRevealed) 1f else 0f },
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        ActionRow(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd),
            alpha = alphaTransition,
            actionContent = actionContent,
        )
        Box(
            modifier = Modifier
                .background(Color.Transparent)
                .fillMaxWidth()
                .offset { IntOffset(-offsetTransition.roundToInt(), 0) }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        when {
                            dragAmount < -MIN_DRAG -> onExpand()
                            dragAmount >= MIN_DRAG -> onCollapse()
                        }
                    }
                },
        ) {
            rowContent()
        }
    }
}

@Composable
private fun ActionRow(
    modifier: Modifier = Modifier,
    alpha: Float,
    actionContent: @Composable (RowScope.() -> Unit)
) {
    Row(
        modifier.then(
            Modifier
                .fillMaxHeight()
                .alpha(alpha = alpha)
        )
    ) {
        actionContent(this)
    }
}