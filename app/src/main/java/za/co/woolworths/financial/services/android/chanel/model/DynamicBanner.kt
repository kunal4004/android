package za.co.woolworths.financial.services.android.chanel.model

data class DynamicBanner(
    val externalImageRefV2: String,
    val headerText: String,
    val label: String,
    val name: String,
    val navigation: List<Navigation>,
    val navigationState: String,
    val products: List<Product>
)