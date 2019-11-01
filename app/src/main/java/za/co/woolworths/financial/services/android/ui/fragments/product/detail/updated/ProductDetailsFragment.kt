package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.product_details_add_to_cart_and_find_in_store_button_layout.*
import kotlinx.android.synthetic.main.product_details_fragment.*
import kotlinx.android.synthetic.main.product_details_price_layout.*
import kotlinx.android.synthetic.main.product_details_size_and_color_layout.*
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewHolderItems
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseProductUtils
import za.co.woolworths.financial.services.android.ui.views.actionsheet.QuantitySelectorFragment
import za.co.woolworths.financial.services.android.util.ScreenManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class ProductDetailsFragment : Fragment(), ProductDetailsContract.ProductDetailsView, ProductViewPagerAdapter.MultipleImageInterface, IOnConfirmDeliveryLocationActionListener {


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
    private var selectedQuantity: Int? = null
    private val SSO_REQUEST_ADD_TO_CART = 1010
    private val REQUEST_SUBURB_CHANGE = 153


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
        productDetailsPresenter = ProductDetailsPresenterImpl(this, ProductDetailsInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        addToCartAction.setOnClickListener { addItemToCart() }
        quantitySelector.setOnClickListener { onQuantitySelector() }
        configureDefaultUI()
    }

    private fun onQuantitySelector() {
        activity?.supportFragmentManager?.apply {
            getSelectedSku()?.quantity?.let {
                if (it > 0)
                    QuantitySelectorFragment.newInstance(it, this@ProductDetailsFragment).show(this, QuantitySelectorFragment::class.java.simpleName)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.product_details_fragment, container, false)
    }

    private fun configureDefaultUI() {

        productDetails?.let {
            productName.text = it.productName
            BaseProductUtils.displayPrice(textPrice, textActualPrice, it.price, it.wasPrice, it.priceType, it.kilogramPrice)
            auxiliaryImages.add(activity?.let { it1 -> getImageByWidth(it.externalImageRef, it1) }.toString())
            updateAuxiliaryImages(auxiliaryImages)
            it.saveText?.apply { setPromotionalText(this) }
        }



        if (mFetchFromJson) {
            val productDetails = Utils.stringToJson(activity, defaultProductResponse)!!.product
            this.onProductDetailsSuccess(productDetails)
        } else {
            //loadProductDetails.
            productDetailsPresenter?.loadProductDetails(ProductRequest(productDetails?.productId, productDetails?.sku))
        }

    }

    fun addItemToCart() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity, SSO_REQUEST_ADD_TO_CART)
            return
        }

        val deliveryLocation = Utils.getPreferredDeliveryLocation()
        if (deliveryLocation == null) {
            productDetailsPresenter?.loadCartSummary()
        }

        val storeId = Utils.retrieveStoreId(productDetails?.fulfillmentType)
        if (!storeId.equals(storeIdForInventory, ignoreCase = true)) {
            updateStockAvailability(storeId)
            return
        }

        if (TextUtils.isEmpty(Utils.retrieveStoreId(productDetails?.fulfillmentType))) {
            //setSelectedSku(null)
            val message = "Unfortunately this item is unavailable in " + deliveryLocation.suburb.name + ". Try changing your delivery location and try again."
            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, getString(R.string.product_unavailable), message)
            return
        }
        //finally add to cart after all checks
        getSelectedSku()?.apply {
            addToCartForSelectedSKU()
        }

    }

    private fun addToCartForSelectedSKU() {
        val item = getSelectedQuantity()?.let { AddItemToCart(productDetails?.productId, getSelectedSku()?.sku, it) }
        val listOfItems = ArrayList<AddItemToCart>()
        item?.let { listOfItems.add(it) }
        if (listOfItems.isNotEmpty())
            productDetailsPresenter?.postAddItemToCart(listOfItems)
    }

    override fun showProgressBar() {
    }

    override fun hideProgressBar() {
    }

    override fun onSessionTokenExpired() {
    }

    override fun onProductDetailsSuccess(productDetails: ProductDetails) {
        this.productDetails = productDetails
        if (!this.productDetails?.otherSkus.isNullOrEmpty()) {

            storeIdForInventory = ProductListingViewHolderItems.getFulFillmentStoreId(productDetails.fulfillmentType)?.apply {
                val multiSKUs = productDetails.otherSkus.joinToString(separator = "-") { it.sku }
                productDetailsPresenter?.loadStockAvailability(this, multiSKUs, true)
            }


        } else {
            // getViewDataBinding().llLoadingColorSize.setVisibility(View.GONE)
            // getViewDataBinding().loadingInfoView.setVisibility(View.GONE)

            if (isAdded)
                Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.CLI_ERROR, getString(R.string.statement_send_email_false_desc))
        }
    }

    override fun onProductDetailedFailed(response: Response) {
    }

    override fun onFailureResponse(error: String) {
    }

    override fun onStockAvailabilitySuccess(skusInventoryForStoreResponse: SkusInventoryForStoreResponse, isDefaultRequest: Boolean) {

        productDetails?.otherSkus?.forEach { otherSku ->
            skusInventoryForStoreResponse.skuInventory.forEach { skuInventory ->
                if (otherSku.sku.equals(skuInventory.sku, ignoreCase = true)) {
                    otherSku.quantity = skuInventory.quantity
                    return@forEach
                }
            }
        }
        if (isDefaultRequest) {
            otherSKUsByGroupKey = productDetails?.otherSkus?.let { groupOtherSKUsByColor(it) }!!
            updateDefaultUI()
        } else {
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

    override fun getImageByWidth(imageUrl: String, context: Context): String {
        (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).apply {
            //val deviceHeight = this.defaultDisplay
            val size = Point()
            //deviceHeight.getSize(size)
            val width = size.x
            var imageLink = imageUrl
            if (imageLink.isEmpty()) imageLink = "https://images.woolworthsstatic.co.za/"
            return imageLink + "" + if (imageLink.contains("jpg")) "" else "?w=$width&q=85"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        productDetailsPresenter?.onDestroy()
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
        productColorSelectorAdapter = ProductColorSelectorAdapter(otherSKUsByGroupKey, this).apply {
            colorSelectorRecycleView.adapter = this
            setSelect(getSelectedGroupKey())
        }
        //productColorSelectorAdapter?
        /*val layoutManager1 = FlexboxLayoutManager(activity)
        layoutManager1.flexDirection = FlexDirection.ROW
        layoutManager1.justifyContent = JustifyContent.FLEX_START*/
        sizeSelectorRecycleView.layoutManager = GridLayoutManager(activity, 4)
        productSizeSelectorAdapter = ProductSizeSelectorAdapter(otherSKUsByGroupKey[getSelectedGroupKey()]!!, this).apply {
            sizeSelectorRecycleView.adapter = this
        }

    }

    private fun groupOtherSKUsByColor(otherSKUsList: ArrayList<OtherSkus>): HashMap<String, ArrayList<OtherSkus>> {
        for (otherSkuObj in otherSKUsList) {
            var groupKey = ""
            if (TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                this.hasSize = !otherSkuObj.size.equals("NO SZ", ignoreCase = true)
                groupKey = otherSkuObj.size.trim()
            } else if (!TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                this.hasColor = !otherSkuObj.colour.equals("N/A", ignoreCase = true)
                this.hasSize = !otherSkuObj.size.equals("NO SZ", ignoreCase = true)
                groupKey = otherSkuObj.colour.trim()
            } else {
                this.hasColor = true
                groupKey = otherSkuObj.colour.trim()
            }

            if (!otherSKUsByGroupKey.containsKey(groupKey)) {
                this.otherSKUsByGroupKey.put(groupKey, ArrayList<OtherSkus>())
            }
            this.otherSKUsByGroupKey.get(groupKey)!!.add(otherSkuObj)
        }
        return otherSKUsByGroupKey
    }

    override fun updateDefaultUI() {
        this.defaultSku = getDefaultSku(otherSKUsByGroupKey)
        if (!hasSize)
            setSelectedSku(this.defaultSku)
        /*getViewDataBinding().llLoadingColorSize.setVisibility(View.GONE);
        getViewDataBinding().loadingInfoView.setVisibility(View.GONE);
        this.configureButtonsAndSelectors();*/
        /*if (hasColor)
            this.setSelectedColorIcon()*/
        loadSizeAndColor()
        configureActionItems()
        productDetails?.let {
            it.saveText?.apply { setPromotionalText(this) }
            BaseProductUtils.displayPrice(textPrice, textActualPrice, it.price, it.wasPrice, it.priceType, it.kilogramPrice)
        }
    }

    private fun configureActionItems() {

    }

    private fun getDefaultSku(otherSKUsList: HashMap<String, ArrayList<OtherSkus>>): OtherSkus? {
        otherSKUsList.keys.forEach { key ->
            otherSKUsList[key]?.forEach { otherSku ->
                if (otherSku.sku.equals(this.productDetails?.sku, ignoreCase = true)) {
                    setSelectedGroupKey(key)
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

    override fun setPromotionalText(promotionValue: String) {
        if (promotionValue.isNotEmpty()) {
            promotionText.apply {
                text = promotionValue
                visibility = View.VISIBLE
            }
        }
    }

    override fun onSizeSelection(selectedSku: OtherSkus) {
        setSelectedSku(selectedSku)
        updateUIForSelectedSKU(getSelectedSku())
    }

    override fun onColorSelection(selectedColor: String?) {
        setSelectedGroupKey(selectedColor)
        if (hasSize) updateSizesOnColorSelection() else setSelectedSku(otherSKUsByGroupKey[getSelectedGroupKey()]?.get(0))
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
                    setSelectedSku(null)
                    productSizeSelectorAdapter?.clearSelection()
                    defaultSku = otherSKUsByGroupKey[getSelectedGroupKey()]?.get(0)
                    updateUIForSelectedSKU(defaultSku)
                }
                else -> {
                    setSelectedSku(otherSKUsByGroupKey[getSelectedGroupKey()]?.get(index))
                    productSizeSelectorAdapter?.setSelection(getSelectedSku())
                    updateUIForSelectedSKU(getSelectedSku())
                }
            }

        }

    }

    private fun updateAddToCartButtonForSelectedSKU() {

        when (getSelectedSku()) {
            null -> showAddToCart()
            else -> {
                getSelectedSku()?.quantity.let {
                    when (it) {
                        0 -> showFindInStore();
                        else -> showAddToCart()
                    }
                }
            }
        }

    }


    private fun showFindInStore() {
        groupAddToCartAction.visibility = View.GONE
        findInStoreAction.visibility = View.VISIBLE
    }

    private fun showAddToCart() {
        groupAddToCartAction.visibility = View.VISIBLE
        findInStoreAction.visibility = View.GONE
    }

    private fun updateUIForSelectedSKU(otherSku: OtherSkus?) {
        otherSku?.let {
            BaseProductUtils.displayPrice(textPrice, textActualPrice, it.price, it.wasPrice, "", it.kilogramPrice)
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
        setSelectedQuantity(quantity)
    }

    override fun setSelectedQuantity(selectedQuantity: Int?) {
        this.selectedQuantity = selectedQuantity
    }

    override fun getSelectedQuantity(): Int? {
        return this.selectedQuantity
    }

    private fun getAuxiliaryImagesByGroupKey(): List<String> {

        if (getSelectedGroupKey().isNullOrEmpty())
            return auxiliaryImages

        val auxiliaryImagesForGroupKey = ArrayList<String>()

        otherSKUsByGroupKey[getSelectedGroupKey()]?.get(0)?.externalImageRef?.let {
            if (productDetails?.otherSkus?.size!! > 0)
                auxiliaryImagesForGroupKey.add(it)
        }

        val allAuxImages = Gson().fromJson<Map<String, AuxiliaryImage>>(this.productDetails?.auxiliaryImages, object : TypeToken<Map<String, AuxiliaryImage>>() {}.type)

        getImageCodeForAuxiliaryImages().let { imageCode ->
            allAuxImages.entries.forEach { entry ->
                if (entry.key.contains(imageCode, true))
                    auxiliaryImagesForGroupKey.add(entry.value.externalImageRef)
            }
        }

        return if (auxiliaryImagesForGroupKey.isNotEmpty()) auxiliaryImagesForGroupKey else auxiliaryImages
    }

    private fun getImageCodeForAuxiliaryImages(): String {
        var imageCode = ""

        getSelectedGroupKey()?.split("\\s+")?.let {
            imageCode = when (it.size) {
                1 -> it[0]
                else -> {
                    it.forEachIndexed { i, s ->
                        imageCode = if (i == 0) s else imageCode.plus(s)
                    }
                    imageCode
                }
            }
        }

        return imageCode
    }

    override fun onCartSummarySuccess(cartSummaryResponse: CartSummaryResponse) {
        if (Utils.isCartSummarySuburbIDEmpty(cartSummaryResponse)) {
            activity?.apply {
                ScreenManager.presentDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE)
            }
        } else confirmDeliveryLocation()
    }

    override fun responseFailureHandler(response: Response) {

    }

    private fun confirmDeliveryLocation() {
        this.childFragmentManager.apply {
            ConfirmDeliveryLocationFragment.newInstance().show(this, ConfirmDeliveryLocationFragment::class.java.simpleName)
        }
    }

    override fun onConfirmLocation() {
        //continue add to cart request
        addItemToCart()
    }

    override fun onConfirmLocationDialogDismiss() {
        //cancel add to cart request
    }

    override fun onSetNewLocation() {
        ScreenManager.presentDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE)
    }

    private fun updateStockAvailability(storeId: String?) {
        storeIdForInventory = storeId
        productDetails?.apply {
            otherSkus?.let {
                val multiSKUs = it.joinToString(separator = "-") { it.sku }
                productDetailsPresenter?.loadStockAvailability(storeIdForInventory!!, multiSKUs, false)
            }
        }

    }

    override fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse) {
        activity?.apply {
            addItemToCartResponse.data?.let {
                if (it.size > 0) {
                    setResult(RESULT_OK, Intent().putExtra("addedToCartMessage", it[0].message))
                    onBackPressed()
                }
            }
        }
    }

}