package za.co.woolworths.financial.services.android.ui.views.card_swipe.internal

import android.graphics.Point

class CardStackState {
    var topIndex = 0
    var lastPoint: Point? = null
    var lastCount = 0
    var isPaginationReserved = false
    var isInitialized = false
    fun reset() {
        topIndex = 0
        lastPoint = null
        lastCount = 0
        isPaginationReserved = false
        isInitialized = false
    }
}