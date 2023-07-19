package za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions

import android.content.Context
import android.content.ContextWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import dagger.hilt.android.internal.managers.ViewComponentManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import me.leolin.shortcutbadger.ShortcutBadger
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.util.BadgeUtils
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

fun Modifier.conditional(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: (Modifier.() -> Modifier)? = null
): Modifier {
    return if (condition) {
        then(ifTrue(Modifier))
    } else if (ifFalse != null) {
        then(ifFalse(Modifier))
    } else {
        this
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.testAutomationTag(
    locator: String,
): Modifier {
    return testTag(locator)
        .semantics {
            testTagsAsResourceId = true
            testTag = locator
            contentDescription = locator
        }
        .then(Modifier)

}


fun Modifier.noRippleClickable(onClick: () -> Unit): Modifier = composed {
    clickable(indication = null,
        interactionSource = remember { MutableInteractionSource() }) {
        onClick()
    }
}

fun saveToLocalDatabase(key: SessionDao.KEY, value : String?) {
    runBlocking {
        withContext(Dispatchers.IO) {
            Utils.sessionDaoSave(key,value)
        }
    }
}

fun fetchFromLocalDatabase(key: SessionDao.KEY) = runBlocking {
    val result = withContext(Dispatchers.IO) {
        Utils.getSessionDaoValue(key)
    }
    result
}

fun getJwtModel(): JWTDecodedModel = SessionUtilities.getInstance().jwt

fun setBadgeCounter(badgeCount: Int) = runBlocking {
    withContext(Dispatchers.IO) {
        if (badgeCount == 0) {
            Utils.removeBadgeCounter()
            return@withContext
        }
        val context = WoolworthsApplication.getAppContext()
        if (ShortcutBadger.isBadgeCounterSupported(context)) {
            ShortcutBadger.applyCount(context, badgeCount)
        } else {
            //fallback solution if ShortcutBadger is not supported
            BadgeUtils.setBadge(context, badgeCount)
        }
        Utils.sessionDaoSave(SessionDao.KEY.UNREAD_MESSAGE_COUNT, badgeCount.toString())
    }
}

enum class ButtonAnimationState { Pressed, Idle }

fun Modifier.bounceClick(onClick: () -> Unit) = composed {
    var buttonState by remember { mutableStateOf(ButtonAnimationState.Idle) }
    val scale by animateFloatAsState(if (buttonState == ButtonAnimationState.Pressed) 0.985f else 1f)

    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { onClick() }
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonAnimationState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonAnimationState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonAnimationState.Pressed
                }
            }
        }
}


fun Modifier.pressClickEffect() = composed {
    var buttonState by remember { mutableStateOf(ButtonAnimationState.Idle) }
    val ty by animateFloatAsState(if (buttonState == ButtonAnimationState.Pressed) 0f else -20f)

    this
        .graphicsLayer {
            translationY = ty
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { }
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonAnimationState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonAnimationState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonAnimationState.Pressed
                }
            }
        }
}


fun Modifier.shakeClickEffect() = composed {
    var buttonState by remember { mutableStateOf(ButtonAnimationState.Idle) }
    val tx by animateFloatAsState(
        targetValue = if (buttonState == ButtonAnimationState.Pressed) 0f else -50f,
        animationSpec = repeatable(
            iterations = 2,
            animation = tween(durationMillis = 50, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    this
        .graphicsLayer {
            translationX = tx
        }
        .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null,
            onClick = { }
        )
        .pointerInput(buttonState) {
            awaitPointerEventScope {
                buttonState = if (buttonState == ButtonAnimationState.Pressed) {
                    waitForUpOrCancellation()
                    ButtonAnimationState.Idle
                } else {
                    awaitFirstDown(false)
                    ButtonAnimationState.Pressed
                }
            }
        }
}

fun createLocator(default : String, key : String?) =default.replace("#", key ?: "")

fun Context.findActivity(): AppCompatActivity? = when (this) {
    is AppCompatActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun Modifier.circleLayout() =
    layout { measurable, constraints ->
// Measure the composable
        val placeable = measurable.measure(constraints)

//get the current max dimension to assign width=height
        val currentHeight = placeable.height
        val currentWidth = placeable.width
        val newDiameter = maxOf(currentHeight, currentWidth)

//assign the dimension and the center position
        layout(newDiameter, newDiameter) {
// Where the composable gets placed
            placeable.placeRelative((newDiameter-currentWidth)/2, (newDiameter-currentHeight)/2)
        }
    }


@Composable
fun Dp.roundToPx() = with(LocalDensity.current) { this@roundToPx.toPx() }


@Composable
fun Int.roundToDp() = with(LocalDensity.current) { this@roundToDp.toDp() }


@Composable
fun getContext(): Context? {
    val context = LocalContext.current
    return if (context is ViewComponentManager.FragmentContextWrapper)
        context.baseContext
    else
        context
}

@Composable
fun <T> T.AnimationBox(
    enter: EnterTransition = expandHorizontally() + fadeIn(),
    exit: ExitTransition = fadeOut() + shrinkHorizontally(),
    content: @Composable T.() -> Unit
) {
    val state = remember {
        MutableTransitionState(false).apply {
            // Start the animation immediately.
            targetState = true
        }
    }

    AnimatedVisibility(
        visibleState = state,
        enter = enter,
        exit = exit
    ) { content() }
}

