package za.co.woolworths.financial.services.android.dynamicyield.data.response.request

data class DynamicVariantRequestEvent(
    val contextDY: ContextDY,
    val options: Options,
    val session: Session,
    val user: User
)