package za.co.woolworths.financial.services.android.ui.activities.product.dynamicyield.response.request

import java.io.Serializable

class DyKeywordSearchRequestEvent(
    var session: Session,
    var context: Context,
    var user: User,
    var events: List<Events>
) : Serializable