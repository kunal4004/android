package za.co.woolworths.financial.services.android.util.drag

import android.view.View
import androidx.customview.widget.ViewDragHelper
import za.co.woolworths.financial.services.android.util.drag.DragToClose.Companion.HEIGHT_THRESHOLD_TO_CLOSE
import za.co.woolworths.financial.services.android.util.drag.DragToClose.Companion.SPEED_THRESHOLD_TO_CLOSE

internal class DragHelperCallback(
    private val dragToClose: DragToClose,
    private val draggableContainer: View
) : ViewDragHelper.Callback() {

    private var lastDraggingState: Int = ViewDragHelper.STATE_IDLE
    private var topBorderDraggableContainer: Int = 0

    /**
     * Checks dragging states and notifies them.
     */
    override fun onViewDragStateChanged(state: Int) {
        // If no state change, don't do anything
        if (state == lastDraggingState) return
        // If last state was dragging or settling and current state is idle,
        // the view has stopped moving. If the top border of the container is
        // equal to the vertical draggable range, the view has being dragged out,
        // so close activity is called
        if ((lastDraggingState == ViewDragHelper.STATE_DRAGGING
                    || lastDraggingState == ViewDragHelper.STATE_SETTLING)
            && state == ViewDragHelper.STATE_IDLE
            && topBorderDraggableContainer >= dragToClose.getDraggableRange()) {
            dragToClose.closeActivity()
        }
        // If the view has just started being dragged, notify event
        if (state == ViewDragHelper.STATE_DRAGGING) {
            dragToClose.onStartDraggingView()
        }
        // Save current state
        lastDraggingState = state
    }

    /**
     * Registers draggable container position and changes the transparency of the container
     * based on the vertical position while the view is being vertical dragged.
     */
    override fun onViewPositionChanged(changedView: View, left: Int, top: Int, dx: Int, dy: Int) {
        topBorderDraggableContainer = top
        dragToClose.onViewPositionChanged()
    }

    /**
     * Handles the settling of the draggable view when it is released.
     * Dragging speed is more important than the place the view is released.
     * If the speed is greater than SPEED_THRESHOLD_TO_CLOSE the view is settled to closed.
     * Else if the top
     */
    override fun onViewReleased(releasedChild: View, xVel: Float, yVel: Float) {
        // If view is in its original position or out of range, don't do anything
        if (topBorderDraggableContainer == 0 || topBorderDraggableContainer >= dragToClose.getDraggableRange()) {
            return
        }
        var settleToClosed = false
        // Check speed
        if (yVel > SPEED_THRESHOLD_TO_CLOSE) {
            settleToClosed = true
        } else {
            // Check position
            val verticalDraggableThreshold = (dragToClose.getDraggableRange() * HEIGHT_THRESHOLD_TO_CLOSE).toInt()
            if (topBorderDraggableContainer > verticalDraggableThreshold) {
                settleToClosed = true
            }
        }
        // If settle to closed -> moved view out of the screen
        val settleDestY = if (settleToClosed) dragToClose.getDraggableRange() else 0
        dragToClose.smoothScrollToY(settleDestY)
    }

    /**
     * Sets the vertical draggable range.
     */
    override fun getViewVerticalDragRange(child: View): Int = dragToClose.getDraggableRange()

    /**
     * Configures which is going to be the draggable container.
     */
    override fun tryCaptureView(child: View, pointerId: Int): Boolean = child == draggableContainer

    /**
     * Defines clamped position for left border.
     * DragToClose padding must be taken into consideration.
     */
    override fun clampViewPositionHorizontal(child: View, left: Int, dx: Int): Int = child.left

    /**
     * Defines clamped position for top border.
     */
    override fun clampViewPositionVertical(child: View, top: Int, dy: Int): Int {
        val topBound = dragToClose.paddingTop // Top limit
        val bottomBound = dragToClose.getDraggableRange() // Bottom limit
        return Math.min(Math.max(top, topBound), bottomBound)
    }
}