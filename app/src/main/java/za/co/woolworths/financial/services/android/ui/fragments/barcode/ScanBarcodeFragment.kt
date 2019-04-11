package za.co.woolworths.financial.services.android.ui.fragments.barcode

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.scan_barcode_fragment.*
import za.co.woolworths.financial.services.android.models.dto.ProductDetailResponse
import za.co.woolworths.financial.services.android.models.dto.ProductView
import za.co.woolworths.financial.services.android.models.dto.ProductsRequestParams
import za.co.woolworths.financial.services.android.models.rest.product.GetProductDetail
import za.co.woolworths.financial.services.android.models.rest.product.GetProductsRequest
import za.co.woolworths.financial.services.android.models.rest.product.ProductRequest
import za.co.woolworths.financial.services.android.ui.activities.card.BarcodeScannerActivity
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.OnEventListener
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.barcode.*

class ScanBarcodeFragment : Fragment() {

    private var mCodeScanner: CodeScanner? = null

    companion object {
        fun newInstance() = ScanBarcodeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? BarcodeScannerActivity)?.configureToolbar(R.string.scan_product, R.drawable.close_white)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.scan_barcode_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        codeScannerView?.let { configureScanner(it) }
        startPreview()
        initEvent()
    }

    private fun initEvent() {
        btnManualBarcode?.setOnClickListener {
            replaceFragment(
                    fragment = ManualBarcodeScanFragment.newInstance(),
                    tag = ManualBarcodeScanFragment::class.java.simpleName,
                    containerViewId = R.id.flScanBarcodeContainer,
                    allowStateLoss = true,
                    enterAnimation = R.anim.slide_in_from_right,
                    exitAnimation = R.anim.slide_to_left,
                    popEnterAnimation = R.anim.slide_from_left,
                    popExitAnimation = R.anim.slide_to_right
            )
        }
    }

    override fun onPause() {
        super.onPause()
        pausePreview()
    }

    private fun configureScanner(it: CodeScannerView): FragmentActivity? = activity?.apply {
        mCodeScanner = CodeScanner.builder()
                .formats(CodeScanner.ONE_DIMENSIONAL_FORMATS)
                .autoFocusMode(AutoFocusMode.SAFE)
                .autoFocusInterval(2000L)
                .flash(false)
                .onDecoded { searchTerm ->
                    runOnUiThread {
                        searchTerm.text?.apply { getBarcodeProduct(activity, ProductsRequestParams(this, ProductsRequestParams.SearchType.BARCODE, ProductsRequestParams.ResponseType.DETAIL, 0)) }
                    }
                }
                .onError {

                }.build(this, it)
    }

    private fun startPreview() = mCodeScanner?.startPreview()

    private fun pausePreview() = mCodeScanner?.releaseResources()

    private fun showProgress(visible: Boolean) {
        pbRetrieveProduct?.apply {
            indeterminateDrawable?.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            visibility = if (visible) VISIBLE else GONE
        }
    }

    fun getBarcodeProduct(context: Context?, requestParams: ProductsRequestParams): GetProductsRequest {
        return GetProductsRequest(context!!, requestParams, object : OnEventListener<ProductView> {
            override fun onSuccess(product: ProductView?) {
                product?.products?.get(0)?.let { ProductRequest(it.productId, it.sku) }?.also { getProductDetail(it) }
            }

            override fun onFailure(e: String?) {
            }


        })
    }

    fun getProductDetail(productRequest: ProductRequest): GetProductDetail {
        return GetProductDetail(productRequest, object : OnEventListener<ProductDetailResponse> {
            override fun onSuccess(response: ProductDetailResponse?) {
                response?.apply {
                    when (httpCode) {
                        200 -> {
                            product?.apply {
                                if (productId != null) {
                                    val bundle = Bundle().apply {
                                        putString("strProductList", Utils.objectToJson(this))
                                        putString("strProductCategory", productName)
                                        putString("productResponse", Utils.objectToJson(this))
                                        putBoolean("fetchFromJson", true)
                                    }
                                    activity?.let { ScreenManager.presentProductDetails(it, bundle) }
                                }
                            }
                        }
                        else -> {
                        }
                    }
                }
            }

            override fun onFailure(e: String?) {
                activity?.let {
                    it.runOnUiThread {


                    }
                }
            }
        })
    }
}