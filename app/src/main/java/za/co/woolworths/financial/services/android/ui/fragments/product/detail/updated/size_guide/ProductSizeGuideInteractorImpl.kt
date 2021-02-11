package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide

import retrofit2.Call
import za.co.woolworths.financial.services.android.contracts.IGenericAPILoaderView
import za.co.woolworths.financial.services.android.models.dto.size_guide.SizeGuideResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.extension.request

class ProductSizeGuideInteractorImpl : ProductSizeGuideContract.ProductSizeGuideInteractor {
    override fun querySizeGuideContent(sizeGuideId: String, requestListener: IGenericAPILoaderView<Any>): Call<SizeGuideResponse>? {
        return request(OneAppService.getSizeGuideContent(sizeGuideId), requestListener)
    }
}