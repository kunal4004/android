package za.co.woolworths.financial.services.android.ui.wfs.component.pull_to_refresh

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.ui.wfs.component.SpacerHeight12dp
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.pull_to_refresh_box_icon
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.analytics.AutomationTestScreenLocator.Locator.pull_to_refresh_box_row
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.conditional
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.testAutomationTag
import za.co.woolworths.financial.services.android.ui.wfs.theme.Dimens
import za.co.woolworths.financial.services.android.ui.wfs.theme.Margin
@Composable
fun WfsPullToRefreshUI(
        isEnabled : Boolean,
        isAccountRefreshingTriggered: Boolean,
        isAccountRefreshing: (Boolean) -> Unit,
        content: @Composable (PullToRefreshState) -> Unit
) {
    PullToRefresh(
        state = rememberPullToRefreshState(isRefreshing = isAccountRefreshingTriggered),
        onRefresh = { isAccountRefreshing(true) },
        enabled = isEnabled,
        dragMultiplier = 1f,
        refreshTriggerDistance = Dimens.pull_to_refresh_trigger_distance_dp,
        refreshingOffset = Dimens.pull_to_refresh_offset_dp,
        indicatorPadding = PaddingValues(0.dp),
        indicator = { state, refreshTriggerDistance, _ ->
            PullToRefreshIndicator( state = state, refreshTriggerDistance = refreshTriggerDistance)
        }
    ) {
        content(it)
    }
}

@Composable
private fun PullToRefreshIndicator(
    refreshTriggerDistance: Dp,
    state: PullToRefreshState
) {
    val refreshTriggerPx = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val indicatorSize = Dimens.thirty_six_dp
    val indicatorHeightPx = with(LocalDensity.current) { indicatorSize.toPx() }
    var rotation by remember { mutableStateOf(0f) }
    val scaleFraction: Float
    val alphaFraction: Float
    if (!state.isRefreshing) {
        val progress = (state.contentOffset / refreshTriggerPx.coerceAtLeast(1f))
            .coerceIn(0f, 1f)
        rotation = progress * 180
        scaleFraction = LinearOutSlowInEasing.transform(progress)
        alphaFraction = progress
    } else {
        val transition = rememberInfiniteTransition()
        val rotationAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1332, // 1 and 1/3 second
                    easing = LinearEasing
                )
            )
        )
        rotation = rotationAnimation
        scaleFraction = 1f
        alphaFraction = 1f
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testAutomationTag(pull_to_refresh_box_row)
            .conditional(state.isRefreshing, ifTrue = { background(Color.White) }, ifFalse = null)
            .padding(bottom = Margin.dp3)
            .graphicsLayer {
                translationY = (state.contentOffset - indicatorHeightPx) / 2f
                scaleX = scaleFraction
                scaleY = scaleFraction
                alpha = alphaFraction
                clip = false
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.ic_refresh),
                contentDescription = stringResource(id = R.string.cptr_refreshing),
                modifier = Modifier
                    .size(indicatorSize)
                    .testAutomationTag(pull_to_refresh_box_icon)
                    .graphicsLayer(rotationZ = rotation)
            )
            SpacerHeight12dp()
        }
    }
}
