package za.co.woolworths.financial.services.android.ui.wfs.theme.dimens

class FloatDimensions(
    val zero: Float = 0f,
    val one: Float = 1f,
    val my_offer_margin_end_guideline: Float = 0.36f,
    val my_offer_card_width: Float =  0.85f,
    val native_swipe_up_rounded_corner : Float= 3.5f
    )

val smallFloatDimensions by lazy { FloatDimensions() }

val sw320FloatDimensions by lazy {
    FloatDimensions(
        my_offer_margin_end_guideline = 0.30f,
        my_offer_card_width = 0.9f
    )
}
val sw360FloatDimensions by lazy {
    FloatDimensions(
        my_offer_card_width = 0.875f
    )
}
val sw400FloatDimensions by lazy { FloatDimensions() }

val sw440FloatDimensions by lazy {
    FloatDimensions(my_offer_card_width = 0.845f)
}
val sw420FloatDimensions by lazy {
    FloatDimensions(my_offer_card_width = 0.845f)
}

val sw480FloatDimensions by lazy {
    FloatDimensions(
        my_offer_margin_end_guideline = 0.4f,
        my_offer_card_width = 0.84f)
}

val sw520FloatDimensions by lazy { FloatDimensions() }

val sw560FloatDimensions by lazy { FloatDimensions() }

val sw600FloatDimensions by lazy { FloatDimensions() }

