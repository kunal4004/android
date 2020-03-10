package za.co.woolworths.financial.services.android.ui.fragments.npc

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.npc.BlockCardRequestBody
import za.co.woolworths.financial.services.android.models.dto.npc.BlockMyCardResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService

abstract class BlockMyCardRequestExtension : MyCardExtension() {

    private var mPostBlockMyCard: Call<BlockMyCardResponse>? = null
    abstract fun blockCardSuccessResponse(blockMyCardResponse: BlockMyCardResponse?)
    abstract fun blockMyCardFailure()
    fun blockMyCardRequest(blockMyCardRequest: BlockCardRequestBody, productOfferingId: String?) {
        productOfferingId?.let {
            mPostBlockMyCard = OneAppService.postBlockMyCard(blockMyCardRequest, it)
            mPostBlockMyCard?.enqueue(CompletionHandler(object : IResponseListener<BlockMyCardResponse> {
                override fun onSuccess(blockMyCardResponse: BlockMyCardResponse?) {
                    blockCardSuccessResponse(blockMyCardResponse)
                }

                override fun onFailure(error: Throwable?) {
                    activity?.apply {
                        runOnUiThread {
                            blockMyCardFailure()
                        }
                    }
                }
            }, BlockMyCardResponse::class.java))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mPostBlockMyCard?.apply {
            if (!isCanceled)
                cancel()
        }
    }
}