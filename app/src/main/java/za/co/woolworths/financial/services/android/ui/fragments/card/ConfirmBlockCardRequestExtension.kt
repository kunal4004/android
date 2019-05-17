package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.AsyncTask
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.rest.npc.PostBlockMyCard
import za.co.woolworths.financial.services.android.util.OnEventListener

abstract class ConfirmBlockCardRequestExtension : MyCardExtension() {

    private var mPostBlockMyCard: AsyncTask<String, String, BlockMyCardResponse>? = null
    abstract fun blockCardSuccessResponse(blockMyCardResponse: BlockMyCardResponse?)
    abstract fun blockMyCardFailure()
    fun blockMyCardRequest(blockMyCardRequest: BlockCardRequestBody, productOfferingId: String?) {
        val postBlockMyCard = PostBlockMyCard(blockMyCardRequest, productOfferingId, object : OnEventListener<BlockMyCardResponse> {
            override fun onSuccess(blockMyCardResponse: BlockMyCardResponse?) {
                blockCardSuccessResponse(blockMyCardResponse)
            }

            override fun onFailure(e: String?) {
                activity?.apply {
                    runOnUiThread {
                        blockMyCardFailure()
                    }
                }
            }
        })

        mPostBlockMyCard = postBlockMyCard.execute()
    }


    override fun onDestroy() {
        super.onDestroy()
        mPostBlockMyCard?.apply {
            if (!isCancelled)
                cancel(true)
        }
    }
}