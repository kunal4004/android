package za.co.woolworths.financial.services.android.util.drag

/**
 * Enables to listen drag events.
 */
interface DragListener {

    /**
     * Invoked when the view has just started to be dragged.
     */
    fun onStartDraggingView()

    /**
     * Invoked when the view is being dragged.
     * By default it's called only when the user is dragging the view.
     * If you also want to be notified when the view is dragged programatically use
     * `DragToClose.setAlwaysNotifyOnDragging(true)`.
     *
     * @param dragOffset vertical drag offset between 0 (start) and 1 (end).
     */
    fun onDragging(dragOffset: Float)

    /**
     * Invoked when the view has being dragged out of the screen
     * and just before calling activity.finish().
     */
    fun onViewCosed()
}