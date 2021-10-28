package za.co.woolworths.financial.services.android.firebase.model

data class InactiveConfiguration(
    val description: String,
    val firstButton: FirstButton,
    val secondButton: SecondButton,
    val imageUrl: String,
    val title: String
)