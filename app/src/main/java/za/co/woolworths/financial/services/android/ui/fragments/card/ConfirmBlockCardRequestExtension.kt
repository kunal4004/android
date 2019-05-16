package za.co.woolworths.financial.services.android.ui.fragments.card

import android.os.AsyncTask
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.rest.npc.PostBlockMyCard
import za.co.woolworths.financial.services.android.util.OnEventListener

abstract class ConfirmBlockCardRequestExtension : MyCardExtension() {

    var mPostBlockMyCard: AsyncTask<String, String, BlockMyCardResponse>? = null
    abstract fun progressBarVisibility(visible: Boolean)
    abstract fun onSuccess(blockMyCardResponse: BlockMyCardResponse?)

    fun blockMyCardRequest(blockMyCardRequest: BlockCardRequestBody, productOfferingId: String?) {
        progressBarVisibility(true)
        val postBlockMyCard = PostBlockMyCard(blockMyCardRequest,productOfferingId,object : OnEventListener<BlockMyCardResponse> {
            override fun onSuccess(blockMyCardResponse: BlockMyCardResponse?) {
                progressBarVisibility(false)
                onSuccess(blockMyCardResponse)
            }

            override fun onFailure(e: String?) {
                activity?.apply {
                    runOnUiThread {
                        progressBarVisibility(false)
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