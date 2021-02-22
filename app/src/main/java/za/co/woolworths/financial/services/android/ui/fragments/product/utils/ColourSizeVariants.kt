package za.co.woolworths.financial.services.android.ui.fragments.product.utils

enum class ColourSizeVariants(val value: String) {
    NO_VARIANT("noVariant"), COLOUR_VARIANT("colourVariant"), SIZE_VARIANT("sizeVariant"), NO_COLOUR_SIZE_VARIANT("noColourSizeVariant"), COLOUR_SIZE_VARIANT("colourSizeVariant"), DEFAULT("");

    companion object {
        fun find(value: String): ColourSizeVariants? = values().find { it.value.equals(value, true) }
    }
}