package za.co.woolworths.financial.services.android.ui.activities.write_a_review.repository

import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.request.PrepareWriteAReviewFormRequestEvent
import za.co.woolworths.financial.services.android.ui.activities.write_a_review.response.WriteAReviewFormResponse

interface WriteAReviewFormRepository {
    suspend fun getWriteAReviewFormResponse(productId: String?, writeAReviewFormRequest: PrepareWriteAReviewFormRequestEvent?): Resource<WriteAReviewFormResponse>
}