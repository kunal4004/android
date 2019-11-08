package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.Manifest
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.location.Location
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
import kotlinx.android.synthetic.main.product_details_options_and_information_layout.*
import kotlinx.android.synthetic.main.product_details_price_layout.*
import kotlinx.android.synthetic.main.product_details_size_and_color_layout.*
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.ui.activities.AddToShoppingListActivity.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductColorSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductSizeSelectorAdapter
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter
import za.co.woolworths.financial.services.android.ui.adapters.holder.ProductListingViewHolderItems
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.ConfirmDeliveryLocationFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.ProductDetailsFragmentNew.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseProductUtils
import za.co.woolworths.financial.services.android.ui.fragments.shop.utils.NavigateToShoppingList
import za.co.woolworths.financial.services.android.ui.views.actionsheet.QuantitySelectorFragment
import za.co.woolworths.financial.services.android.util.*
import java.util.*
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.Color
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.product_deatils_delivery_location_layout.*
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.product.ProductInformationActivity
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter.*


class ProductDetailsFragment : Fragment(), ProductDetailsContract.ProductDetailsView, MultipleImageInterface, IOnConfirmDeliveryLocationActionListener, PermissionResultCallback, ILocationProvider, View.OnClickListener {

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
    private val SSO_REQUEST_ADD_TO_SHOPPING_LIST = 1011
    private val SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK = 1012
    private var permissionUtils: PermissionUtils? = null
    private var mFuseLocationAPISingleton: FuseLocationAPISingleton? = null
    private var isApiCallInProgress: Boolean = false


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
        mFuseLocationAPISingleton = FuseLocationAPISingleton
        initViews()
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
        closePage.setOnClickListener { activity?.onBackPressed() }
        configureDefaultUI()
    }

    override fun onClick(v: View?) {
        if (isApiCallInProgress)
            return
        when (v?.id) {
            R.id.addToCartAction -> addItemToCart()
            R.id.quantitySelector -> onQuantitySelector()
            R.id.addToShoppingList -> addItemToShoppingList()
            R.id.checkInStoreAvailability, R.id.findInStoreAction -> findItemInStore()
            R.id.editDeliveryLocation -> updateDeliveryLocation()
            R.id.productDetailsInformation -> showProductDetailsInformation()
            R.id.productIngredientsInformation -> showProductIngredientsInformation()
        }
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

        updateStockAvailabilityLocation()

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

        if (getSelectedSku() == null) {
            requestSelectSize()
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

        if (TextUtils.isEmpty(Utils.retrieveStoreId(productDetails?.fulfillmentType))) {
            //setSelectedSku(null)
            hideProgressBar()
            val message = "Unfortunately this item is unavailable in " + deliveryLocation.suburb.name + ". Try changing your delivery location and try again."
            activity?.apply {
                Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC, getString(R.string.product_unavailable), message)
            }
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
        if (listOfItems.isNotEmpty()) {
            productDetailsPresenter?.postAddItemToCart(listOfItems)
        }
    }

    override fun onSessionTokenExpired() {
        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
        activity?.runOnUiThread { ScreenManager.presentSSOSignin(activity) }
    }

    override fun onProductDetailsSuccess(productDetails: ProductDetails) {
        this.productDetails = productDetails
        if (!this.productDetails?.otherSkus.isNullOrEmpty()) {

            storeIdForInventory = ProductListingViewHolderItems.getFulFillmentStoreId(productDetails.fulfillmentType)
            when (storeIdForInventory.isNullOrEmpty()) {
                true -> showProductUnavailable()
                false -> {
                    showProductDetailsLoading()
                    val multiSKUs = productDetails.otherSkus.joinToString(separator = "-") { it.sku }
                    productDetailsPresenter?.loadStockAvailability(storeIdForInventory!!, multiSKUs, true)
                }
            }

        } else {
            showErrorWhileLoadingProductDetails()
        }
    }

    override fun onProductDetailedFailed(response: Response) {
        showErrorWhileLoadingProductDetails()
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

    }

    private fun showColors() {
        val spanCount = Utils.calculateNoOfColumns(activity, 50F)
        colorSelectorRecycleView.layoutManager = GridLayoutManager(activity, spanCount)
        /* val layoutManager = FlexboxLayoutManager(activity)
         layoutManager.flexDirection = FlexDirection.ROW
         layoutManager.justifyContent = JustifyContent.FLEX_START
         colorSelectorRecycleView.layoutManager = layoutManager*/
        productColorSelectorAdapter = ProductColorSelectorAdapter(otherSKUsByGroupKey, this).apply {
            colorSelectorRecycleView.adapter = this
            updateColorSelection(getSelectedGroupKey())
            showSelectedColor()
        }
        colorSelectorLayout.visibility = View.VISIBLE
    }

    private fun showSize() {
        //productColorSelectorAdapter?
        /*val layoutManager1 = FlexboxLayoutManager(activity)
        layoutManager1.flexDirection = FlexDirection.ROW
        layoutManager1.justifyContent = JustifyContent.FLEX_START*/
        sizeSelectorRecycleView.layoutManager = GridLayoutManager(activity, 4)
        productSizeSelectorAdapter = ProductSizeSelectorAdapter(otherSKUsByGroupKey[getSelectedGroupKey()]!!, this).apply {
            sizeSelectorRecycleView.adapter = this
        }
        sizeSelectorLayout.visibility = View.VISIBLE
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
        if (!hasSize) {
            setSelectedSku(this.defaultSku)
            updateAddToCartButtonForSelectedSKU()
        }
        /*if (hasColor)
            this.setSelectedColorIcon()*/
        loadSizeAndColor()

        if (!TextUtils.isEmpty(this.productDetails?.ingredients))
            productIngredientsInformation.visibility = View.VISIBLE

        productDetails?.let {
            it.saveText?.apply { setPromotionalText(this) }
            BaseProductUtils.displayPrice(textPrice, textActualPrice, it.price, it.wasPrice, it.priceType, it.kilogramPrice)
        }
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
        showSelectedSize()
        updateUIForSelectedSKU(getSelectedSku())
    }

    override fun onColorSelection(selectedColor: String?) {
        setSelectedGroupKey(selectedColor)
        showSelectedColor()
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
            showSelectedSize()

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
        activity?.apply {
            Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.ERROR, response.desc)
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

    override fun onConfirmLocationDialogDismiss() {
        //cancel add to cart request
    }

    override fun onSetNewLocation() {
        ScreenManager.presentDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE)
    }

    private fun updateStockAvailability(isDefaultRequest: Boolean) {
        storeIdForInventory = Utils.retrieveStoreId(productDetails?.fulfillmentType)
        when(storeIdForInventory.isNullOrEmpty()){
            true->showProductUnavailable()
            false ->{
                productDetails?.apply {
                    otherSkus?.let {
                        val multiSKUs = it.joinToString(separator = "-") { it.sku }
                        productDetailsPresenter?.loadStockAvailability(storeIdForInventory!!, multiSKUs, isDefaultRequest)
                    }
                }
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

    private fun addItemToShoppingList() {

        if (getSelectedSku() == null) {
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
                        ScreenManager.presentDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE)
                    }
                    FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS -> {
                        findItemInStore()
                    }
                    REQUEST_SUBURB_CHANGE_FOR_STOCK -> {

                        updateStockAvailabilityLocation()

                        if (!Utils.retrieveStoreId(productDetails?.fulfillmentType).equals(storeIdForInventory, ignoreCase = true)) {
                            updateStockAvailability(true)
                        }
                    }
                }
            }
            SSOActivity.SSOActivityResult.SUCCESS.rawValue() -> {
                updateStockAvailabilityLocation()
                when (requestCode) {
                    SSO_REQUEST_ADD_TO_CART -> {
                        addItemToCart()
                    }
                    SSO_REQUEST_ADD_TO_SHOPPING_LIST -> {
                        addItemToShoppingList()
                        //One time biometricsWalkthrough
                        activity?.apply { ScreenManager.presentBiometricWalkthrough(this) }
                    }
                    SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK -> {
                        ScreenManager.presentDeliveryLocationActivity(activity, REQUEST_SUBURB_CHANGE_FOR_STOCK)
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
                    Utils.displayValidationMessage(this, CustomPopUpWindow.MODAL_LAYOUT.LOCATION_OFF, "")
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

    override fun PartialPermissionGranted(request_code: Int, granted_permissions: ArrayList<String>?) {
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
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
            toCartAndFindInStoreLayout.visibility = View.VISIBLE
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
            hideProgressBar()
            Utils.displayValidationMessage(activity, CustomPopUpWindow.MODAL_LAYOUT.CLI_ERROR, getString(R.string.statement_send_email_false_desc))
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showSelectedColor() {
        selectedColor.text = " - ${getSelectedGroupKey()}"
    }

    @SuppressLint("SetTextI18n")
    private fun showSelectedSize() {
        getSelectedSku().let {
            selectedSizePlaceholder.text = getString(if (it != null) R.string.product_placeholder_selected_size else R.string.product_placeholder_select_size)
            selectedSize.text = if (it != null) " - ${it.size}" else ""
        }
    }

    override fun updateDeliveryLocation() {
        activity?.apply {
            when (SessionUtilities.getInstance().isUserAuthenticated) {
                true -> ScreenManager.presentDeliveryLocationActivity(this, REQUEST_SUBURB_CHANGE_FOR_STOCK)
                false -> ScreenManager.presentSSOSignin(this, SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK)
            }

        }
    }

    override fun updateStockAvailabilityLocation() {
        activity?.apply {
            val userLocation = Utils.getPreferredDeliveryLocation()
            val defaultLocation = WoolworthsApplication.getQuickShopDefaultValues()
            currentDeliveryLocation.text = if (userLocation != null) userLocation.suburb?.name else defaultLocation?.suburb?.name
        }

    }

    override fun showProductDetailsInformation() {
        activity?.apply {
            val intent = Intent(this, ProductInformationActivity::class.java)
            intent.putExtra(ProductInformationActivity.PRODUCT_DETAILS, Utils.toJson(productDetails))
            intent.putExtra(ProductInformationActivity.PRODUCT_INFORMATION_TYPE, ProductInformationActivity.ProductInformationType.DETAILS)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun showProductIngredientsInformation() {
        activity?.apply {
            val intent = Intent(this, ProductInformationActivity::class.java)
            intent.putExtra(ProductInformationActivity.PRODUCT_DETAILS, Utils.toJson(productDetails))
            intent.putExtra(ProductInformationActivity.PRODUCT_INFORMATION_TYPE, ProductInformationActivity.ProductInformationType.INGREDIENTS)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    private fun showProductUnavailable(){
        /*setSelectedSku(this.defaultSku)
        hideProductDetailsLoading()
        toCartAndFindInStoreLayout.visibility = View.GONE*/
        hideProgressBar()
    }


}