package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pdp_rating_layout.*
import kotlinx.android.synthetic.main.product_details_add_to_cart_and_find_in_store_button_layout.*
import kotlinx.android.synthetic.main.product_details_delivery_location_layout.*
import kotlinx.android.synthetic.main.product_details_fragment.*
import kotlinx.android.synthetic.main.product_details_gift_with_purchase.*
import kotlinx.android.synthetic.main.product_details_options_and_information_layout.*
import kotlinx.android.synthetic.main.product_details_options_and_information_layout.ratingBar
import kotlinx.android.synthetic.main.product_details_price_layout.*
import kotlinx.android.synthetic.main.product_details_size_and_color_layout.*
import kotlinx.android.synthetic.main.product_listing_page_row.view.*
import kotlinx.android.synthetic.main.promotional_image.view.*
import kotlinx.android.synthetic.main.ratings_ratingdetails.*
import kotlinx.android.synthetic.main.review_row_layout.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.dto.quick_shop.QuickShopDefaultValues
import za.co.woolworths.financial.services.android.ui.activities.*
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductDetailsActivity.Companion.TAG
import za.co.woolworths.financial.services.android.ui.activities.product.ProductInformationActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter.MultipleImageInterface
import za.co.woolworths.financial.services.android.ui.adapters.holder.RecyclerViewViewHolderItems
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.deviceWidth
import za.co.woolworths.financial.services.android.ui.extension.underline
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.DeliveryOrClickAndCollectSelectorDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.OutOfStockMessageDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.ProductNotAvailableForCollectionDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseProductUtils
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ColourSizeVariants
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.ui.views.actionsheet.QuantitySelectorFragment
import za.co.woolworths.financial.services.android.util.*
import java.util.*
import kotlin.collections.ArrayList
import android.widget.LinearLayout
import com.facebook.FacebookSdk.getApplicationContext
import kotlinx.android.synthetic.main.review_helpful_and_report_layout.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.ReviewerInfoDetailsActivity
import za.co.woolworths.financial.services.android.ui.adapters.ReviewThumbnailAdapter


class ProductDetailsFragment : Fragment(), ProductDetailsContract.ProductDetailsView,
    MultipleImageInterface, IOnConfirmDeliveryLocationActionListener, PermissionResultCallback,
    ILocationProvider, View.OnClickListener, OutOfStockMessageDialogFragment.IOutOfStockMessageDialogDismissListener,
    DeliveryOrClickAndCollectSelectorDialogFragment.IDeliveryOptionSelection,
    ProductNotAvailableForCollectionDialog.IProductNotAvailableForCollectionDialogListener,
    ReviewThumbnailAdapter.ThumbnailClickListener {

    private var productDetails: ProductDetails? = null
    private var subCategoryTitle: String? = null
    private var mFetchFromJson: Boolean = false
    private var defaultProductResponse: String? = null
    private var auxiliaryImages: MutableList<String> = ArrayList()
    private var productViewPagerAdapter: ProductViewPagerAdapter? = null
    private var productDetailsPresenter: ProductDetailsContract.ProductDetailsPresenter? = null
    private var storeIdForInventory: String? = ""
    private var otherSKUsByGroupKey: HashMap<String, ArrayList<OtherSkus>> = hashMapOf()
    private var hasColor: Boolean = false
    private var hasSize: Boolean = false
    private var defaultSku: OtherSkus? = null
    private var selectedSku: OtherSkus? = null
    private var selectedGroupKey: String? = null
    private var productSizeSelectorAdapter: ProductSizeSelectorAdapter? = null
    private var productColorSelectorAdapter: ProductColorSelectorAdapter? = null
    private var selectedQuantity: Int? = 1
    private val SSO_REQUEST_ADD_TO_CART = 1010
    private val REQUEST_SUBURB_CHANGE = 153
    private val REQUEST_SUBURB_CHANGE_FOR_STOCK = 155
    private val REQUEST_SUBURB_CHANGE_FOR_LIQUOR = 156
    private val SSO_REQUEST_ADD_TO_SHOPPING_LIST = 1011
    private val SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK = 1012
    private var permissionUtils: PermissionUtils? = null
    private var mFuseLocationAPISingleton: FuseLocationAPISingleton? = null
    private var isApiCallInProgress: Boolean = false
    private var defaultGroupKey: String? = null
    private var mFreeGiftPromotionalImage: String? = null
    private var EDIT_LOCATION_LOGIN_REQUEST = 2020
    private var HTTP_EXPECTATION_FAILED_417: String = "417"
    private var isOutOfStock_502 = false
    private var isOutOfStockFragmentAdded = false
    private var liquorDialog: Dialog? = null
    private var LOGIN_REQUEST_SUBURB_CHANGE = 1419
    private lateinit var  reviewThumbnailAdapter: ReviewThumbnailAdapter
    private var thumbnailList = mutableListOf<Thumbnail>()

    companion object {
        const val INDEX_STORE_FINDER = 1
        const val INDEX_ADD_TO_CART = 2
        const val INDEX_ADD_TO_SHOPPING_LIST = 3
        const val INDEX_SEARCH_FROM_LIST = 4
        const val RESULT_FROM_ADD_TO_CART_PRODUCT_DETAIL = 4002
        const val HTTP_CODE_502 = 502
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
        productDetailsPresenter = ProductDetailsPresenterImpl(this, ProductDetailsInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFuseLocationAPISingleton = FuseLocationAPISingleton
        initViews()
        setUniqueIds()
    }

    private fun initViews() {
        addToCartAction.setOnClickListener(this)
        quantitySelector.setOnClickListener(this)
        addToShoppingList.setOnClickListener(this)
        checkInStoreAvailability.setOnClickListener(this)
        editDeliveryLocation.setOnClickListener(this)
        findInStoreAction.setOnClickListener(this)
        productDetailsInformation.setOnClickListener(this)
        productIngredientsInformation.setOnClickListener(this)
        nutritionalInformation.setOnClickListener(this)
        dietaryInformation.setOnClickListener(this)
        allergensInformation.setOnClickListener(this)
        moreColor.setOnClickListener(this)
        tvRatingDetails.setOnClickListener(this)
        closePage.setOnClickListener {
            activity?.apply {
                setResult(RESULT_CANCELED)
                onBackPressed()
            }
        }
        share?.setOnClickListener(this)
        sizeGuide?.setOnClickListener(this)
        isOutOfStockFragmentAdded = false
        configureDefaultUI()
    }

    override fun onClick(v: View?) {
        KotlinUtils.avoidDoubleClicks(v)
        if (isApiCallInProgress)
            return
        when (v?.id) {
            R.id.addToCartAction -> addItemToCart()
            R.id.quantitySelector -> onQuantitySelector()
            R.id.addToShoppingList -> addItemToShoppingList()
            R.id.checkInStoreAvailability, R.id.findInStoreAction -> findItemInStore()
            R.id.editDeliveryLocation -> updateDeliveryLocation()
            R.id.productDetailsInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.DETAILS)
            R.id.productIngredientsInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.INGREDIENTS)
            R.id.nutritionalInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.NUTRITIONAL_INFO)
            R.id.allergensInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.ALLERGEN_INFO)
            R.id.dietaryInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.DIETARY_INFO)
            R.id.moreColor -> showMoreColors()
            R.id.share -> shareProduct()
            R.id.sizeGuide -> showDetailsInformation(ProductInformationActivity.ProductInformationType.SIZE_GUIDE)
            R.id.tvRatingDetails -> showRatingDetailsDailog()
        }
    }

    fun showRatingDetailsDailog() {
        val dialog = RatingDetailDialog()
        activity?.apply {
            this@ProductDetailsFragment.childFragmentManager.beginTransaction()
                .let { fragmentTransaction ->
                    dialog.show(
                        fragmentTransaction,
                        RatingDetailDialog::class.java.simpleName
                    )
                }
        }
    }


    private fun onQuantitySelector() {
        activity?.supportFragmentManager?.apply {
            if (getSelectedSku() == null) {
                requestSelectSize()
                return
            }
            getSelectedSku()?.quantity?.let {
                if (it > 0) {
                    QuantitySelectorFragment.newInstance(it, this@ProductDetailsFragment)
                        .show(this, QuantitySelectorFragment::class.java.simpleName)
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.product_details_fragment, container, false)
    }

    private fun configureDefaultUI() {

        updateStockAvailabilityLocation()

        productDetails?.let {
            productName?.text = it.productName
            brandName?.apply {
                if (!it.brandText.isNullOrEmpty()) {
                    text = it.brandText
                    visibility = View.VISIBLE
                }
            }

            BaseProductUtils.displayPrice(fromPricePlaceHolder, textPrice, textActualPrice, it.price, it.wasPrice, it.priceType, it.kilogramPrice)
            auxiliaryImages.add(activity?.let { it1 -> getImageByWidth(it.externalImageRef, it1) }.toString())
            updateAuxiliaryImages(auxiliaryImages)
        }

        mFreeGiftPromotionalImage = productDetails?.promotionImages?.freeGift

        loadPromotionalImages()

        if (mFetchFromJson) {
            val productDetails = Utils.stringToJson(activity, defaultProductResponse)!!.product
            this.onProductDetailsSuccess(productDetails)
        } else {
            //loadProductDetails.
            productDetailsPresenter?.loadProductDetails(ProductRequest(productDetails?.productId, productDetails?.sku))
        }
    }

    fun addItemToCart() {

        if (getSelectedSku() == null) {
            if (getSelectedGroupKey().isNullOrEmpty())
                requestSelectColor()
            else
                requestSelectSize()
            return
        }

        if (!Utils.isDeliverySelectionModalShown()) {
            showDeliveryOptionDialog()
            return
        }

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity, SSO_REQUEST_ADD_TO_CART)
            return
        }

        val deliveryLocation = Utils.getPreferredDeliveryLocation()
        if (deliveryLocation == null) {
            productDetailsPresenter?.loadCartSummary()
            return
        }

        if (!Utils.retrieveStoreId(productDetails?.fulfillmentType).equals(storeIdForInventory, ignoreCase = true)) {
            updateStockAvailability(false)
            return
        }

        if (TextUtils.isEmpty(Utils.retrieveStoreId(productDetails?.fulfillmentType)) || getSelectedSku()?.quantity == 0) {
            //setSelectedSku(null)
            hideProgressBar()
            var message = ""
            var title = ""
            when (TextUtils.isEmpty(Utils.retrieveStoreId(productDetails?.fulfillmentType))) {
                true -> {
                    title = getString(R.string.product_unavailable)
                    message = "Unfortunately this item is unavailable in " + if (deliveryLocation.storePickup) deliveryLocation.store?.name else deliveryLocation.suburb?.name + ". Try changing your delivery location and try again."
                }
                else -> {
                    title = getString(R.string.out_of_stock)
                    message = "Unfortunately this item is out of stock in " + if (deliveryLocation.storePickup) deliveryLocation.store?.name else deliveryLocation.suburb?.name + ". Try changing your delivery location and try again."
                }
            }
            activity?.apply {
                Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, title, message)
            }
            updateAddToCartButtonForSelectedSKU()
            return
        }
        //finally add to cart after all checks
        getSelectedSku()?.apply {
            addToCartForSelectedSKU()
        }

    }

    private fun addToCartForSelectedSKU() {
        val item = getSelectedQuantity()?.let {
            AddItemToCart(productDetails?.productId, getSelectedSku()?.sku, if (it > getSelectedSku()?.quantity!!) getSelectedSku()?.quantity!! else it)
        }
        val listOfItems = ArrayList<AddItemToCart>()
        item?.let { listOfItems.add(it) }
        if (listOfItems.isNotEmpty()) {
            activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PDP_ADD_TO_CART, this) }
            productDetailsPresenter?.postAddItemToCart(listOfItems)
        }
    }

    override fun onSessionTokenExpired() {
        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
        activity?.let { activity -> activity.runOnUiThread { ScreenManager.presentSSOSignin(activity) } }
        updateStockAvailabilityLocation()
    }

    override fun onProductDetailsSuccess(productDetails: ProductDetails) {
        if (!isAdded) return

        this.productDetails = productDetails
        otherSKUsByGroupKey = this.productDetails?.otherSkus?.let { groupOtherSKUsByColor(it) }!!
        this.defaultSku = getDefaultSku(otherSKUsByGroupKey)

        if(productDetails?.isLiquor == true && !KotlinUtils.isCurrentSuburbDeliversLiquor() && !KotlinUtils.isLiquorModalShown()){
            KotlinUtils.setLiquorModalShown()
            showLiquorDialog()
        }

        if ((!hasColor && !hasSize)) {
            setSelectedSku(this.defaultSku)
            updateAddToCartButtonForSelectedSKU()
        }

        if (hasSize)
            setSelectedGroupKey(defaultGroupKey)

        Utils.getPreferredDeliveryLocation()?.let {
            updateDefaultUI(false)
            if (!this.productDetails?.productType.equals(getString(R.string.food_product_type), ignoreCase = true) && it.storePickup) {
                showProductUnavailable()
                showProductNotAvailableForCollection()
                return
            }
        }

        if (!this.productDetails?.otherSkus.isNullOrEmpty()) {
            storeIdForInventory = RecyclerViewViewHolderItems.getFulFillmentStoreId(productDetails.fulfillmentType)

            when (storeIdForInventory.isNullOrEmpty()) {
                true -> showProductUnavailable()
                false -> {
                    showProductDetailsLoading()
                    val multiSKUs = productDetails.otherSkus.joinToString(separator = "-") { it.sku }
                    productDetailsPresenter?.loadStockAvailability(storeIdForInventory!!, multiSKUs, true)
                }
            }

        } else if (productDetails.otherSkus.isNullOrEmpty()) {
            showProductOutOfStock()
        } else {
            showErrorWhileLoadingProductDetails()
        }
    }

    override fun onProductDetailedFailed(response: Response, httpCode: Int) {
        if (httpCode == HTTP_CODE_502) {
            isOutOfStock_502 = true
            val message = getString(R.string.out_of_stock_502)
            OutOfStockMessageDialogFragment.newInstance(message).show(this@ProductDetailsFragment.childFragmentManager, OutOfStockMessageDialogFragment::class.java.simpleName)
        } else if (isAdded) {
            isOutOfStock_502 = false
            showErrorWhileLoadingProductDetails()
        }
    }

    override fun onFailureResponse(error: String) {
    }

    override fun onStockAvailabilitySuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse, isDefaultRequest: Boolean) {

        productDetails?.otherSkus?.forEach { otherSku ->
            otherSku?.quantity = 0
            skusInventoryForStoreResponse.skuInventory.forEach { skuInventory ->
                if (otherSku.sku.equals(skuInventory.sku, ignoreCase = true)) {
                    otherSku.quantity = skuInventory.quantity
                    return@forEach
                }
            }
        }
        if (isDefaultRequest) {
            clearSelectedOnLocationChange()
            updateDefaultUI(true)
            hideProductDetailsLoading()
        } else {
            hideProgressBar()
            getSelectedSku()?.let { selectedSku ->
                productDetails?.otherSkus?.forEach {
                    if (it.sku.equals(selectedSku.sku, ignoreCase = true)) {
                        selectedSku.quantity = it.quantity
                        return@forEach
                    }
                }
            }
            addItemToCart()
        }
    }

    override fun getImageByWidth(imageUrl: String?, context: Context): String {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).apply {
            var imageLink = imageUrl
            val deviceHeight = this.defaultDisplay
            val size = Point()
            deviceHeight.getSize(size)
            val width = size.x
            if (imageLink.isNullOrEmpty()) imageLink = KotlinUtils.productImageUrlPrefix
            return imageLink + "" + if (imageLink.contains("jpg")) "" else "?w=$width&q=85"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        productDetailsPresenter?.onDestroy()
    }

    override fun SelectedImage(image: String?) {
        activity?.apply {
            val openMultipleImage = Intent(this, MultipleImageActivity::class.java)
            openMultipleImage.putExtra("auxiliaryImages", image)
            startActivity(openMultipleImage)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun loadSizeAndColor() {
        if (hasColor)
            showColors()
        if (hasSize)
            showSize()

        if (productDetailsPresenter?.isSizeGuideApplicable(productDetails?.colourSizeVariants, productDetails?.sizeGuideId) == true) {
            sizeGuide?.apply {
                underline()
                visibility = View.VISIBLE
            }
        }

    }

    private fun showColors() {
        val spanCount = Utils.calculateNoOfColumns(activity, 50F)
        colorSelectorRecycleView.layoutManager = GridLayoutManager(activity, spanCount)
        if (otherSKUsByGroupKey.size == 1 && !hasSize) {
            onColorSelection(this.defaultGroupKey)
        }
        productColorSelectorAdapter = ProductColorSelectorAdapter(otherSKUsByGroupKey, this, spanCount, getSelectedGroupKey()).apply {
            colorSelectorRecycleView.adapter = this
            showSelectedColor()
        }

        otherSKUsByGroupKey.size.let {
            if (it > spanCount) {
                moreColor.text = "+ " + (it - spanCount) + " More"
                moreColor.visibility = View.VISIBLE
            }
        }

        colorSelectorLayout.visibility = View.VISIBLE
    }

    private fun showSize() {
        sizeSelectorRecycleView.layoutManager = GridLayoutManager(activity, 4)
        productSizeSelectorAdapter = ProductSizeSelectorAdapter(otherSKUsByGroupKey[getSelectedGroupKey()]!!, this).apply {
            sizeSelectorRecycleView.adapter = this
        }

        otherSKUsByGroupKey[getSelectedGroupKey()]?.let {
            if (it.size == 1) {
                productSizeSelectorAdapter?.setSelection(it[0])
                onSizeSelection(it[0])
            }
        }

        sizeSelectorLayout.visibility = View.VISIBLE
    }

    private fun groupOtherSKUsByColor(otherSKUsList: ArrayList<OtherSkus>): HashMap<String, ArrayList<OtherSkus>> {

        val variant = ColourSizeVariants.find(productDetails?.colourSizeVariants ?: "")
        when (variant) {
            ColourSizeVariants.DEFAULT, ColourSizeVariants.NO_VARIANT -> {
                hasColor = false
                hasSize = false
            }
            ColourSizeVariants.COLOUR_VARIANT -> {
                hasColor = true
                hasSize = false
            }
            ColourSizeVariants.SIZE_VARIANT, ColourSizeVariants.COLOUR_SIZE_VARIANT -> {
                hasColor = true
                hasSize = true
            }
            ColourSizeVariants.NO_COLOUR_SIZE_VARIANT -> {
                hasColor = false
                hasSize = true
            }
        }

        for (otherSkuObj in otherSKUsList) {
            var groupKey = ""
            groupKey = if (TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                otherSkuObj.size.trim()
            } else if (!TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                otherSkuObj.colour.trim()
            } else {
                otherSkuObj.colour.trim()
            }

            if (variant == ColourSizeVariants.NO_COLOUR_SIZE_VARIANT) {
                otherSkuObj.apply { size = colour }
                groupKey = "N/A"
            }

            if (!otherSKUsByGroupKey.containsKey(groupKey)) {
                this.otherSKUsByGroupKey[groupKey] = ArrayList<OtherSkus>()
            }
            if (!otherSKUsByGroupKey[groupKey]!!.any { it.sku == otherSkuObj.sku }) this.otherSKUsByGroupKey[groupKey]!!.add(otherSkuObj)
        }
        return otherSKUsByGroupKey
    }

    override fun updateDefaultUI(isInventoryCalled: Boolean) {
        loadSizeAndColor()
        loadPromotionalImages()
        updateAuxiliaryImages(getAuxiliaryImagesByGroupKey())
        if (!TextUtils.isEmpty(this.productDetails?.ingredients))
            productIngredientsInformation.visibility = View.VISIBLE
        if (this.productDetails?.nutritionalInformationDetails != null)
            nutritionalInformation.visibility = View.VISIBLE
        if (!this.productDetails?.dietary.isNullOrEmpty())
            dietaryInformation.visibility = View.VISIBLE
        if (!this.productDetails?.allergens.isNullOrEmpty())
            allergensInformation.visibility = View.VISIBLE


        productDetails?.let {
            BaseProductUtils.displayPrice(fromPricePlaceHolder, textPrice, textActualPrice, it.price, it.wasPrice, it.priceType, it.kilogramPrice)
            brandName.apply {
                if (!it.brandText.isNullOrEmpty()) {
                    text = it.brandText
                    visibility = View.VISIBLE
                }
            }

            if (it.isRnREnabled) {
                ratingBarTop.rating = it.averageRating
                if(it.reviewCount<2)
                    tvTotalReviews.text = getString(R.string.no_review, it.reviewCount)
                else
                    tvTotalReviews.text = getString(R.string.no_reviews, it.reviewCount)
                ratingBarTop.visibility = View.VISIBLE
                tvTotalReviews.visibility = View.VISIBLE
                tvTotalReviews.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
                ratingBar.rating = it.averageRating
                if(it.reviewCount<2)
                    tvCustomerReviewCount.text = getString(R.string.customer_review, "("+it.reviewCount+")")
                else
                    tvCustomerReviewCount.text = getString(R.string.customer_reviews, "("+it.reviewCount+")")
                tvRecommend.text = getString(R.string.percent_recommend_to_friend,"96%")
                setReviewUI()
            }else{
                headerCustomerReview.visibility = View.GONE
                reviewDetailsInformation.visibility = View.GONE
            }

            if (!it.freeGiftText.isNullOrEmpty()) {
                freeGiftText.text = it.freeGiftText
                freeGiftWithPurchaseLayout.visibility = View.VISIBLE
            }
            if (productDetails?.promotionsList?.isEmpty() == false) {
                productDetails?.promotionsList?.forEachIndexed { i, it ->
                    var editedPromotionalText: String? = it.promotionalText
                    if (it.promotionalText?.contains(":") == true) {
                        val beforeColon: String? = it.promotionalText?.substringBefore(":")
                        val afterColon: String? = it.promotionalText?.substringAfter(":")
                        editedPromotionalText = "<b>" + beforeColon + ":" + "</b>" + afterColon
                    }
                    when (i) {
                        0 -> {
                            onlinePromotionalTextView1?.visibility = View.VISIBLE
                            onlinePromotionalTextView1?.text = Html.fromHtml(editedPromotionalText)
                        }
                        1 -> {
                            onlinePromotionalTextView2?.visibility = View.VISIBLE
                            onlinePromotionalTextView2?.text = Html.fromHtml(editedPromotionalText)
                        }
                        2 -> {
                            onlinePromotionalTextView3?.visibility = View.VISIBLE
                            onlinePromotionalTextView3?.text = Html.fromHtml(editedPromotionalText)
                        }
                    }
                }
            } else {
                onlinePromotionalTextView1?.text = ""
                onlinePromotionalTextView2?.text = ""
                onlinePromotionalTextView3?.text = ""
                onlinePromotionalTextView1?.visibility = View.GONE
                onlinePromotionalTextView2?.visibility = View.GONE
                onlinePromotionalTextView3?.visibility = View.GONE
            }
        }

        if (isAllProductsOutOfStock() && isInventoryCalled) {
            showProductOutOfStock()
            return
        }
    }

    private fun setReviewUI(){
        tvReport.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
        tvSkinProfile.setPaintFlags(Paint.UNDERLINE_TEXT_FLAG)
        for (i in 1..1){
            val rootView = LinearLayout(context)
            rootView.layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rootView.orientation = LinearLayout.HORIZONTAL

            val tv1 = TextView(context)
            tv1.alpha = 0.5F
            val tv2 = TextView(context)
            tv2.alpha = 0.5F
            val ivCircle = ImageView(context)
            /*val img = requireContext().resources.getDrawable(R.drawable.ic_circle)
            img.setBounds(20,0,0,0)
            tv2.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_circle,0,0,0)*/

            val tvParam: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tvParam.setMargins(25, 0, 0, 8)
            tv2.layoutParams = tvParam
            val ivParam: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            ivParam.setMargins(25,15,0,0)
            ivCircle.layoutParams = ivParam
            if (Build.VERSION.SDK_INT < 23) {
                tv1.setTextAppearance(getApplicationContext(), R.style.myriad_pro_regular_black_15_text_style);
                tv2.setTextAppearance(getApplicationContext(), R.style.myriad_pro_semi_bold_black_15_text_style);
            } else{
                tv1.setTextAppearance(R.style.myriad_pro_regular_black_15_text_style);
                tv2.setTextAppearance(R.style.myriad_pro_semi_bold_black_15_text_style);
            }
            tv1.text = "Size ordered"
            ivCircle.setImageResource(R.drawable.ic_circle)
            tv2.text = "Medium"

            rootView.addView(tv1)
            rootView.addView(ivCircle)
            rootView.addView(tv2)
            llAdditionalFields.addView(rootView)
            tvCustomerReview.setOnClickListener {
                val intent = Intent(context, ReviewerInfoDetailsActivity::class.java)
                startActivity(intent)
            }
        }
        setSecondaryRatingsUI()
    }

    private fun setSecondaryRatingsUI(){
        for (i in 1..2 step 2){
            val rootView = LinearLayout(context)
            rootView.layoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            rootView.orientation = LinearLayout.HORIZONTAL

            val tv1 = TextView(context)
            val tv2 = TextView(context)

            val tvParam: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tvParam.setMargins(15, 0, 0, 8)
            tv2.layoutParams = tvParam
            if (Build.VERSION.SDK_INT < 23) {
                tv1.setTextAppearance(getApplicationContext(), R.style.myriad_pro_regular_black_15_text_style);
                tv2.setTextAppearance(getApplicationContext(), R.style.myriad_pro_semi_bold_black_15_text_style);
            } else{
                tv1.setTextAppearance(R.style.myriad_pro_regular_black_15_text_style);
                tv2.setTextAppearance(R.style.myriad_pro_semi_bold_black_15_text_style);
            }
            tv1.text = "Product Quality:"
            tv2.text = "4/5"

            rootView.addView(tv1)
            rootView.addView(tv2)

            val tv3 = TextView(context)
            val tv4 = TextView(context)

            val tvParam2: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tvParam2.setMargins(65, 0, 0, 8)
            tv3.layoutParams = tvParam2
            val tvParam3: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tvParam3.setMargins(15, 0, 0, 8)
            tv4.layoutParams = tvParam3
            if (Build.VERSION.SDK_INT < 23) {
                tv3.setTextAppearance(getApplicationContext(), R.style.myriad_pro_regular_black_15_text_style);
                tv4.setTextAppearance(getApplicationContext(), R.style.myriad_pro_semi_bold_black_15_text_style);
            } else{
                tv3.setTextAppearance(R.style.myriad_pro_regular_black_15_text_style);
                tv4.setTextAppearance(R.style.myriad_pro_semi_bold_black_15_text_style);
            }
            tv3.text = "Fit:"
            tv4.text = "Runs Large"

            rootView.addView(tv3)
            rootView.addView(tv4)
            llSecondaryRatings.addView(rootView)
        }
        setReviewThumbnailUI()
    }

    private fun setReviewThumbnailUI(){
        rvThumbnail.layoutManager = GridLayoutManager(getApplicationContext(),3)
        reviewThumbnailAdapter = ReviewThumbnailAdapter(getApplicationContext(),this)
        rvThumbnail.adapter = reviewThumbnailAdapter

        thumbnailList.clear()
        thumbnailList.add(Thumbnail(1,R.drawable.header_image))
        thumbnailList.add(Thumbnail(2,R.drawable.header_image))
        thumbnailList.add(Thumbnail(3,R.drawable.header_image))
        reviewThumbnailAdapter.setDataList(thumbnailList)
    }

    override fun thumbnailClicked() {
        thumbnailList.clear()
        thumbnailList.add(Thumbnail(1,R.drawable.header_image))
        thumbnailList.add(Thumbnail(2,R.drawable.header_image))
        thumbnailList.add(Thumbnail(3,R.drawable.header_image))
        thumbnailList.add(Thumbnail(4,R.drawable.header_image))
        thumbnailList.add(Thumbnail(5,R.drawable.header_image))
        reviewThumbnailAdapter.setDataList(thumbnailList)
    }

    private fun setBrandText(it: ProductDetails) {
        brandName.apply {
            if (!it.brandText.isNullOrEmpty()) {
                text = it.brandText
                visibility = View.VISIBLE
            }
        }
    }

    private fun getDefaultSku(otherSKUsList: HashMap<String, ArrayList<OtherSkus>>): OtherSkus? {
        otherSKUsList.keys.forEach { key ->
            otherSKUsList[key]?.forEach { otherSku ->
                if (otherSku.sku.equals(this.productDetails?.sku, ignoreCase = true)) {
                    defaultGroupKey = key
                    return otherSku
                }
            }
        }

        return null

    }

    override fun updateAuxiliaryImages(imagesList: List<String>) {
        activity?.apply {
            productViewPagerAdapter = ProductViewPagerAdapter(activity, imagesList, this@ProductDetailsFragment).apply {
                productImagesViewPager?.let { pager ->
                    pager.adapter = this
                    productImagesViewPagerIndicator.setViewPager(pager)
                }
            }
        }
    }

    override fun onSizeSelection(selectedSku: OtherSkus) {
        setSelectedSku(selectedSku)
        showSelectedSize()
        updateUIForSelectedSKU(getSelectedSku())
    }

    override fun onColorSelection(selectedColor: String?) {
        setSelectedGroupKey(selectedColor)
        showSelectedColor()
        if (hasSize) updateSizesOnColorSelection() else {
            setSelectedSku(otherSKUsByGroupKey[getSelectedGroupKey()]?.get(0))
            updateUIForSelectedSKU(getSelectedSku())
        }
        updateAuxiliaryImages(getAuxiliaryImagesByGroupKey())
    }

    private fun updateSizesOnColorSelection() {
        productSizeSelectorAdapter?.updatedSizes(otherSKUsByGroupKey[getSelectedGroupKey()]!!)

        //===== positive flow
        // if selected size available for the selected color
        // get the sku for the selected size from the new color group
        // update the selectedSizeSKU

        //===== negative flow
        // if selected size not available on the new color group
        // make selectedSKU to null

        getSelectedSku()?.let { selected ->
            var index = -1
            otherSKUsByGroupKey[getSelectedGroupKey()]?.forEachIndexed { i, it ->
                if (it.size.equals(selected.size, true)) {
                    index = i
                    return@forEachIndexed
                }
            }
            when (index) {
                -1 -> {
                    var otherSku: OtherSkus? = null
                    otherSKUsByGroupKey[getSelectedGroupKey()]?.forEach {
                        if (it.quantity > 0) {
                            otherSku = it
                            return@forEach
                        }
                    }
                    setSelectedSku(otherSku)
                    if (getSelectedSku() == null) productSizeSelectorAdapter?.clearSelection() else productSizeSelectorAdapter?.setSelection(
                        getSelectedSku()
                    )
                    if (getSelectedSku() == null) defaultSku =
                        otherSKUsByGroupKey[getSelectedGroupKey()]?.get(0)
                    if (getSelectedSku() == null) updateUIForSelectedSKU(defaultSku) else updateUIForSelectedSKU(
                        getSelectedSku()
                    )
                }
                else -> {
                    setSelectedSku(otherSKUsByGroupKey[getSelectedGroupKey()]?.get(index))
                    productSizeSelectorAdapter?.setSelection(getSelectedSku())
                    updateUIForSelectedSKU(getSelectedSku())
                }
            }
            showSelectedSize()

        }

    }

    private fun updateAddToCartButtonForSelectedSKU() {

        when (getSelectedSku()) {
            null -> showAddToCart()
            else -> {
                getSelectedSku()?.quantity?.let {
                    when (it) {
                        0, -1 -> showFindInStore()
                        else -> {
                            getSelectedQuantity()?.apply {
                                if (it < this)
                                    onQuantitySelection(1)
                            }
                            showAddToCart()
                        }
                    }
                }
            }
        }

    }

    private fun showFindInStore() {
        productDetails?.isnAvailable?.toBoolean()?.apply {
            if (!this) {
                toCartAndFindInStoreLayout?.visibility = View.GONE
                checkInStoreAvailability?.visibility = View.GONE
                return
            }
        }


        toCartAndFindInStoreLayout?.visibility = View.VISIBLE
        groupAddToCartAction?.visibility = View.GONE
        findInStoreAction?.visibility = View.VISIBLE
    }

    private fun showAddToCart() {
        toCartAndFindInStoreLayout?.visibility = View.VISIBLE
        groupAddToCartAction?.visibility = View.VISIBLE
        findInStoreAction?.visibility = View.GONE
        if (isAllProductsOutOfStock()) {
            showFindInStore()
        }
    }

    private fun updateUIForSelectedSKU(otherSku: OtherSkus?) {
        otherSku?.let {
            BaseProductUtils.displayPrice(
                fromPricePlaceHolder,
                textPrice,
                textActualPrice,
                it.price,
                it.wasPrice,
                "",
                it.kilogramPrice
            )
        }
        updateAddToCartButtonForSelectedSKU()
    }

    override fun setSelectedSku(selectedSku: OtherSkus?) {
        this.selectedSku = selectedSku
    }

    override fun getSelectedSku(): OtherSkus? {
        return this.selectedSku
    }

    private fun setSelectedGroupKey(selectedGroupKey: String?) {
        this.selectedGroupKey = selectedGroupKey
    }

    private fun getSelectedGroupKey(): String? {
        return this.selectedGroupKey
    }

    override fun onQuantitySelection(quantity: Int) {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PDP_SELECT_QUANTITY, this) }
        setSelectedQuantity(quantity)
        quantityText.text = quantity.toString()
    }

    override fun setSelectedQuantity(selectedQuantity: Int?) {
        this.selectedQuantity = selectedQuantity
    }

    override fun getSelectedQuantity(): Int? {
        return this.selectedQuantity
    }

    private fun getAuxiliaryImagesByGroupKey(): List<String> {

        if (getSelectedGroupKey().isNullOrEmpty() && defaultGroupKey.isNullOrEmpty())
            return auxiliaryImages

        val auxiliaryImagesForGroupKey = ArrayList<String>()
        val groupKey = getSelectedGroupKey() ?: defaultGroupKey

        otherSKUsByGroupKey[groupKey]?.get(0)?.externalImageRef?.let {
            if (productDetails?.otherSkus?.size!! > 0)
                auxiliaryImagesForGroupKey.add(it)
        }

        val allAuxImages = Gson().fromJson<Map<String, AuxiliaryImage>>(
            this.productDetails?.auxiliaryImages,
            object : TypeToken<Map<String, AuxiliaryImage>>() {}.type
        )

        getImageCodeForAuxiliaryImages(groupKey).forEach { imageCode ->
            allAuxImages.entries.forEach { entry ->
                if (entry.key.contains(imageCode, true))
                    auxiliaryImagesForGroupKey.add(entry.value.externalImageRef)
            }
        }

        return if (auxiliaryImagesForGroupKey.isNotEmpty()) auxiliaryImagesForGroupKey else auxiliaryImages
    }

    private fun getImageCodeForAuxiliaryImages(groupKey: String?): ArrayList<String> {
        var imageCode = ""
        val imageCodesList = arrayListOf<String>()
        groupKey?.split("\\s".toRegex())?.let {
            when (it.size) {
                1 -> imageCodesList.add(it[0])
                else -> {
                    it.forEachIndexed { i, s ->
                        imageCode = if (i == 0) s[0].toString() else imageCode.plus(s)
                    }
                    imageCodesList.add(imageCode)
                    imageCodesList.add(it.joinToString(""))
                }
            }
        }

        return imageCodesList
    }

    override fun onCartSummarySuccess(cartSummaryResponse: CartSummaryResponse) {

        if (Utils.isCartSummarySuburbIDEmpty(cartSummaryResponse)) {
            activity?.apply {
                KotlinUtils.presentEditDeliveryLocationActivity(this, REQUEST_SUBURB_CHANGE)
            }
        } else confirmDeliveryLocation()
    }

    override fun responseFailureHandler(response: Response) {
        if (response.code.equals(HTTP_EXPECTATION_FAILED_417)) {
            confirmDeliveryLocation()
            return
        }
        activity?.apply {
            Utils.displayValidationMessage(
                this,
                CustomPopUpWindow.MODAL_LAYOUT.ERROR,
                response.desc
            )
        }
    }

    private fun confirmDeliveryLocation() {
        this.childFragmentManager.apply {
            ConfirmDeliveryLocationFragment.newInstance()?.let {
                it.isCancelable = false
                it.show(this, ConfirmDeliveryLocationFragment::class.java.simpleName)
            }
        }
    }

    override fun onConfirmLocation() {
        //continue add to cart request
        addItemToCart()
    }

    override fun onSetNewLocation() {
        activity?.apply {
            KotlinUtils.presentEditDeliveryLocationActivity(
                this,
                REQUEST_SUBURB_CHANGE
            )
        }
    }

    private fun updateStockAvailability(isDefaultRequest: Boolean) {
        storeIdForInventory = Utils.retrieveStoreId(productDetails?.fulfillmentType)
        when (storeIdForInventory.isNullOrEmpty()) {
            true -> showProductUnavailable()
            false -> {
                productDetails?.apply {
                    otherSkus?.let {
                        val multiSKUs = it.joinToString(separator = "-") { it.sku }
                        productDetailsPresenter?.loadStockAvailability(
                            storeIdForInventory!!,
                            multiSKUs,
                            isDefaultRequest
                        )
                    }
                }
            }
        }

    }

    override fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse) {
        activity?.apply {
            addItemToCartResponse.data?.let {
                if (it.size > 0) {
                    val intent: Intent = Intent()
                    intent.apply {
                        putExtra("addedToCartMessage", it[0].message)
                        putExtra("ItemsCount", getSelectedQuantity())
                        putExtra("ProductCountMap", Utils.toJson(it[0].productCountMap))
                    }
                    setResult(RESULT_OK, intent)
                    onBackPressed()
                }
            }
        }
    }

    private fun addItemToShoppingList() {
        activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOPADDTOLIST, this) }

        if (getSelectedSku() == null) {
            if (getSelectedGroupKey().isNullOrEmpty())
                requestSelectColor()
            else
                requestSelectSize()
            return
        }

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity, SSO_REQUEST_ADD_TO_SHOPPING_LIST)
        } else if (getSelectedSku() != null) {
            activity?.apply {
                val item = AddToListRequest()
                getSelectedSku()?.let {
                    item.apply {
                        quantity = "1"
                        catalogRefId = it.sku
                        giftListId = it.sku
                        skuID = it.sku
                    }
                }
                val listOfItems = ArrayList<AddToListRequest>()
                item.let {
                    listOfItems.add(it)
                }
                scrollView?.fullScroll(View.FOCUS_UP)
                NavigateToShoppingList.openShoppingList(activity, listOfItems, "", false)
            }

        } else {
            // Select size to contine
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (resultCode) {
            RESULT_OK -> {
                when (requestCode) {
                    REQUEST_SUBURB_CHANGE -> {
                        updateStockAvailabilityLocation()
                        addItemToCart()
                    }
                    ADD_TO_SHOPPING_LIST_REQUEST_CODE -> {
                        /*int listSize = data.getIntExtra("sizeOfList", 0);
                        boolean isSessionExpired = data.getBooleanExtra("sessionExpired", false);
                        if (isSessionExpired) {
                            onSessionTokenExpired();
                            return;
                        }
                        showToastMessage(getActivity(), listSize);*/
                    }
                    SET_DELIVERY_LOCATION_REQUEST_CODE -> {
                        activity?.apply {
                            KotlinUtils.presentEditDeliveryLocationActivity(
                                this,
                                REQUEST_SUBURB_CHANGE
                            )
                        }
                    }
                    FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS -> {
                        findItemInStore()
                    }
                    REQUEST_SUBURB_CHANGE_FOR_STOCK -> {

                        updateStockAvailabilityLocation()

                        Utils.getPreferredDeliveryLocation()?.let {
                            if (!this.productDetails?.productType.equals(
                                    getString(R.string.food_product_type),
                                    ignoreCase = true
                                ) && it.storePickup
                            ) {
                                storeIdForInventory = ""
                                clearStockAvailability()
                                showProductUnavailable()
                                showProductNotAvailableForCollection()
                                reloadFragment()
                                return
                            }
                        }

                        if (Utils.retrieveStoreId(productDetails?.fulfillmentType)
                                .isNullOrEmpty()
                        ) {
                            storeIdForInventory = ""
                            clearStockAvailability()
                            showProductUnavailable()
                            reloadFragment()
                            return
                        }

                        if (!Utils.retrieveStoreId(productDetails?.fulfillmentType)
                                .equals(storeIdForInventory, ignoreCase = true)
                        ) {
                            updateStockAvailability(true)
                            reloadFragment()
                        }
                    }

                    REQUEST_SUBURB_CHANGE_FOR_LIQUOR -> {

                        updateStockAvailabilityLocation()

                        Utils.getPreferredDeliveryLocation()?.let {
                            if (!this.productDetails?.productType.equals(
                                    getString(R.string.food_product_type),
                                    ignoreCase = true
                                ) && it.storePickup
                            ) {
                                storeIdForInventory = ""
                                clearStockAvailability()
                                showProductUnavailable()
                                showProductNotAvailableForCollection()
                                reloadFragment()
                                return
                            }
                        }

                        if (Utils.retrieveStoreId(productDetails?.fulfillmentType)
                                .isNullOrEmpty()
                        ) {
                            storeIdForInventory = ""
                            clearStockAvailability()
                            showProductUnavailable()
                            reloadFragment()
                            return
                        }

                        if (!Utils.retrieveStoreId(productDetails?.fulfillmentType)
                                .equals(storeIdForInventory, ignoreCase = true)
                        ) {
                            updateStockAvailability(true)
                            reloadFragment()
                        }
                    }

                }
            }
            SSOActivity.SSOActivityResult.SUCCESS.rawValue() -> {
                updateStockAvailabilityLocation()
                when (requestCode) {
                    SSO_REQUEST_ADD_TO_CART, EDIT_LOCATION_LOGIN_REQUEST -> {
                        addItemToCart()
                    }
                    SSO_REQUEST_ADD_TO_SHOPPING_LIST -> {
                        addItemToShoppingList()
                        //One time biometricsWalkthrough
                        activity?.apply { ScreenManager.presentBiometricWalkthrough(this) }
                    }
                    SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK -> {
                        activity?.apply {
                            KotlinUtils.presentEditDeliveryLocationActivity(
                                this,
                                REQUEST_SUBURB_CHANGE_FOR_STOCK
                            )
                        }
                    }
                    LOGIN_REQUEST_SUBURB_CHANGE -> {
                        activity?.apply {
                            KotlinUtils.presentEditDeliveryLocationActivity(
                                this,
                                REQUEST_SUBURB_CHANGE_FOR_LIQUOR,
                                DeliveryType.DELIVERY_LIQUOR
                            )
                        }
                    }
                }
            }
            RESULT_CANCELED -> {
                when (requestCode) {
                    SET_DELIVERY_LOCATION_REQUEST_CODE -> {
                        //dismissFindInStoreProgress()
                    }
                }
            }
        }
    }

    private fun findItemInStore() {

        if (getSelectedSku() == null) {
            if (getSelectedGroupKey().isNullOrEmpty())
                requestSelectColor()
            else
                requestSelectSize()
            return
        }

        activity?.apply {
            when (Utils.isLocationEnabled(this)) {
                true -> {
                    if (!checkRunTimePermissionForLocation()) {
                        return
                    }
                }
                else -> {
                    Utils.displayValidationMessage(
                        this,
                        CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF,
                        ""
                    )
                    return
                }
            }
        }
        getSelectedSku()?.let {
            startLocationUpdates()
        }

    }


    private fun checkRunTimePermissionForLocation(): Boolean {
        permissionUtils = PermissionUtils(activity, this)
        permissionUtils?.apply {
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            return checkAndRequestPermissions(permissions, 1)
        }
        return false
    }

    override fun PermissionGranted(request_code: Int) {
        findItemInStore()
    }

    override fun PartialPermissionGranted(
        request_code: Int,
        granted_permissions: ArrayList<String>?
    ) {
    }

    override fun PermissionDenied(request_code: Int) {
    }

    override fun NeverAskAgain(request_code: Int) {
    }

    override fun onLocationChange(location: Location?) {
        activity?.apply {
            Utils.saveLastLocation(location, this)
            stopLocationUpdate()
            getSelectedSku()?.apply {
                productDetailsPresenter?.findStoresForSelectedSku(this)
                return
            }
        }

    }

    override fun onPopUpLocationDialogMethod() {
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startLocationUpdates() {
        activity?.apply {
            showProgressBar()
            mFuseLocationAPISingleton?.apply {
                addLocationChangeListener(this@ProductDetailsFragment)
                startLocationUpdate()
            }
        }
    }

    fun stopLocationUpdate() {
        // stop location updates
        mFuseLocationAPISingleton?.apply {
            stopLocationUpdate()
        }

    }

    private fun requestSelectSize() {
        activity?.apply {
            resources.displayMetrics?.let {
                val mid: Int = it.heightPixels / 2 - selectedSizePlaceholder.height
                ObjectAnimator.ofInt(scrollView, "scrollY", mid).setDuration(500).start()
            }
            selectedSizePlaceholder?.let {
                it.setTextColor(Color.RED)
                it.postDelayed({
                    it.setTextColor(ContextCompat.getColor(this, R.color.black))
                }, 5000)
            }
        }
    }

    private fun requestSelectColor() {
        activity?.apply {
            resources.displayMetrics?.let {
                val mid: Int = it.heightPixels / 2 - colorPlaceholder.height
                ObjectAnimator.ofInt(scrollView, "scrollY", mid).setDuration(500).start()
            }
            colorPlaceholder?.let {
                it.setTextColor(Color.RED)
                it.postDelayed({
                    it.setTextColor(ContextCompat.getColor(this, R.color.black))
                }, 5000)
            }
        }
    }

    override fun onFindStoresSuccess(location: List<StoreDetails>) {
        activity?.apply {
            WoolworthsApplication.getInstance().wGlobalState.storeDetailsArrayList = location
            val intentInStoreFinder = Intent(activity, WStockFinderActivity::class.java)
            intentInStoreFinder.putExtra("PRODUCT_NAME", subCategoryTitle)
            startActivity(intentInStoreFinder)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    override fun showOutOfStockInStores() {
        activity?.apply {
            Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.NO_STOCK, "")
        }
    }

    override fun showProductDetailsLoading() {
        activity?.apply {
            showProgressBar()
            viewsToHideOnProductLoading.visibility = View.GONE
            toCartAndFindInStoreLayout.visibility = View.GONE
        }
    }

    override fun hideProductDetailsLoading() {
        activity?.apply {
            hideProgressBar()
            viewsToHideOnProductLoading.visibility = View.VISIBLE
            updateAddToCartButtonForSelectedSKU()
        }
    }

    override fun showProgressBar() {
        activity?.apply {
            isApiCallInProgress = true
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun hideProgressBar() {
        activity?.apply {
            isApiCallInProgress = false
            progressBar.visibility = View.GONE
        }
    }

    private fun showErrorWhileLoadingProductDetails() {
        activity?.apply {
            showProductUnavailable()
            Utils.displayValidationMessage(
                activity,
                CustomPopUpWindow.MODAL_LAYOUT.CLI_ERROR,
                getString(R.string.statement_send_email_false_desc)
            )
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSelectedColor() {
        activity?.apply {
            getSelectedGroupKey()?.let {
                colorPlaceholder.text = getString(R.string.selected_colour)
                colorPlaceholder.setTextColor(ContextCompat.getColor(this, R.color.black))
                selectedColor.text = " - $it"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSelectedSize() {
        getSelectedSku().let {
            selectedSizePlaceholder.text =
                getString(if (it != null) R.string.product_placeholder_selected_size else R.string.product_placeholder_select_size)
            selectedSize.text = if (it != null) " - ${it.size}" else ""
            activity?.apply {
                if (it != null)
                    selectedSizePlaceholder.setTextColor(
                        ContextCompat.getColor(
                            this,
                            R.color.black
                        )
                    )
            }
        }
    }

    override fun updateDeliveryLocation() {
        activity?.apply {
            when (SessionUtilities.getInstance().isUserAuthenticated) {
                true -> KotlinUtils.presentEditDeliveryLocationActivity(
                    this,
                    REQUEST_SUBURB_CHANGE_FOR_STOCK
                )
                false -> ScreenManager.presentSSOSignin(this, SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK)
            }

        }
    }

    override fun updateStockAvailabilityLocation() {
        activity?.apply {
            getDeliveryLocation()?.let {
                when (it) {
                    is ShoppingDeliveryLocation -> {
                        when (it.storePickup) {
                            true -> {
                                currentDeliveryLocation.text =
                                    resources?.getString(R.string.store) + it.store?.name
                                defaultLocationPlaceholder.text =
                                    getString(R.string.collecting_from) + " "
                            }
                            else -> {
                                currentDeliveryLocation.text =
                                    it.suburb?.name + "," + it.province?.name
                                defaultLocationPlaceholder.text =
                                    getString(R.string.delivering_to_pdp)
                            }
                        }
                    }
                    is QuickShopDefaultValues -> {
                        currentDeliveryLocation.text = it.suburb.name
                        defaultLocationPlaceholder.text = getString(R.string.set_to_default)
                    }
                }
            }
        }
    }

    override fun showDetailsInformation(productInformationType: ProductInformationActivity.ProductInformationType) {
        activity?.apply {
            val intent = Intent(this, ProductInformationActivity::class.java)
            intent.putExtra(
                ProductInformationActivity.PRODUCT_DETAILS,
                Utils.toJson(productDetails)
            )
            intent.putExtra(
                ProductInformationActivity.PRODUCT_INFORMATION_TYPE,
                productInformationType
            )
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    private fun showProductUnavailable() {
        clearStockAvailability()
        productDetails?.otherSkus?.get(0)?.let { otherSku -> setSelectedSku(otherSku) }
        getSelectedSku()?.quantity = -1
        hideProductDetailsLoading()
        toCartAndFindInStoreLayout?.visibility = View.GONE
        updateAddToCartButtonForSelectedSKU()
        //hideProgressBar()
    }

    private fun showMoreColors() {
        productColorSelectorAdapter?.apply {
            showMoreColors()
            moreColor.visibility = View.INVISIBLE
        }
    }

    override fun loadPromotionalImages() {
        val images = ArrayList<String>()
        activity?.apply {
            productDetails?.promotionImages?.let {
                if (!it.save.isNullOrEmpty()) images.add(it.save)
                if (!it.wRewards.isNullOrEmpty()) images.add(it.wRewards)
                if (!it.vitality.isNullOrEmpty()) images.add(it.vitality)
                if (!it.newImage.isNullOrEmpty()) images.add(it.newImage)
                if (!it.reduced.isNullOrEmpty()) images.add(it.reduced)

            }

            promotionalImages?.removeAllViews()

            mFreeGiftPromotionalImage?.let { freeGiftImage -> images.add(freeGiftImage) }

            val promoImages = productDetails?.promotionImages
            images.forEach { image ->
                when (image) {
                    promoImages?.reduced -> {
                        val width = deviceWidth() / 5
                        layoutInflater.inflate(R.layout.promotional_image, null)?.let { view ->
                            val promotionImageView =
                                view.findViewById<ImageView>(R.id.promotionImage)
                            promotionImageView?.apply {
                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                layoutParams?.width = width
                                ImageManager.setPictureOverrideWidthHeight(
                                    view.promotionImage,
                                    image
                                )
                                promotionalImages?.addView(view)
                            }
                        }
                    }
                    promoImages?.save -> {
                        val width = deviceWidth() / 10
                        layoutInflater.inflate(R.layout.promotional_image, null)?.let { view ->
                            val promotionImageView =
                                view.findViewById<ImageView>(R.id.promotionImage)
                            promotionImageView?.apply {
                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.FIT_CENTER
                                layoutParams?.width = width
                                ImageManager.setPictureOverrideWidthHeight(
                                    view.promotionImage,
                                    image
                                )
                                promotionalImages?.addView(view)
                            }
                        }
                    }
                    else -> {
                        layoutInflater.inflate(R.layout.promotional_image, null)?.let { view ->
                            ImageManager.loadImage(view.promotionImage, image)
                            promotionalImages?.addView(view)
                        }
                    }

                }

            }
        }

    }

    override fun onResume() {
        super.onResume()
        activity?.apply {
            Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_DETAIL)
        }
    }

    private fun isAllProductsOutOfStock(): Boolean {
        var isAllProductsOutOfStock = true
        productDetails?.otherSkus?.forEach {
            if (it.quantity > 0) {
                isAllProductsOutOfStock = false
                return@forEach
            }
        }
        return isAllProductsOutOfStock
    }

    private fun showProductOutOfStock() {
        if (!isOutOfStockFragmentAdded) {
            isOutOfStockFragmentAdded = true
            activity?.apply {
                getDeliveryLocation().let {
                    val suburbName = when (it) {
                        is ShoppingDeliveryLocation -> if (it.storePickup) it.store?.name else it.suburb?.name
                        is QuickShopDefaultValues -> it.suburb.name
                        else -> ""
                    }
                    val message =
                        bindString(R.string.product_details_out_of_stock, suburbName ?: "")
                    OutOfStockMessageDialogFragment.newInstance(message).show(
                        this@ProductDetailsFragment.childFragmentManager,
                        OutOfStockMessageDialogFragment::class.java.simpleName
                    )
                    updateAddToCartButtonForSelectedSKU()
                }
            }
        }
    }

    private fun getDeliveryLocation(): Any? {
        val userLocation = Utils.getPreferredDeliveryLocation()
        val defaultLocation = WoolworthsApplication.getQuickShopDefaultValues()
        return if (userLocation != null && SessionUtilities.getInstance().isUserAuthenticated) userLocation else defaultLocation
    }

    override fun onOutOfStockDialogDismiss() {
        if (isOutOfStock_502) {
            isOutOfStock_502 = false
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else
                activity?.finish()
        } else if (productDetails?.otherSkus.isNullOrEmpty())
            activity?.onBackPressed()
    }

    override fun setUniqueIds() {
        resources?.apply {
            productLayout?.contentDescription = getString(R.string.pdp_layout)
            productImagesViewPagerIndicator?.contentDescription =
                getString(R.string.store_card_image)
            closePage?.contentDescription = getString(R.string.pdp_layout)
            productName?.contentDescription = getString(R.string.pdp_textViewProductName)
            priceLayout?.contentDescription = getString(R.string.pdp_textViewPrice)
            colorPlaceholder?.contentDescription = getString(R.string.pdp_textViewColourPlaceHolder)
            selectedColor?.contentDescription = getString(R.string.pdp_textSelectedColour)
            colorSelectorRecycleView?.contentDescription =
                getString(R.string.pdp_colorSelectorRecycleView)
            addToCartAction?.contentDescription = getString(R.string.pdp_buttonAddToCart)
            quantitySelector?.contentDescription = getString(R.string.pdp_quantitySelector)
            quantityText?.contentDescription = getString(R.string.pdp_quantitySelected)
            sizeColorSelectorLayout?.contentDescription =
                getString(R.string.pdp_sizeColourSelectorLayout)
            sizeSelectorRecycleView?.contentDescription =
                getString(R.string.pdp_sizeSelectorRecycleView)
            selectedSizePlaceholder?.contentDescription =
                getString(R.string.pdp_selectedSizePlaceholder)
            selectedSize?.contentDescription = getString(R.string.pdp_textViewSelectedSize)
            stockAvailabilityPlaceholder?.contentDescription =
                getString(R.string.pdp_stockAvailabilityPlaceholder)
            deliveryLocationLayout?.contentDescription =
                getString(R.string.pdp_deliveryLocationLayout)
            currentDeliveryLocation?.contentDescription =
                getString(R.string.pdp_txtCurrentDeliveryLocation)
            defaultLocationPlaceholder?.contentDescription =
                getString(R.string.pdp_defaultLocationPlaceholder)
            editDeliveryLocation?.contentDescription =
                getString(R.string.pdp_buttoneditDeliveryLocationn)
            productDetailOptionsAndInformation?.contentDescription =
                getString(R.string.pdp_productDetailOptionsAndInformationLayout)
            headerProductOptions?.contentDescription =
                getString(R.string.pdp_headerProductOptionsLayout)
            checkInStoreAvailability?.contentDescription =
                getString(R.string.pdp_checkInStoreAvailabilityLayout)
            buttonView?.contentDescription = getString(R.string.pdp_buttonView)
            addToShoppingList?.contentDescription = getString(R.string.pdp_addToShoppingListLayout)
            headerProductInformation?.contentDescription =
                getString(R.string.pdp_headerProductInformationLayout)
            productDetailsInformation?.contentDescription =
                getString(R.string.pdp_productDetailsInformationLayout)
            nutritionalInformation?.contentDescription =
                getString(R.string.pdp_productIngredientsInformationLayout)
            productIngredientsInformation?.contentDescription =
                getString(R.string.pdp_nutritionalInformationLayout)
        }
    }

    private fun reloadFragment() {
        val currentFragment = activity?.supportFragmentManager?.findFragmentByTag(TAG)
        val fragmentTransaction: FragmentTransaction? =
            activity?.supportFragmentManager?.beginTransaction()
        if (fragmentTransaction != null && currentFragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fragmentTransaction?.detach(currentFragment!!).commitNow()
                fragmentTransaction?.attach(currentFragment!!).commitNow()
            } else
                fragmentTransaction.detach(this).attach(this).commit()
        }
    }

    private fun showDeliveryOptionDialog() {
        (activity as? AppCompatActivity)?.supportFragmentManager?.beginTransaction()
            ?.let { fragmentTransaction ->
                DeliveryOrClickAndCollectSelectorDialogFragment.newInstance(this).show(
                    fragmentTransaction,
                    DeliveryOrClickAndCollectSelectorDialogFragment::class.java.simpleName
                )
            }
    }

    override fun onDeliveryOptionSelected(deliveryType: DeliveryType) {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            activity?.apply {
                KotlinUtils.presentEditDeliveryLocationActivity(
                    this,
                    REQUEST_SUBURB_CHANGE,
                    deliveryType
                )
            }
        } else {
            ScreenManager.presentSSOSignin(activity, EDIT_LOCATION_LOGIN_REQUEST)
        }
    }

    override fun clearSelectedOnLocationChange() {
        if (!(!hasColor && !hasSize)) {
            setSelectedSku(null)
            selectedSize?.text = ""
            selectedColor?.text = ""
        }
    }

    override fun showProductNotAvailableForCollection() {
        activity?.apply {
            if (!ProductNotAvailableForCollectionDialog.dialogInstance.isVisible)
                ProductNotAvailableForCollectionDialog.newInstance().show(
                    this@ProductDetailsFragment.childFragmentManager,
                    ProductNotAvailableForCollectionDialog::class.java.simpleName
                )
        }
    }

    override fun onChangeDeliveryOption() {
        this.updateDeliveryLocation()
    }

    override fun onFindInStore() {
        this.findItemInStore()
    }

    override fun clearStockAvailability() {
        productDetails?.otherSkus?.forEach {
            it.quantity = -1
        }
        loadSizeAndColor()
    }

    override fun shareProduct() {
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHOP_PDP_NATIVE_SHARE, hashMapOf(Pair(FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_ID, productDetails?.productId
                    ?: "")), this)
            val message = WoolworthsApplication.getProductDetailsPage()?.shareItemMessage + " " + productDetails?.productId?.let { WoolworthsApplication.getProductDetailsPage()?.shareItemURITemplate?.replace("{product_id}", it, true) }
            val shareIntent = Intent()
            shareIntent.apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(shareIntent)
        }
    }

    /**
     * Conditions to show liquor popup
     * This should be checked before inventory call
     * 1. isLiquor flag from service response. /wfs/app/v4/searchSortAndFilterV2
     * 2. Current suburb doesn't match up with config suburbs.
     * 3. It is showing for the first time
     */
    fun showLiquorDialog() {

        liquorDialog = activity?.let { activity -> Dialog(activity) }
        liquorDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.liquor_info_dialog, null)
            val desc = view.findViewById<TextView>(R.id.desc)
            val close = view.findViewById<Button>(R.id.close)
            val setSuburb = view.findViewById<TextView>(R.id.setSuburb)
            desc?.text = WoolworthsApplication.getLiquor()?.message ?: ""
            close?.setOnClickListener { dismiss() }
            setSuburb?.setOnClickListener {
                dismiss()
                if (!SessionUtilities.getInstance().isUserAuthenticated) {
                    ScreenManager.presentSSOSignin(activity, LOGIN_REQUEST_SUBURB_CHANGE)
                } else {
                    activity?.apply {
                        KotlinUtils.presentEditDeliveryLocationActivity(
                            this,
                            REQUEST_SUBURB_CHANGE_FOR_LIQUOR,
                            DeliveryType.DELIVERY_LIQUOR
                        )
                    }
                }
            }
            setContentView(view)
            window?.apply {
                setLayout(
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT
                )
                setBackgroundDrawableResource(R.color.transparent)
                setGravity(Gravity.CENTER)
            }

            setTitle(null)
            setCancelable(true)
            show()
        }
    }

}