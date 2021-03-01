package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse

interface ProductSizeGuideContract {

    interface ProductSizeGuideView {
        fun showProgressBar()
        fun hideProgressBar()
        fun onSizeGuideContentSuccess(sizeGuideHtmlContent: String?)
        fun onSizeGuideContentFailed(errorMessage: String?)
        fun loadSizeGuideView(sizeGuideHtmlContent: String?)
        fun getSizeGuideContent()
    }

    interface ProductSizeGuidePresenter {
        fun onDestroy()
        fun loadSizeGuideContent(sizeGuideId: String)
    }

    interface ProductSizeGuideInteractor {
        fun querySizeGuideContent(sizeGuideId: String, requestListener: IGenericAPILoaderView<Any>): Call<SizeGuideResponse>?
    }
}