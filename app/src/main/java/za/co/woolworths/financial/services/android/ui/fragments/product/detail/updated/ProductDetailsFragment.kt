package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_details_fragment.*
import kotlinx.android.synthetic.main.product_details_size_and_color_layout.*
import kotlinx.android.synthetic.main.product_details_price_layout.*
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.Response
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ProductUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.ArrayList

class ProductDetailsFragment : Fragment(), ProductDetailsContract.ProductDetailsView, ProductViewPagerAdapter.MultipleImageInterface {


    var productDetails: ProductDetails? = null
    private var subCategoryTitle: String? = null
    private var mFetchFromJson: Boolean = false
    private var defaultProductResponse: String? = null
    private var auxiliaryImages: MutableList<String> = ArrayList()
    private var productViewPagerAdapter: ProductViewPagerAdapter? = null

    companion object {
        fun newInstance() = ProductDetailsFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productDetails = Utils.jsonStringToObject(getString("strProductList"), ProductDetails::class.java) as ProductDetails
            subCategoryTitle = getString("strProductCategory")
            defaultProductResponse = getString("productResponse")
            mFetchFromJson = getBoolean("fetchFromJson")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSizeAndColor()
        configureDefaultUI()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.product_details_fragment, container, false)
    }

    private fun configureDefaultUI() {

        productDetails?.let {
            productName.text = it.productName
            //ProductUtils.displayPrice(textPrice, textActualPrice, it.fromPrice.toString(), it.fromPrice.toString())
            auxiliaryImages.add(activity?.let { it1 -> getImageByWidth(it.externalImageRef, it1) }.toString())
        }

        productViewPagerAdapter = ProductViewPagerAdapter(activity, auxiliaryImages, this).apply {
            productImagesViewPager?.let { pager ->
                pager.adapter = this
                productImagesViewPagerIndicator.setViewPager(pager)
            }
        }

    }

    override fun showProgressBar() {
    }

    override fun hideProgressBar() {
    }

    override fun onSessionTokenExpired() {
    }

    override fun onProductDetailsSuccess(productDetails: ProductDetails) {
    }

    override fun onProductDetailedFailed(response: Response) {
    }

    override fun onFailureResponse(error: String) {
    }

    override fun onStockAvailabilitySuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse) {

    }

    override fun getImageByWidth(imageUrl: String, context: Context): String {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).apply {
            val deviceHeight = this.defaultDisplay
            val size = Point()
            deviceHeight.getSize(size)
            val width = size.x
            var imageLink = imageUrl
            if (imageLink.isEmpty()) imageLink = "https://images.woolworthsstatic.co.za/"
            return imageLink + "" + if (imageLink.contains("jpg")) "" else "?w=$width&q=85"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun SelectedImage(otherSkus: String?) {

    }

    private fun loadSizeAndColor() {
        val spanCount = Utils.calculateNoOfColumns(activity, 50F)
        colorSelectorRecycleView.layoutManager = GridLayoutManager(activity, spanCount)
        /* val layoutManager = FlexboxLayoutManager(activity)
         layoutManager.flexDirection = FlexDirection.ROW
         layoutManager.justifyContent = JustifyContent.FLEX_START
         colorSelectorRecycleView.layoutManager = layoutManager*/
        colorSelectorRecycleView.adapter = ProductColorSelectorAdapter()

        /*val layoutManager1 = FlexboxLayoutManager(activity)
        layoutManager1.flexDirection = FlexDirection.ROW
        layoutManager1.justifyContent = JustifyContent.FLEX_START*/
        sizeSelectorRecycleView.layoutManager = GridLayoutManager(activity, 4)
        sizeSelectorRecycleView.adapter = ProductSizeSelectorAdapter()
    }

}