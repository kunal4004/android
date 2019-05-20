package za.co.woolworths.financial.services.android.contracts

interface IProgressAnimationState {
    fun onAnimationEnd(cardIsBlocked: Boolean)
}