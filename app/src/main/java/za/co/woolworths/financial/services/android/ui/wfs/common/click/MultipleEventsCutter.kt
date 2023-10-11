package za.co.woolworths.financial.services.android.ui.wfs.common.click

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.debugInspectorInfo
import androidx.compose.ui.semantics.Role

// This is an internal interface called MultipleEventsCutter.
// It defines a method processEvent that takes a lambda as a parameter.
// This interface also has a companion object.
internal interface MultipleEventsCutter {
    fun processEvent(event: () -> Unit)
    companion object // This companion object is used to create instances of MultipleEventsCutter.
}

// This function is an extension function for the companion object of MultipleEventsCutter.
// It returns an instance of MultipleEventsCutterImpl.
internal fun MultipleEventsCutter.Companion.get(): MultipleEventsCutter =
    MultipleEventsCutterImpl()

// This is a private class called MultipleEventsCutterImpl that implements the MultipleEventsCutter interface.
private class MultipleEventsCutterImpl : MultipleEventsCutter {
    // This private property holds the current time in milliseconds.
    private val now: Long
        get() = System.currentTimeMillis()

    // This private variable keeps track of the time of the last event.
    private var lastEventTimeMs: Long = 0

    // This method processes an event by invoking the provided lambda if a certain time threshold has passed.
    override fun processEvent(event: () -> Unit) {
        if (now - lastEventTimeMs >= 300L) {
            event.invoke()
        }
        lastEventTimeMs = now
    }
}

// This is an extension function for the Modifier class.
// It adds a clickable modifier with some custom properties.
fun Modifier.clickableSingle(
    enabled: Boolean = true,
    onClickLabel: String? = null,
    role: Role? = null,
    onClick: () -> Unit
) = composed(
    inspectorInfo = debugInspectorInfo {
        name = "clickable"
        properties["enabled"] = enabled
        properties["onClickLabel"] = onClickLabel
        properties["role"] = role
        properties["onClick"] = onClick
    }
) {
    // This line retrieves an instance of MultipleEventsCutter using the remember function.
    val multipleEventsCutter = remember { MultipleEventsCutter.get() }

    // This line adds a clickable modifier with the provided properties.
    Modifier.clickable(
        enabled = enabled,
        onClickLabel = onClickLabel,
        onClick = { multipleEventsCutter.processEvent { onClick() } },
        role = role,
        indication = LocalIndication.current,
        interactionSource = remember { MutableInteractionSource() }
    )
}
