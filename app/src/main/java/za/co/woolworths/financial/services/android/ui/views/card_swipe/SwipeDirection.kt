package za.co.woolworths.financial.services.android.ui.views.card_swipe

enum class SwipeDirection {
    Left, Right, Top, Bottom;

    companion object {
        val FREEDOM = listOf(*values())
        val FREEDOM_NO_BOTTOM = listOf(Top, Left, Right)
        val HORIZONTAL = listOf(Left, Right)
        val VERTICAL = listOf(Top, Bottom)

        @JvmStatic
        fun from(value: Int): List<SwipeDirection> {
            return when (value) {
                1 -> FREEDOM_NO_BOTTOM
                2 -> HORIZONTAL
                3 -> VERTICAL
                else -> FREEDOM
            }
        }
    }
}