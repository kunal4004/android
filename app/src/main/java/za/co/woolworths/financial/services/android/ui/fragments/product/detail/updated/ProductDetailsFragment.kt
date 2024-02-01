package za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.text.Html
import android.text.TextUtils
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.*
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ProductDetailsFragmentBinding
import com.awfs.coordination.databinding.PromotionalImageBinding
import com.facebook.FacebookSdk.getApplicationContext
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.perfectcorp.perfectlib.CameraView
import com.perfectcorp.perfectlib.MakeupCam
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.chanel.utils.ChanelUtils
import za.co.woolworths.financial.services.android.common.SingleMessageCommonToast
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.ILocationProvider
import za.co.woolworths.financial.services.android.dynamicyield.data.response.request.*
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.Item
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.ProductSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureEnable
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener.EnhancedSubstitutionBottomSheetDialog
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.listener.EnhancedSubstitutionListener
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.triggerFirebaseEventForAddSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.triggerFirebaseEventForSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.ManageSubstitutionFragment
import za.co.woolworths.financial.services.android.enhancedSubstitution.view.SearchSubstitutionFragment
import za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel.ProductSubstitutionViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.AddToCartLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmLocationResponseLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UpdateScreenLiveData
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.BrandNavigationDetails
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.*
import za.co.woolworths.financial.services.android.models.network.AppContextProviderImpl
import za.co.woolworths.financial.services.android.models.network.NetworkConfig
import za.co.woolworths.financial.services.android.models.network.Resource
import za.co.woolworths.financial.services.android.models.network.Status
import za.co.woolworths.financial.services.android.presentation.addtolist.AddToListFragment.Companion.ADD_TO_SHOPPING_LIST_REQUEST_CODE
import za.co.woolworths.financial.services.android.recommendations.data.response.request.Event
import za.co.woolworths.financial.services.android.recommendations.data.response.request.ProductX
import za.co.woolworths.financial.services.android.recommendations.presentation.viewmodel.RecommendationViewModel
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess
import za.co.woolworths.financial.services.android.shoptoggle.common.UnsellableAccess.Companion.getToggleFulfilmentResultWithUnSellable
import za.co.woolworths.financial.services.android.shoptoggle.presentation.ShopToggleActivity
import za.co.woolworths.financial.services.android.ui.activities.CustomPopUpWindow
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity
import za.co.woolworths.financial.services.android.ui.activities.SSOActivity
import za.co.woolworths.financial.services.android.ui.activities.WStockFinderActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_CART
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.*
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Options
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.request.Page
import za.co.woolworths.financial.services.android.ui.activities.dashboard.DynamicYield.response.DyChooseVariationCallViewModel
import za.co.woolworths.financial.services.android.ui.activities.product.ProductInformationActivity
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.featureutils.RatingAndReviewUtil
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.model.*
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.network.apihelper.RatingAndReviewApiHelper
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModel
import za.co.woolworths.financial.services.android.ui.activities.rating_and_review.viewmodel.RatingAndReviewViewModelFactory
import za.co.woolworths.financial.services.android.ui.adapters.*
import za.co.woolworths.financial.services.android.ui.adapters.ProductViewPagerAdapter.MultipleImageInterface
import za.co.woolworths.financial.services.android.ui.extension.deviceWidth
import za.co.woolworths.financial.services.android.ui.extension.underline
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.payflex.PayFlexBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.Request.*
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.DyChangeAttribute.ViewModel.DyChangeAttributeViewModel
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.IOnConfirmDeliveryLocationActionListener
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.dialog.OutOfStockMessageDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.detail.updated.size_guide.SkinProfileDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFragment.Companion.SET_DELIVERY_LOCATION_REQUEST_CODE
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.FoodProductNotAvailableForCollectionDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.ProductNotAvailableForCollectionDialog
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.usecase.Constants
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.BaseProductUtils
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ColourSizeVariants
import za.co.woolworths.financial.services.android.ui.fragments.shoppinglist.listitems.ShoppingListDetailFragment.Companion.ADD_TO_CART_SUCCESS_RESULT
import za.co.woolworths.financial.services.android.ui.views.CustomBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.views.LockableNestedScrollViewV2
import za.co.woolworths.financial.services.android.ui.views.UnsellableItemsBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.views.WMaterialShowcaseView
import za.co.woolworths.financial.services.android.ui.views.actionsheet.ProductDetailsFindInStoreDialog
import za.co.woolworths.financial.services.android.ui.views.actionsheet.QuantitySelectorFragment
import za.co.woolworths.financial.services.android.ui.views.tooltip.TooltipDialog
import za.co.woolworths.financial.services.android.ui.vto.di.qualifier.OpenSelectOption
import za.co.woolworths.financial.services.android.ui.vto.di.qualifier.OpenTermAndLighting
import za.co.woolworths.financial.services.android.ui.vto.presentation.DataPrefViewModel
import za.co.woolworths.financial.services.android.ui.vto.presentation.LiveCameraViewModel
import za.co.woolworths.financial.services.android.ui.vto.presentation.PermissionViewModel
import za.co.woolworths.financial.services.android.ui.vto.presentation.VtoApplyEffectOnImageViewModel
import za.co.woolworths.financial.services.android.ui.vto.ui.PermissionAction
import za.co.woolworths.financial.services.android.ui.vto.ui.PfSDKInitialCallback
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoSelectOptionListener
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.ui.vto.ui.camera.CameraMonitor
import za.co.woolworths.financial.services.android.ui.vto.ui.gallery.ImageResultContract
import za.co.woolworths.financial.services.android.ui.vto.utils.PermissionUtil
import za.co.woolworths.financial.services.android.ui.vto.utils.SdkUtility
import za.co.woolworths.financial.services.android.ui.vto.utils.VirtualTryOnUtil
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.pdp.ShoptimiserProductDetailPageImpl
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.ui.viewmodel.ShopOptimiserViewModel
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_1000_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_1500_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_500_MS
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.SDK_INIT_FAIL
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.STOCK_AVAILABILITY_0
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_COLOR_LIVE_CAMERA
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_COLOR_NOT_MATCH
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_FACE_NOT_DETECT
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.VTO_FAIL_IMAGE_LOAD
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.saveAnonymousUserLocationDetails
import za.co.woolworths.financial.services.android.util.Utils.ADD_TO_CART
import za.co.woolworths.financial.services.android.util.Utils.ADD_TO_CART_V1
import za.co.woolworths.financial.services.android.util.Utils.CHANGE_ATTRIBUTE
import za.co.woolworths.financial.services.android.util.Utils.CHANGE_ATTRIBUTE_DY_TYPE
import za.co.woolworths.financial.services.android.util.Utils.COLOR_ATTRIBUTE
import za.co.woolworths.financial.services.android.util.Utils.DY_CHANNEL
import za.co.woolworths.financial.services.android.util.Utils.IPAddress
import za.co.woolworths.financial.services.android.util.Utils.PRODUCT_DETAILS_PAGE
import za.co.woolworths.financial.services.android.util.Utils.PRODUCT_PAGE
import za.co.woolworths.financial.services.android.util.Utils.QUANTITY_ATTRIBUTE
import za.co.woolworths.financial.services.android.util.Utils.SIZE_ATTRIBUTE
import za.co.woolworths.financial.services.android.util.Utils.SYNC_CART
import za.co.woolworths.financial.services.android.util.Utils.SYNC_CART_V1
import za.co.woolworths.financial.services.android.util.Utils.ZAR
import za.co.woolworths.financial.services.android.util.analytics.AnalyticsManager
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.logException
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager.Companion.setCrashlyticsString
import za.co.woolworths.financial.services.android.util.analytics.dto.AddToWishListFirebaseEventData
import za.co.woolworths.financial.services.android.util.analytics.dto.toAnalyticItem
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import za.co.woolworths.financial.services.android.util.pickimagecontract.PickImageFileContract
import za.co.woolworths.financial.services.android.util.pickimagecontract.PickImageGalleryContract
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import java.io.File
import javax.inject.Inject
import kotlin.collections.set


@AndroidEntryPoint
class ProductDetailsFragment :
    BaseFragmentBinding<ProductDetailsFragmentBinding>(ProductDetailsFragmentBinding::inflate),
    ProductDetailsContract.ProductDetailsView,
    MultipleImageInterface, IOnConfirmDeliveryLocationActionListener, PermissionResultCallback,
    ILocationProvider, View.OnClickListener,
    OutOfStockMessageDialogFragment.IOutOfStockMessageDialogDismissListener,
    ProductNotAvailableForCollectionDialog.IProductNotAvailableForCollectionDialogListener,
    VtoSelectOptionListener, WMaterialShowcaseView.IWalkthroughActionListener, VtoTryAgainListener,
    View.OnTouchListener, ReviewThumbnailAdapter.ThumbnailClickListener,
    FoodProductNotAvailableForCollectionDialog.IProductNotAvailableForCollectionDialogListener,
    EnhancedSubstitutionListener {

    var productDetails: ProductDetails? = null
    private var subCategoryTitle: String? = null
    private var brandHeaderText: String? = null
    private var mFetchFromJson: Boolean = false
    private var defaultProductResponse: String? = null
    private var auxiliaryImages: MutableList<String> = ArrayList()
    private var productDetailsPresenter: ProductDetailsContract.ProductDetailsPresenter? = null
    private var storeIdForInventory: String? = ""
    private var otherSKUsByGroupKey: LinkedHashMap<String, ArrayList<OtherSkus>> = linkedMapOf()
    private var hasColor: Boolean = false
    private var hasSize: Boolean = false
    private var defaultSku: OtherSkus? = null
    private var selectedSku: OtherSkus? = null
    private var selectedGroupKey: String? = null
    private var productSizeSelectorAdapter: ProductSizeSelectorAdapter? = null
    private var productColorSelectorAdapter: ProductColorSelectorAdapter? = null
    private var selectedQuantity: Int? = 1
    private val SSO_REQUEST_ADD_TO_CART = 1010
    private val SSO_REQUEST_WRITE_A_REVIEW = 1020
    private val REQUEST_SUBURB_CHANGE = 153
    private val REQUEST_SUBURB_CHANGE_FOR_STOCK = 155
    private val REQUEST_SUBURB_CHANGE_FOR_LIQUOR = 156
    private val SSO_REQUEST_ADD_TO_SHOPPING_LIST = 1011
    private val SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK = 1012
    private val SSO_REQUEST_FOR_ENHANCE_SUBSTITUTION = 1013
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
    private lateinit var reviewThumbnailAdapter: ReviewThumbnailAdapter
    private lateinit var secondaryRatingAdapter: SecondaryRatingAdapter
    private var thumbnailFullList = listOf<Thumbnails>()
    private var ratingReviewResponse: RatingReviewResponse? = null
    private val permissionViewModel: PermissionViewModel by viewModels()
    private var isFromFile = false
    private var liveCamera: Boolean = false
    private lateinit var uri: Uri
    private var isVtoImage: Boolean = false
    private var isTryIt: Boolean = true
    private var selectedImageUri: Uri? = null
    private var isPhotoPickedFromGallery: Boolean = false
    private var isPhotoPickedFromDefaultCamera: Boolean = false
    private var saveVtoApplyImage: Bitmap? = null
    private var isColorSelectionLayoutOnTop: Boolean = false
    private var isLiveCamera: Boolean = false
    private var unSellableFlowFromOnActivityResult: Boolean = false
    private var isColorAppliedWithLiveCamera: Boolean = false
    private val vtoApplyEffectOnImageViewModel: VtoApplyEffectOnImageViewModel? by activityViewModels()
    private val liveCameraViewModel: LiveCameraViewModel? by activityViewModels()
    private val dataPrefViewModel: DataPrefViewModel? by activityViewModels()
    private val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()
    private var makeupCamera: MakeupCam? = null
    private var isObserveImageData: Boolean = false
    private var isRefreshImageEffectLiveCamera: Boolean = false
    private var isDividerVtoEffect: Boolean = false
    private var isLiveCameraResumeState: Boolean = false
    private var isLiveCameraOpened: Boolean = false
    private lateinit var job: Job
    private lateinit var coroutineScope: CoroutineScope
    private var isFaceNotDetect: Boolean = false
    private var isFaceDetect: Boolean = false
    private var isColorNotMatch: Boolean = false
    private var isTakePicture: Boolean = false
    private var isPickedImageFromLiveCamera: Boolean = false
    private var takenOriginalPicture: Bitmap? = null
    private var isVtoSdkInitFail: Boolean = false
    private var bannerLabel: String? = null
    private var bannerImage: String? = null
    private var isUnSellableItemsRemoved: Boolean? = false
    private var isUserBrowsing: Boolean = false
    private var isRnRAPICalled = false
    private var prodId: String = "-1"
    private lateinit var moreReviewViewModel: RatingAndReviewViewModel
    private val dialogInstance = FoodProductNotAvailableForCollectionDialog.newInstance()
    private val productSubstitutionViewModel: ProductSubstitutionViewModel by activityViewModels()
    private var productList:ProductList? = null
    private var selectionChoice: String = ""
    private var substitutionId: String? = ""
    private var commarceItemId: String? = ""
    private var substitutionProductItem: ProductList? = null
    private var kiboItem: Item? = null
    private var isSubstiuteItemAdded = false

    private val recommendationViewModel: RecommendationViewModel by viewModels()
    private var bottomSheetWebView: PayFlexBottomSheetDialog? =null
    private var stockAvailable: Int? = null

    @OpenTermAndLighting
    @Inject
    lateinit var vtoBottomSheetDialog: VtoBottomSheetDialog

    @OpenSelectOption
    @Inject
    lateinit var vtoOptionSelectBottomDialog: VtoBottomSheetDialog

    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    @Inject
    lateinit var vtoSavedPhotoToast: SingleMessageCommonToast


    @Inject
    lateinit var enhancedSubstitutionBottomSheetDialog: EnhancedSubstitutionBottomSheetDialog

    lateinit var wfsShoptimiserProduct: ShoptimiserProductDetailPageImpl

    private val dyReportEventViewModel: DyChangeAttributeViewModel by viewModels()
    private var productId: String? = null
    private val dyChooseVariationViewModel: DyChooseVariationCallViewModel by viewModels()
    private var dyServerId: String? = null
    private var dySessionId: String? = null
    private var config: NetworkConfig? = null

    companion object {
        const val INDEX_STORE_FINDER = 1
        const val INDEX_ADD_TO_CART = 2
        const val INDEX_ADD_TO_SHOPPING_LIST = 3
        const val INDEX_SEARCH_FROM_LIST = 4
        const val TAG = "ProductDetailsFragment"
        const val HTTP_CODE_502 = 502
        fun newInstance() = ProductDetailsFragment()
        const val REQUEST_PERMISSION_MEDIA = 100
        const val ZERO_REVIEWS = "0 Reviews"

        const val STR_PRODUCT_CATEGORY = "strProductCategory"
        const val STR_PRODUCT_LIST = "strProductList"
        const val STR_BRAND_HEADER = "strBandHeaderDesc"
        const val IS_BROWSING = "isBrowsing"
        const val BRAND_NAVIGATION_DETAILS = "BRAND_NAVIGATION_DETAILS"
        const val STR_STOCK_AVAILABLE = "STR_STOCK_AVAILABLE"

        const val PRODUCTLIST = "PRODUCT_LIST"
        fun newInstance(
            productList: ProductList?,
        ) = ProductDetailsFragment().withArgs {
            putSerializable(PRODUCTLIST, productList)
        }
    }

    private val  shoptimiserViewModel: ShopOptimiserViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productDetails = Utils.jsonStringToObject(
                getString(STR_PRODUCT_LIST),
                ProductDetails::class.java
            ) as ProductDetails
            subCategoryTitle = getString(STR_PRODUCT_CATEGORY)
            brandHeaderText = getString(STR_BRAND_HEADER, AppConstant.EMPTY_STRING)

            (getSerializable(BRAND_NAVIGATION_DETAILS) as? BrandNavigationDetails)?.let {
                bannerLabel = it.bannerLabel ?: ""
                bannerImage = it.bannerImage ?: ""
            }

            brandHeaderText = getString(STR_BRAND_HEADER, AppConstant.EMPTY_STRING)
            defaultProductResponse = getString("productResponse")
            mFetchFromJson = getBoolean("fetchFromJson")
            isUserBrowsing = getBoolean(IS_BROWSING, false)
            productList = getSerializable(PRODUCTLIST) as? ProductList?
            stockAvailable = getInt(STR_STOCK_AVAILABLE)
        }
        productDetailsPresenter = ProductDetailsPresenterImpl(this, ProductDetailsInteractorImpl())
        productId = productDetails?.productId
        config = NetworkConfig(AppContextProviderImpl())
        if (Utils.getDyServerId() != null)
            dyServerId = Utils.getDyServerId()
        if (Utils.getDySessionId() != null)
            dySessionId = Utils.getDySessionId()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mFuseLocationAPISingleton = FuseLocationAPISingleton
        wfsShoptimiserProduct  = ShoptimiserProductDetailPageImpl(binding, shoptimiserViewModel)
        binding.initViews()
        addFragmentListener()
        setUniqueIds()
        productDetails?.let {
            wfsShoptimiserProduct.addProductDetails(it)
        }
        setUpCartCountPDP()

    }

    fun showEnhancedSubstitutionDialog() {
        if (SessionUtilities.getInstance().isUserAuthenticated
            && Utils.isEnhanceSubstitutionFeatureShown() == false
            && KotlinUtils.getDeliveryType()?.deliveryType == Delivery.DASH.type
            && isEnhanceSubstitutionFeatureEnable() == true
        ) {
            enhancedSubstitutionBottomSheetDialog.showEnhancedSubstitionBottomSheetDialog(
                this@ProductDetailsFragment,
                requireActivity(),
                getString(R.string.enhanced_substitution_title),
                getString(R.string.enhanced_substitution_desc),
                getString(R.string.enhanced_substitution_btn)
            )
        }
    }

    private fun prepareDynamicYieldPageViewRequestEvent() {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress, config?.getDeviceModel())
        val skuIdList: ArrayList<String>? = ArrayList()
        for (othersku in productDetails!!.otherSkus) {
            if (othersku.sku != null) {
                var skuID = othersku.sku
                skuIdList?.add(skuID!!)
            }
        }
        val page = Page(skuIdList, PRODUCT_DETAILS_PAGE, PRODUCT_PAGE, null,null)
        val context = Context(device, page, DY_CHANNEL)
        val options = Options(true)
        val homePageRequestEvent = HomePageRequestEvent(user, session, context, options)
        dyChooseVariationViewModel.createDyRequest(homePageRequestEvent)
    }


    private fun setUpCartCountPDP() {
        val cartIconMargin = requireContext().resources.getDimensionPixelSize(R.dimen.five_dp)
        val params = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        if (SessionUtilities.getInstance().isUserAuthenticated && QueryBadgeCounter.instance.cartCount <= 0) {
            binding.openCart.cartCountTextView?.visibility = View.GONE
        } else if (SessionUtilities.getInstance().isUserAuthenticated && QueryBadgeCounter.instance.cartCount > 0) {
            binding.openCart.cartCountTextView?.visibility = View.VISIBLE
            binding.openCart.cartCountTextView?.text =
                QueryBadgeCounter.instance.cartCount.toString()
            params.setMargins(cartIconMargin, 0, 0, 0)
            binding.openCart.cartCountImage.setLayoutParams(params)
        }
    }

    private fun addFragmentListener() {
        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT) { _, _ ->
            // As User selects to change the delivery location. So we will call confirm place API and will change the users location.
            binding.getUpdatedValidateResponse()
        }
        listenerForUnsellable()
        KotlinUtils.setAddToListFragmentResultListener(
            ADD_TO_SHOPPING_LIST_REQUEST_CODE,
            requireActivity(),
            viewLifecycleOwner,
            binding.productLayout
        ) {}

        setFragmentResultListener(SearchSubstitutionFragment.SELECTED_SUBSTITUTED_PRODUCT) { _, bundle ->
            // User Selects product from search  or kibo and came back to pdp
            bundle?.apply {
                if (bundle.containsKey(SearchSubstitutionFragment.SUBSTITUTION_ITEM_KEY)) {
                    // item is added in cart yet i.e. commerce id is not empty so call getSubstitution api to refresh substitution cell
                    substitutionProductItem =
                        getSerializable(SearchSubstitutionFragment.SUBSTITUTION_ITEM_KEY) as? ProductList
                    showSubstituteItemCell(true, substitutionProductItem)
                }
                if (bundle.containsKey(ManageSubstitutionFragment.DONT_WANT_SUBSTITUTE_LISTENER)) {
                    updateItemCellForEnhanceSubstitution(
                        getString(R.string.dont_substitute),
                        SubstitutionChoice.NO.name
                    )
                }
                if (bundle.containsKey(ManageSubstitutionFragment.LET_MY_SHOPPER_CHOOSE)) {
                    updateItemCellForEnhanceSubstitution(
                        getString(R.string.substitute_default),
                        SubstitutionChoice.SHOPPER_CHOICE.name
                    )
                }
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setUpToolBar()
    }

    private fun ProductDetailsFragmentBinding.initViews() {
        openCart.root.setOnClickListener(this@ProductDetailsFragment)
        backArrow?.setOnClickListener(this@ProductDetailsFragment)
        share?.setOnClickListener(this@ProductDetailsFragment)
        imgVTOOpen?.setOnClickListener(this@ProductDetailsFragment)
        isOutOfStockFragmentAdded = false
        configureDefaultUI()
        scrollView.setOnTouchListener(this@ProductDetailsFragment)
        scrollView.setOnScrollStoppedListener(onScrollStoppedListener)

        hideRatingAndReview()
        setupViewModel()
        updateReportLikeStatus()
        toCartAndFindInStoreLayout.apply {
            addToCartAction?.setOnClickListener(this@ProductDetailsFragment)
            quantitySelector?.setOnClickListener(this@ProductDetailsFragment)
            findInStoreAction?.setOnClickListener(this@ProductDetailsFragment)
        }
        productDetailOptionsAndInformation.apply {
            addToShoppingList?.setOnClickListener(this@ProductDetailsFragment)
            checkInStoreAvailability?.setOnClickListener(this@ProductDetailsFragment)
            productDetailsInformation?.setOnClickListener(this@ProductDetailsFragment)
            productIngredientsInformation?.setOnClickListener(this@ProductDetailsFragment)
            nutritionalInformation?.setOnClickListener(this@ProductDetailsFragment)
            dietaryInformation?.setOnClickListener(this@ProductDetailsFragment)
            allergensInformation?.setOnClickListener(this@ProductDetailsFragment)
            btViewMoreReview.setOnClickListener(this@ProductDetailsFragment)
            tvRatingDetails.setOnClickListener(this@ProductDetailsFragment)
            writeAReviewLink.root.setOnClickListener(this@ProductDetailsFragment)
        }
        deliveryLocationLayout.apply {
            editDeliveryLocation?.setOnClickListener(this@ProductDetailsFragment)
        }
        sizeColorSelectorLayout.apply {
            moreColor?.setOnClickListener(this@ProductDetailsFragment)
            sizeGuide?.setOnClickListener(this@ProductDetailsFragment)
        }
        vtoLayout.apply {
            imgCloseVTO?.setOnClickListener(this@ProductDetailsFragment)
            imgVTORefresh?.setOnClickListener(this@ProductDetailsFragment)
            brandView?.brandOpenCart?.setOnClickListener(this@ProductDetailsFragment)
            brandView?.brandBackArrow?.setOnClickListener(this@ProductDetailsFragment)
            retakeCamera?.setOnClickListener(this@ProductDetailsFragment)
            changeImage?.setOnClickListener(this@ProductDetailsFragment)
            changeImageFiles?.setOnClickListener(this@ProductDetailsFragment)
            imgDownloadVTO?.setOnClickListener(this@ProductDetailsFragment)
            imgVTOSplit?.setOnClickListener(this@ProductDetailsFragment)
            captureImage?.setOnClickListener(this@ProductDetailsFragment)
            cameraSurfaceView.setOnTouchListener { _, event ->
                pinchZoomOnVtoLiveCamera(event)
                true
            }
        }
        productDetailOptionsAndInformation.customerReview.tvSkinProfile.setOnClickListener(this@ProductDetailsFragment)
        ratingLayout.tvTotalReviews.setOnClickListener(this@ProductDetailsFragment)
        productDetailOptionsAndInformation.customerReview.reviewHelpfulReport.tvReport.setOnClickListener(
            this@ProductDetailsFragment)
    }


    private fun setupViewModel() {
        moreReviewViewModel = ViewModelProvider(
            this,
            RatingAndReviewViewModelFactory(RatingAndReviewApiHelper())
        ).get(RatingAndReviewViewModel::class.java)
    }

    private fun ProductDetailsFragmentBinding.updateReportLikeStatus() {
        if (ratingReviewResponse?.reviews?.isNotEmpty() == true) {
            ratingReviewResponse?.reviews?.get(0)?.let {
                if (RatingAndReviewUtil.likedReviews.contains(it.id.toString())) {
                    productDetailOptionsAndInformation.customerReview.reviewHelpfulReport.ivLike?.setImageResource(
                        R.drawable.iv_like_selected)
                }
                if (RatingAndReviewUtil.reportedReviews.contains(it.id.toString())) {
                    productDetailOptionsAndInformation.customerReview.reviewHelpfulReport.apply {
                        tvReport?.setTextColor(Color.RED)
                        tvReport?.text = resources?.getString(R.string.reported)
                        tvReport?.setTypeface(tvReport.typeface, Typeface.BOLD)
                        tvReport?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }
                }
            }

        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            updateAddToCartButtonForSelectedSKU()
            setUpToolBar()
            isUnSellableItemsRemoved()
            listenerForUnsellable()
        }
    }

    private fun setUpToolBar() {
        (activity as? BottomNavigationActivity)?.apply {
            hideBottomNavigationMenu()
            // Animation delay
            Handler().postDelayed({ hideToolbar() }, DELAY_500_MS)
        }
    }

    private fun pinchZoomOnVtoLiveCamera(event: MotionEvent?) {
        binding.scrollView.requestDisallowInterceptTouchEvent(true)
        val cameraMonitor =
            CameraMonitor(requireActivity(), makeupCamera, lifecycle)
        cameraMonitor.pinchZoom(requireActivity(), event!!)

    }

    private fun showVTOTryItOn() {
        binding.imgVTOOpen?.setImageResource(R.drawable.ic_camera_vto)
        if (isTryIt) {
            dataPrefViewModel?.isTryItOn?.observe(
                viewLifecycleOwner,
                Observer { isTryItOn ->
                    if (isTryItOn && isTryIt) {
                        binding.imgVTOOpen?.setImageResource(R.drawable.ic_try_on_camera)
                        isTryIt = false
                        dataPrefViewModel?.disableTryItOn(false)
                    }

                })
        }
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
            R.id.editDeliveryLocation -> updateDeliveryLocation(launchNewToggleScreen = false)
            R.id.productDetailsInformation -> showDetailsInformation(
                ProductInformationActivity.ProductInformationType.DETAILS
            )
            R.id.productIngredientsInformation -> showDetailsInformation(
                ProductInformationActivity.ProductInformationType.INGREDIENTS
            )
            R.id.nutritionalInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.NUTRITIONAL_INFO)
            R.id.allergensInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.ALLERGEN_INFO)
            R.id.dietaryInformation -> showDetailsInformation(ProductInformationActivity.ProductInformationType.DIETARY_INFO)
            R.id.moreColor -> showMoreColors()
            R.id.share -> shareProduct()
            R.id.sizeGuide -> showDetailsInformation(ProductInformationActivity.ProductInformationType.SIZE_GUIDE)
            R.id.imgVTOOpen -> vtoOptionSelectBottomDialog.showBottomSheetDialog(
                this@ProductDetailsFragment,
                requireActivity(),
                false
            )
            R.id.openCart -> openCart()
            R.id.brand_openCart -> openCart()
            R.id.backArrow -> (activity as? BottomNavigationActivity)?.popFragment()
            R.id.brand_backArrow -> (activity as? BottomNavigationActivity)?.popFragment()
            R.id.imgCloseVTO -> binding.closeVto()
            R.id.imgVTORefresh -> clearEffect()
            R.id.retakeCamera -> binding.reOpenCamera()
            R.id.changeImage -> pickPhotoLauncher.launch("image/*")
            R.id.changeImageFiles -> pickPhotoFromFile.launch("image/*")
            R.id.imgDownloadVTO -> saveVtoApplyImage?.let {
                savePhoto(it)
            }
            R.id.imgVTOSplit -> binding.compareWithLiveCamera()
            R.id.captureImage -> binding.captureImageFromVtoLiveCamera()
            R.id.tvRatingDetails -> showRatingDetailsDailog()
            R.id.tvSkinProfile -> viewSkinProfileDialog()
            R.id.btViewMoreReview -> navigateToMoreReviewsScreen()
            R.id.tvTotalReviews -> {
                if (binding.ratingLayout.tvTotalReviews?.text != ZERO_REVIEWS) navigateToMoreReviewsScreen()
            }
            R.id.tvReport -> navigateToReportReviewScreen()
            R.id.iv_like -> likeButtonClicked()
            R.id.txt_substitution_edit -> substitutionEditButtonClick()
            R.id.writeAReviewLink -> openWriteAReviewFragment(productDetails?.productName,productDetails?.externalImageRefV2, productDetails?.productId)
        }
    }

    private fun openWriteAReviewFragment(productName: String?, imagePath: String?, productId: String?) {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(activity,
                SSO_REQUEST_WRITE_A_REVIEW,
                isUserBrowsing)

        } else {
            (activity as? BottomNavigationActivity)?.openWriteAReviewFragment(productName, imagePath, productId)
        }
    }


    private fun savePhoto(bitmap: Bitmap) {
        ImageResultContract.saveImageToStorage(requireContext(), bitmap)
        vtoSavedPhotoToast.showMessage(requireActivity(), getString(R.string.saved_to_photos), 250)
    }

    private fun ProductDetailsFragmentBinding.captureImageFromVtoLiveCamera() {
        try {
            vtoLayout.apply {
                viewLifecycleOwner.lifecycleScope.launch {
                    makeupCamera?.let {
                        job.cancel()
                    }
                    var countText = 3
                    while (countText >= 1) {
                        delay(DELAY_1000_MS)
                        txtCountCameraCaptureImage?.visibility = View.VISIBLE
                        txtCountCameraCaptureImage?.text = countText.toString()
                        countText--
                    }
                    isTakePicture = true
                    liveCameraViewModel?.takenPicture()
                    liveCameraViewModel?.takenPicture?.observe(
                        viewLifecycleOwner,
                        Observer { result ->
                            if (null != result?.originalPicture) {
                                takenOriginalPicture = result?.originalPicture as Bitmap
                                saveVtoApplyImage = result.resultPicture as Bitmap
                                setPickedImage(null, result.originalPicture, true)
                                imgVTOEffect?.setImageBitmap(result.resultPicture as Bitmap)
                            }
                            isPickedImageFromLiveCamera = true
                            txtCountCameraCaptureImage?.visibility = View.GONE
                            isLiveCameraResumeState = false
                            retakeCamera?.visibility = View.VISIBLE
                            imgVTOSplit?.visibility = View.GONE
                            captureImage?.visibility = View.GONE
                            imgDownloadVTO?.visibility = View.VISIBLE
                            isColorAppliedWithLiveCamera = false
                            isRefreshImageEffectLiveCamera = false
                            stopVtoLiveCamera()
                            cameraSurfaceView?.visibility = View.GONE
                        })

                }
            }
        } catch (e: Exception) {
            handleException(e)
        }
    }

    private fun stopVtoLiveCamera() {
        val cameraMonitor =
            CameraMonitor(requireActivity(), makeupCamera, lifecycle)
        cameraMonitor.stopCamera()
    }


    private fun ProductDetailsFragmentBinding.reOpenCamera() {
        if (isLiveCamera) {
            liveCameraViewHandle()
            handleLiveCamera()
            liveCameraViewModel?.liveCameraVtoApplier(
                makeupCamera, productDetails?.productId,
                getSelectedSku()?.sku
            )
            vtoLayout.apply {
                retakeCamera?.visibility = View.GONE
                imgVTORefresh?.visibility = View.VISIBLE
                imgVTOSplit?.visibility = View.VISIBLE
                captureImage?.visibility = View.VISIBLE
                noFaceDetected?.visibility = View.GONE
                imgDownloadVTO?.visibility = View.GONE
            }
            sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
            isColorAppliedWithLiveCamera = true
            isRefreshImageEffectLiveCamera = true
            isLiveCameraOpened = true
            isVtoImage = false
        } else {
            openDefaultCamera()
        }
    }

    private fun ProductDetailsFragmentBinding.compareWithLiveCamera() {
        vtoLayout.apply {
            if (comparisonView?.isCompareModeEnable() == true) {
                captureImage?.visibility = View.VISIBLE
                imgVTOSplit?.setImageResource(R.drawable.ic_vto_split_screen)
                vtoDividerLayout?.visibility = View.GONE
                imgDownloadVTO?.visibility = View.GONE
                imgVTORefresh?.visibility = View.VISIBLE
                comparisonView?.leaveComparisonMode()
                isDividerVtoEffect = false
                scrollView?.setScrollingEnabled(true)
            } else {
                captureImage?.visibility = View.GONE
                imgVTOSplit?.setImageResource(R.drawable.ic_vto_icon_compare)
                comparisonView?.enterComparisonMode()
                imgDownloadVTO?.visibility = View.GONE
                imgVTORefresh?.visibility = View.GONE
                isDividerVtoEffect = true
                scrollView?.setScrollingEnabled(false)
            }
        }
    }

    private fun ProductDetailsFragmentBinding.closeVto() {
        try {
            isColorAppliedWithLiveCamera = false
            isVtoImage = false
            isPickedImageFromLiveCamera = false
            isRefreshImageEffectLiveCamera = false
            isTakePicture = false
            isDividerVtoEffect = false
            scrollView?.setScrollingEnabled(true)
            resetColorSelectionLayout()
            vtoLayout.apply {
                comparisonView?.leaveComparisonMode()
                cameraSurfaceView?.visibility = View.GONE
                imgDownloadVTO?.visibility = View.GONE
                imgVTOSplit?.visibility = View.GONE
                imgVTORefresh?.visibility = View.GONE
                captureImage?.visibility = View.GONE
                retakeCamera?.visibility = View.GONE
                changeImage?.visibility = View.GONE
                changeImageFiles?.visibility = View.GONE
                noFaceDetected?.visibility = View.GONE
                txtCountCameraCaptureImage?.visibility = View.GONE
            }
            sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
            share?.visibility = View.VISIBLE
            productImagesViewPagerIndicator?.visibility = View.VISIBLE
            openCart.root.visibility = View.VISIBLE
            backArrow?.visibility = View.VISIBLE
            productImagesViewPager?.visibility = View.VISIBLE
            imgVTOOpen?.visibility = View.VISIBLE
            if (null != makeupCamera) {
                job?.cancel()
                stopVtoLiveCamera()
            }
            vtoLayout.root.visibility = View.GONE
        } catch (e: Exception) {
            handleException(e)
        }
    }


    private fun openCart() {
        (activity as? BottomNavigationActivity)?.navigateToTabIndex(INDEX_CART, null)
    }

    private fun onQuantitySelector() {

        if (!SessionUtilities.getInstance().isUserAuthenticated || Utils.getPreferredDeliveryLocation() == null) {
            addItemToCart()
        }

        activity?.supportFragmentManager?.apply {
            if (getSelectedSku() == null) {
                binding.requestSelectSize()
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

    @SuppressLint("ClickableViewAccessibility")
    private fun ProductDetailsFragmentBinding.configureDefaultUI() {

        updateStockAvailabilityLocation()

        productDetails?.let {
            setupBrandView()
            priceLayout.apply {
                BaseProductUtils.displayPrice(
                    fromPricePlaceHolder,
                    textPrice,
                    textActualPrice,
                    it.price,
                    it.wasPrice,
                    it.priceType,
                    it.kilogramPrice
                )
                wfsShoptimiserProduct.addProductDetailsToPdpVariant(productDetails = it)
            }

            auxiliaryImages.add(activity?.let { it1 -> getImageByWidth(it.externalImageRefV2, it1) }
                .toString())
            updateAuxiliaryImages(auxiliaryImages)
        }
        mFreeGiftPromotionalImage = productDetails?.promotionImages?.freeGift

        loadPromotionalImages()

        if (mFetchFromJson) {
            val productDetails = Utils.stringToJson(activity, defaultProductResponse)!!.product
            this@ProductDetailsFragment.onProductDetailsSuccess(productDetails)
        } else {
            //loadProductDetails.
            productDetailsPresenter?.loadProductDetails(
                ProductRequest(
                    productDetails?.productId,
                    productDetails?.sku,
                    isUserBrowsing
                )
            )
        }
        setOutOfStock()
    }

    private fun setOutOfStock() {
        AppConfigSingleton.outOfStock?.apply {
                if (stockAvailable == STOCK_AVAILABILITY_0 && isOutOfStockEnabled == true && productDetails?.productType.equals(getString(R.string.food_product_type))) {
                    binding.pdpOutOfStockTag.visibility = View.VISIBLE
                    binding.productImagesViewPager.alpha = 0.5f
                }
        }
    }

    private fun loadpayFlexWidget(amount: String?): String {
        return "<!DOCTYPE html PUBLIC><html><head><meta charset=\"UTF-8\"><meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head><body><script async src=\"https://checkout.uat.payflex.co.za/embedded/partpay-widget-0.1.4.js?type=calculator&min=10&max=2000&amount=$amount\" type=\"application/javascript\"></script></body></html>"
    }

    private fun ProductDetailsFragmentBinding.setupBrandView() {

        productDetails?.let {
            productName?.text = it.productName
            if (!it.range.isNullOrEmpty()) {
                rangeName?.visibility = View.VISIBLE
                rangeName?.text = it.range
            }
            brandName?.apply {
                if (!it.brandText.isNullOrEmpty()) {
                    text = it.brandText
                    visibility = View.VISIBLE
                }
            }

            if (ChanelUtils.isCategoryPresentInConfig(it.brandText)) {
                brandView.root.visibility = View.VISIBLE
                backArrow?.visibility = View.GONE
                openCart.root.visibility = View.GONE
                share?.visibility = View.GONE
                imgVTOOpen?.visibility = View.GONE
                if (!TextUtils.isEmpty(bannerLabel)) {
                    brandView?.brandPdpLogoHeader?.tvLogoName?.text = bannerLabel
                } else {
                    if (TextUtils.isEmpty(bannerImage)) {
                        // Apply logo image from config if not present
                        ImageManager.loadImage(
                            brandView?.brandPdpImgBanner,
                            ChanelUtils.getBrandCategory(
                                it.brandText
                            )?.externalImageRefV2 ?: ""
                        )
                    } else {
                        ImageManager.loadImage(
                            brandView?.brandPdpImgBanner,
                            bannerImage ?: ""
                        )
                    }
                }
            } else {
                brandView.root.visibility = View.GONE
                backArrow?.visibility = View.VISIBLE
                openCart.root.visibility = View.VISIBLE
                share?.visibility = View.VISIBLE
                imgVTOOpen?.visibility = View.VISIBLE
            }
        }
    }

    private fun ProductDetailsFragmentBinding.getUpdatedValidateResponse() {
        val placeId = when (KotlinUtils.browsingDeliveryType) {
            Delivery.STANDARD ->
                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
                    ?: KotlinUtils.getPreferredPlaceId()
            Delivery.CNC ->
                if (WoolworthsApplication.getCncBrowsingValidatePlaceDetails() != null)
                    WoolworthsApplication.getCncBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
            Delivery.DASH ->
                if (WoolworthsApplication.getDashBrowsingValidatePlaceDetails() != null)
                    WoolworthsApplication.getDashBrowsingValidatePlaceDetails()?.placeDetails?.placeId
                else WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
            else ->
                WoolworthsApplication.getValidatePlaceDetails()?.placeDetails?.placeId
        }

        progressBar?.visibility = View.VISIBLE
        lifecycleScope.launch {
            try {
                val validateLocationResponse =
                    placeId?.let { confirmAddressViewModel?.getValidateLocation(it) }
                progressBar?.visibility = View.GONE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            val unsellableList =
                                KotlinUtils.getUnsellableList(validateLocationResponse.validatePlace,
                                    KotlinUtils.browsingDeliveryType)
                            if (unsellableList?.isNullOrEmpty() == false && isUnSellableItemsRemoved == false) {
                                // show unsellable items
                                unsellableList?.let {
                                    navigateToUnsellableItemsFragment(it as java.util.ArrayList<UnSellableCommerceItem>,
                                        KotlinUtils.browsingDeliveryType
                                            ?: KotlinUtils.getPreferredDeliveryType()
                                            ?: Delivery.STANDARD
                                    )
                                }
                            } else
                                confirmAddressViewModel?.let {
                                    UnsellableUtils.callConfirmPlace(
                                        (this@ProductDetailsFragment),
                                        null,
                                        progressBar,
                                        it,
                                        KotlinUtils.browsingDeliveryType
                                            ?: KotlinUtils.getPreferredDeliveryType()
                                            ?: Delivery.STANDARD
                                    )
                                }
                        }
                    }
                }
            } catch (e: Exception) {
                logException(e)
                progressBar?.visibility = View.GONE
            } catch (e: JsonSyntaxException) {
                logException(e)
                progressBar?.visibility = View.GONE
            }
        }
    }

    private fun ProductDetailsFragmentBinding.callConfirmPlace() {
        // Confirm the location
        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                val confirmLocationRequest =
                    KotlinUtils.getConfirmLocationRequest(KotlinUtils.browsingDeliveryType)
                val confirmLocationResponse =
                    confirmAddressViewModel?.postConfirmAddress(confirmLocationRequest)
                progressBar?.visibility = View.GONE
                if (confirmLocationResponse != null) {
                    when (confirmLocationResponse.httpCode) {
                        HTTP_OK -> {
                            if (SessionUtilities.getInstance().isUserAuthenticated) {
                                Utils.savePreferredDeliveryLocation(ShoppingDeliveryLocation(
                                    confirmLocationResponse.orderSummary?.fulfillmentDetails))
                                if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                    KotlinUtils.clearAnonymousUserLocationDetails()
                            } else {
                                saveAnonymousUserLocationDetails(ShoppingDeliveryLocation(
                                    confirmLocationResponse.orderSummary?.fulfillmentDetails))
                            }
                            val savedPlaceId = KotlinUtils.getDeliveryType()?.address?.placeId
                            KotlinUtils.apply {
                                this.placeId = confirmLocationRequest.address.placeId
                                isLocationPlaceIdSame =
                                    confirmLocationRequest.address.placeId?.equals(
                                        savedPlaceId)
                            }
                            showSubstituteItemCell(true, substitutionProductItem)
                            setBrowsingData()
                            updateStockAvailabilityLocation() // update pdp location.
                            addItemToCart()
                        }
                    }
                }
            } catch (e: Exception) {
                logException(e)
                progressBar?.visibility = View.GONE
            }
        }
    }

    private fun setBrowsingData() {
        val browsingPlaceDetails = when (KotlinUtils.browsingDeliveryType) {
            Delivery.STANDARD -> WoolworthsApplication.getValidatePlaceDetails()
            Delivery.CNC -> WoolworthsApplication.getCncBrowsingValidatePlaceDetails()
            Delivery.DASH -> WoolworthsApplication.getDashBrowsingValidatePlaceDetails()
            else -> WoolworthsApplication.getValidatePlaceDetails()
        }
        WoolworthsApplication.setValidatedSuburbProducts(
            browsingPlaceDetails)
        // set latest response to browsing data.
        WoolworthsApplication.setCncBrowsingValidatePlaceDetails(
            browsingPlaceDetails)
        WoolworthsApplication.setDashBrowsingValidatePlaceDetails(
            browsingPlaceDetails)
    }

    private fun isUnSellableItemsRemoved() {
        ConfirmLocationResponseLiveData.observe(viewLifecycleOwner) {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true && (activity as? BottomNavigationActivity)?.mNavController?.currentFrag is ProductDetailsFragment) {
                setBrowsingData()
                updateStockAvailabilityLocation() // update pdp location.
                ConfirmLocationResponseLiveData.value = false
            }
        }

        AddToCartLiveData.observe(viewLifecycleOwner) {
            if (it) {
                AddToCartLiveData.value = false
                addItemToCart() // This will again call addToCart
            }
        }
    }

    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: ArrayList<UnSellableCommerceItem>,
        deliveryType: Delivery
    ) {
        unSellableFlowFromOnActivityResult=false
        val unsellableItemsBottomSheetDialog =
            confirmAddressViewModel?.let { it1 ->
                UnsellableItemsBottomSheetDialog.newInstance(unSellableCommerceItems, deliveryType, binding.progressBar,
                    it1, this,)
            }
        unsellableItemsBottomSheetDialog?.show(requireFragmentManager(),
            UnsellableItemsBottomSheetDialog::class.java.simpleName)
    }

    fun addItemToCart() {
        isUnSellableItemsRemoved()
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(activity,
                SSO_REQUEST_ADD_TO_CART,
                isUserBrowsing)
            return
        }

        if (Utils.getPreferredDeliveryLocation() == null) {
            activity?.apply {
                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                    this,
                    REQUEST_SUBURB_CHANGE,
                    KotlinUtils.getPreferredDeliveryType(),
                    Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                )
            }
            return
        }

        if (getSelectedSku() == null) {
            if (getSelectedGroupKey().isNullOrEmpty())
                binding.requestSelectColor()
            else
                binding.requestSelectSize()
            return
        }

        if (!Utils.retrieveStoreId(productDetails?.fulfillmentType)
                .equals(storeIdForInventory, ignoreCase = true)
        ) {
            updateStockAvailability(false)
            return
        }

        if (TextUtils.isEmpty(Utils.retrieveStoreId(productDetails?.fulfillmentType)) || getSelectedSku()?.quantity == 0) {
            //setSelectedSku(null)
            hideProgressBar()
            var message = ""
            var title = ""
            var isOutOfStockDialog = false
            when (TextUtils.isEmpty(Utils.retrieveStoreId(productDetails?.fulfillmentType))) {
                true -> {
                    title = getString(R.string.product_unavailable)
                    message = getString(
                        R.string.unavailable_item,
                        KotlinUtils.getPreferredDeliveryAddressOrStoreName()
                    )
                }
                else -> {
                    isOutOfStockDialog = true
                    title = getString(R.string.out_of_stock)
                    message =
                        getString(
                            R.string.out_of_stock_item,
                            KotlinUtils.getPreferredDeliveryAddressOrStoreName()
                        )
                }
            }
            activity?.apply {
                Utils.displayValidationMessage(
                    this,
                    CustomPopUpWindow.MODAL_LAYOUT.ERROR_TITLE_DESC,
                    title,
                    message,
                    isOutOfStockDialog
                )
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
            getSelectedSku()?.quantity?.let { selectedQuantity->
                if (KotlinUtils.getDeliveryType()?.deliveryType == Delivery.DASH.type && isEnhanceSubstitutionFeatureEnable() == true) {
                    /* for dash delivery type need to send substitution details */
                    AddItemToCart(
                        productDetails?.productId,
                        getSelectedSku()?.sku,
                        if (it > selectedQuantity) selectedQuantity else it,
                        selectionChoice,
                        substitutionId
                    )
                } else {
                    AddItemToCart(
                        productDetails?.productId,
                        getSelectedSku()?.sku,
                        if (it > selectedQuantity) selectedQuantity else it
                    )
                }
            }
        }
        val listOfItems = ArrayList<AddItemToCart>()
        item?.let { listOfItems.add(it) }
        if (listOfItems.isNotEmpty()) {
            activity?.apply {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_PDP_ADD_TO_CART,
                    this
                )
            }
            productDetailsPresenter?.postAddItemToCart(listOfItems)
        }
        setUpCartCountPDP()
    }

    override fun onSessionTokenExpired() {
        SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE)
        activity?.let { activity ->
            activity.runOnUiThread {
                ScreenManager.presentSSOSignin(activity)
            }
        }
        updateStockAvailabilityLocation()
    }

    override fun onProductDetailsSuccess(productDetails: ProductDetails) {
        if (!isAdded || productDetails == null) return

        this.productDetails = productDetails
        callFirebaseEvents()
        otherSKUsByGroupKey = this.productDetails?.otherSkus.let { groupOtherSKUsByColor(it) }
        this.defaultSku = getDefaultSku(otherSKUsByGroupKey)

        if (productDetails?.isLiquor == true && !KotlinUtils.isCurrentSuburbDeliversLiquor() && !KotlinUtils.isLiquorModalShown()) {
            KotlinUtils.setLiquorModalShown()
            showLiquorDialog()
        }

        if ((!hasColor && !hasSize)) {
            setSelectedSku(this.defaultSku)
            updateAddToCartButtonForSelectedSKU()
            AppConfigSingleton.dynamicYieldConfig?.apply {
                if (isDynamicYieldEnabled == true) {
                    prepareDyChangeAttributeQuantityRequestEvent(
                        defaultSku?.quantity.toString(),
                        defaultSku?.sku
                    )
                }
            }
        } else {
            AppConfigSingleton.dynamicYieldConfig?.apply {
                if (isDynamicYieldEnabled == true) {
                    var color = defaultSku?.colour
                    prepareDyChangeAttributeRequestEvent(color, defaultSku?.sku)
                }
            }
        }

        binding.setupBrandView()
        //Added the BNPL flag checking logic.
        wfsShoptimiserProduct.apply {
            addProductDetails(productDetails = productDetails)
            shoptimiserViewModel.initWfsEmbeddedFinance()
        }

        if (hasSize)
            setSelectedGroupKey(defaultGroupKey)

        Utils.getPreferredDeliveryLocation()?.let {
            updateDefaultUI(false)
            if (!this.productDetails?.productType.equals(
                    getString(R.string.food_product_type),
                    ignoreCase = true
                ) && (KotlinUtils.getPreferredDeliveryType() == Delivery.DASH)
            ) {
                showProductUnavailable()
                showProductNotAvailableForCollection()
                return
            } else if(KotlinUtils.getPreferredDeliveryType() == Delivery.CNC) {
                //Food only
                if(this.productDetails?.fulfillmentType == StoreUtils.Companion.FulfillmentType.FOOD_ITEMS?.type && Utils.retrieveStoreId(this.productDetails?.fulfillmentType) == "") {
                    showProductUnavailable()
                    foodProductNotAvailableForCollection()
                    return
                }  //FBH only
                else if((this.productDetails?.fulfillmentType == StoreUtils.Companion.FulfillmentType.CLOTHING_ITEMS?.type || this.productDetails?.fulfillmentType == StoreUtils.Companion.FulfillmentType.CRG_ITEMS?.type) &&
                    (Utils.retrieveStoreId(this.productDetails?.fulfillmentType) == "")) {
                    showProductUnavailable()
                    showProductNotAvailableForCollection()
                    return
                }
            }
        }

        if (!this.productDetails?.otherSkus.isNullOrEmpty()) {
            //If user is not signed in or User doesn't have any location set then don't make inventory
            if (!SessionUtilities.getInstance().isUserAuthenticated || Utils.getPreferredDeliveryLocation() == null) {
                updateDefaultUI(false)
                hideProductDetailsLoading()
                prepareDynamicYieldPageViewRequestEvent()
                return
            }

            storeIdForInventory =
                Utils.retrieveStoreId(productDetails?.fulfillmentType)

            when (storeIdForInventory.isNullOrEmpty()) {
                true -> showProductUnavailable()
                false -> {
                    showProductDetailsLoading()
                    val multiSKUs =
                        productDetails?.otherSkus?.joinToString(separator = "-") { it.sku.toString() }
                            ?: ""
                    productDetailsPresenter?.loadStockAvailability(
                        storeIdForInventory!!,
                        multiSKUs,
                        true,
                        isUserBrowsing
                    )
                }
            }

        } else if (productDetails?.otherSkus.isNullOrEmpty()) {
            productOutOfStockErrorMessage()
        } else {
            hideSubstitutionLayout()
            showErrorWhileLoadingProductDetails()
        }
        sendRecommendationsDetail()
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true)
                prepareDynamicYieldPageViewRequestEvent()
        }
    }

    private fun sendRecommendationsDetail() {
        val bundle = Bundle()
        val productX = ProductX(productDetails?.productId.toString())
        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA, Event(
                eventType = "monetate:context:PageView", url = "/pdp", pageType = "pdp", categories = listOf(), null, null
            )
        )

        bundle.putParcelable(
            BundleKeysConstants.RECOMMENDATIONS_EVENT_DATA_TYPE, Event(eventType = "monetate:context:ProductDetailView", null, null, null,
                products = listOf(productX), cartLines = listOf()
            )
        )

        val navHostFragment =
            childFragmentManager.findFragmentById(R.id.navHostRecommendation) as NavHostFragment
        val navController = navHostFragment?.navController
        val navGraph = navController?.navInflater?.inflate(R.navigation.nav_recommendation_graph)

        navGraph?.startDestination = R.id.recommendationFragment
        navGraph?.let {
            navController?.graph = it
        }
        navGraph?.let {
            navController?.setGraph(
                it, bundleOf("bundle" to bundle)
            )
        }
    }

    override fun onProductDetailedFailed(response: Response, httpCode: Int) {
        if (httpCode == HTTP_CODE_502) {
            isOutOfStock_502 = true
            val message = getString(R.string.out_of_stock_502)
            OutOfStockMessageDialogFragment.newInstance(message).show(
                this@ProductDetailsFragment.childFragmentManager,
                OutOfStockMessageDialogFragment::class.java.simpleName
            )
        } else if (isAdded) {
            isOutOfStock_502 = false
            hideSubstitutionLayout()
            showErrorWhileLoadingProductDetails()
        }
    }

    override fun onFailureResponse(error: String) {
    }

    override fun onStockAvailabilitySuccess(
        skusInventoryForStoreResponse: SkusInventoryForStoreResponse,
        isDefaultRequest: Boolean,
    ) {

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
            val imageLink: String? = imageUrl
            val deviceHeight = this.defaultDisplay
            val size = Point()
            deviceHeight.getSize(size)
            val width = size.x
            return imageLink + "" + if (imageLink!!.contains("jpg")) "" else "?w=$width&q=85"
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
            binding?.showColors()
        else {
            binding?.sizeColorSelectorLayout?.apply {
                colorSelectorLayout.visibility = View.GONE
                divider1.visibility = View.GONE
            }
        }
        if (hasSize)
            showSize()
        else {
            binding?.sizeColorSelectorLayout?.apply {
                sizeSelectorLayout.visibility = View.GONE
                divider2.visibility = View.GONE
            }
        }

        if (productDetailsPresenter?.isSizeGuideApplicable(
                productDetails?.colourSizeVariants,
                productDetails?.sizeGuideId
            ) == true
        ) {
            binding.sizeColorSelectorLayout.sizeGuide?.apply {
                underline()
                visibility = View.VISIBLE
            }
        }

    }

    private fun ProductDetailsFragmentBinding.showColors() {
        val spanCount = Utils.calculateNoOfColumns(activity, 50F)
        sizeColorSelectorLayout.apply {
            colorSelectorRecycleView.layoutManager = GridLayoutManager(activity, spanCount)
            if (otherSKUsByGroupKey.size == 1 && !hasSize) {
                onColorSelection(this@ProductDetailsFragment.defaultGroupKey, true)
            }
            productColorSelectorAdapter = ProductColorSelectorAdapter(
                otherSKUsByGroupKey,
                this@ProductDetailsFragment,
                spanCount,
                getSelectedGroupKey()
            ).apply {
                colorSelectorRecycleView.adapter = this
                showSelectedColor()
            }

            otherSKUsByGroupKey.size.let {
                if (it > spanCount) {
                    val moreColorCount = otherSKUsByGroupKey.size - spanCount
                    moreColor?.text =
                        requireContext().getString(R.string.product_details_color_count,
                            moreColorCount)
                    moreColor?.visibility = View.VISIBLE
                }
            }

            colorSelectorLayout?.visibility = View.VISIBLE
            divider1.visibility = View.VISIBLE
        }
    }

    private fun showSize() {
        if(!isAdded || binding == null) {
            return
        }
        binding.sizeColorSelectorLayout.apply {
            productSizeSelectorAdapter = ProductSizeSelectorAdapter(
                requireActivity(),
                otherSKUsByGroupKey[getSelectedGroupKey()] ?: ArrayList(0),
                productDetails?.lowStockIndicator ?: 0,
                this@ProductDetailsFragment
            )
            sizeSelectorRecycleView?.apply {
                adapter = productSizeSelectorAdapter
                layoutManager = GridLayoutManager(activity, 4)
            }

            otherSKUsByGroupKey[getSelectedGroupKey()]?.let {
                if (it.size == 1) {
                    productSizeSelectorAdapter?.setSelection(it[0])
                    onSizeSelection(it[0])
                }
            }

            sizeSelectorLayout?.visibility = View.VISIBLE
        }
    }

    private fun groupOtherSKUsByColor(otherSKUsList: ArrayList<OtherSkus>?): LinkedHashMap<String, ArrayList<OtherSkus>> {

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
            else -> {}
        }

        if (otherSKUsList != null) {
            for (otherSkuObj in otherSKUsList) {
                var groupKey =
                    if (TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(otherSkuObj.size)) {
                        otherSkuObj.size?.trim()
                    } else if (!TextUtils.isEmpty(otherSkuObj.colour) && !TextUtils.isEmpty(
                            otherSkuObj.size)
                    ) {
                        otherSkuObj.colour?.trim()
                    } else {
                        otherSkuObj.colour?.trim()
                    }

                if (variant == ColourSizeVariants.NO_COLOUR_SIZE_VARIANT) {
                    otherSkuObj.apply { size = colour }
                    groupKey = "N/A"
                }

                if (!otherSKUsByGroupKey.containsKey(groupKey) && !groupKey.isNullOrEmpty()) {
                    this.otherSKUsByGroupKey[groupKey] = ArrayList()
                }
                if (!otherSKUsByGroupKey[groupKey]!!.any { it.sku == otherSkuObj.sku }) this.otherSKUsByGroupKey[groupKey]!!.add(
                    otherSkuObj
                )
            }
        }
        return otherSKUsByGroupKey
    }

    private fun callFirebaseEvents() {
        productDetails?.let { details ->
            FirebaseAnalyticsEventHelper.viewItem(details)
            productDetails?.promotionsList?.let { promoList ->
                FirebaseAnalyticsEventHelper.viewPromotion(details, promoList)
            }
        }
    }

    override fun updateDefaultUI(isInventoryCalled: Boolean) {
        binding?.apply {
            loadSizeAndColor()
            loadPromotionalImages()
            updateAuxiliaryImages(getAuxiliaryImagesByGroupKey())
            productDetailOptionsAndInformation.apply {
                if (!TextUtils.isEmpty(this@ProductDetailsFragment.productDetails?.ingredients))
                    productIngredientsInformation?.visibility = View.VISIBLE
                if (this@ProductDetailsFragment.productDetails?.nutritionalInformationDetails != null)
                    nutritionalInformation?.visibility = View.VISIBLE
                if (!this@ProductDetailsFragment.productDetails?.dietary.isNullOrEmpty())
                    dietaryInformation?.visibility = View.VISIBLE
                if (!this@ProductDetailsFragment.productDetails?.allergens.isNullOrEmpty())
                    allergensInformation?.visibility = View.VISIBLE
            }


            productDetails?.let {
                priceLayout.apply {
                    BaseProductUtils.displayPrice(
                        fromPricePlaceHolder,
                        textPrice,
                        textActualPrice,
                        it.price,
                        it.wasPrice,
                        it.priceType,
                        it.kilogramPrice
                    )
                }
                brandName?.apply {
                    if (!it.brandText.isNullOrEmpty()) {
                        text = it.brandText
                        visibility = View.VISIBLE
                    }
                }
                if (!it.freeGiftText.isNullOrEmpty()) {
                    freeGiftWithPurchaseLayout.root.visibility = View.VISIBLE
                    if(it.productType==AppConstant.PRODUCT_TYPE_DIGITAL && !it.network.isNullOrEmpty()){
                        freeGiftWithPurchaseLayout.freeSim.visibility = View.VISIBLE
                        freeGiftWithPurchaseLayout.giftPurchase.visibility = View.GONE
                        freeGiftWithPurchaseLayout.freeSimTitle.text = getString(R.string.free_sim_with_purchase,it.network)
                        freeGiftWithPurchaseLayout.freeSimDesc.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(it.freeGiftText, Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            Html.fromHtml(it.freeGiftText)
                        }
                        freeGiftWithPurchaseLayout.root.visibility = View.VISIBLE
                        freeGiftWithPurchaseLayout.downArrow.setOnClickListener {
                            if(freeGiftWithPurchaseLayout.freeSimDesc.visibility == View.VISIBLE) {
                                freeGiftWithPurchaseLayout.freeSimDesc.visibility = View.GONE
                                freeGiftWithPurchaseLayout.downArrow.rotation = 0.0f
                            }else{
                                freeGiftWithPurchaseLayout.freeSimDesc.visibility = View.VISIBLE
                                freeGiftWithPurchaseLayout.downArrow.rotation = 180.0f
                            }
                        }
                    }
                    else {
                        freeGiftWithPurchaseLayout.freeSim.visibility = View.GONE
                        freeGiftWithPurchaseLayout.giftPurchase.visibility = View.VISIBLE
                        freeGiftWithPurchaseLayout.freeGiftText.text = it.freeGiftText
                        freeGiftWithPurchaseLayout.freeGiftText.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            Html.fromHtml(it.freeGiftText, Html.FROM_HTML_MODE_COMPACT)
                        } else {
                            Html.fromHtml(it.freeGiftText)
                        }

                    }
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
                                onlinePromotionalTextView1?.text =
                                    Html.fromHtml(editedPromotionalText)
                            }
                            1 -> {
                                onlinePromotionalTextView2?.visibility = View.VISIBLE
                                onlinePromotionalTextView2?.text =
                                    Html.fromHtml(editedPromotionalText)
                            }
                            2 -> {
                                onlinePromotionalTextView3?.visibility = View.VISIBLE
                                onlinePromotionalTextView3?.text =
                                    Html.fromHtml(editedPromotionalText)
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
                if (true == it.isRnREnabled && RatingAndReviewUtil.isRatingAndReviewConfigavailbel() ) {
                    ratingLayout.apply {
                        ratingBarTop?.rating = it.averageRating
                        tvTotalReviews?.text = resources.getQuantityString(
                            R.plurals.no_review,
                            it.reviewCount,
                            it.reviewCount
                        )
                        ratingBarTop?.visibility = View.VISIBLE
                        tvTotalReviews?.visibility = View.VISIBLE
                        prodId = it.productId
                        tvTotalReviews?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                    }
                    if (RatingAndReviewUtil.isFoodItemAvailable() ||
                        RatingAndReviewUtil.isFashionItemAvailable() ||
                        RatingAndReviewUtil.isHomeItemAvailable() ||
                        RatingAndReviewUtil.isBeautyItemAvailable()) ShowWriteAReview() else  hideWriteAReview()
                } else {
                    hideRatingAndReview()
                    hideWriteAReview()
                }
            }

            if (!isAllProductsOutOfStock() && isInventoryCalled) {
                showEnhancedSubstitutionDialog()
            }
            showSubstituteItemCell(isInventoryCalled, substitutionProductItem)

            if (isAllProductsOutOfStock() && isInventoryCalled) {
                productOutOfStockErrorMessage()
                return
            }
        }
    }

    private fun callGetSubstitutionApi(isInventoryCalled: Boolean) {

        if (!SessionUtilities.getInstance().isUserAuthenticated || (isAllProductsOutOfStock() && isInventoryCalled) || isEnhanceSubstitutionFeatureEnable() == false) {
            return
        }

        productSubstitutionViewModel.getProductSubstitution(productDetails?.productId)
        productSubstitutionViewModel.productSubstitution.observe(viewLifecycleOwner) {

            it.getContentIfNotHandled()?.let { resource ->
                when (resource.status) {
                    Status.LOADING -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                    Status.SUCCESS -> {
                        binding.progressBar.visibility = View.GONE
                        showSubstitutionLayout(isInventoryCalled, resource)
                    }
                    Status.ERROR -> {
                        binding.progressBar.visibility = View.GONE
                        hideSubstitutionLayout()
                    }
                }
            }
        }
    }

    private fun showSubstituteItemCell(
        isInventoryCalled: Boolean,
        substitutionProductItem: ProductList? = null
    ) {
        if ((KotlinUtils.getDeliveryType()?.deliveryType != Delivery.DASH.type || isEnhanceSubstitutionFeatureEnable() == false)
            || (productDetails?.fulfillmentType != getString(R.string.fullfilment_type_01) && productDetails?.productType !=getString(R.string.food_product_type))
        ) {
            binding.productDetailOptionsAndInformation.substitutionLayout.root?.visibility = View.GONE
            return
        }

        binding.productDetailOptionsAndInformation.substitutionLayout.apply {
            root.visibility = View.VISIBLE
            txtSubstitutionEdit.setOnClickListener(this@ProductDetailsFragment)
            if (SessionUtilities.getInstance().isUserAuthenticated) {
                if (substitutionProductItem == null) {
                    callGetSubstitutionApi(isInventoryCalled)
                } else {
                    /*set Locally product name */
                    selectionChoice = SubstitutionChoice.USER_CHOICE.name
                    substitutionId = substitutionProductItem.productId
                    txtSubstitutionTitle.text = substitutionProductItem.productName
                    txtSubstitutionEdit.text = context?.getString(R.string.change)
                }
            } else {
                txtSubstitutionTitle.text = context?.getString(R.string.sign_in_label)
                txtSubstitutionEdit.text = context?.getString(R.string.sign_in)
            }
        }
    }

    fun updateItemCellForEnhanceSubstitution(title: String?,  substitutionChoice:String) {
        binding.productDetailOptionsAndInformation.substitutionLayout.apply {
            txtSubstitutionTitle.text = title
            txtSubstitutionEdit.text = context?.getString(R.string.change)
            selectionChoice = substitutionChoice
            substitutionId = ""
        }
    }

    private fun showSubstitutionLayout(
        isInventoryCalled: Boolean,
        resource: Resource<ProductSubstitution>
    ) {

        binding?.productDetailOptionsAndInformation?.substitutionLayout?.apply {
            if (resource?.data?.data?.isNullOrEmpty() == true) {
                hideSubstitutionLayout()
                return
            }

            if (isAllProductsOutOfStock() && isInventoryCalled) {
                this.txtSubstitutionEdit?.background = resources.getDrawable(
                    R.drawable.grey_background_with_corner_5,
                    null
                )
            } else {
                this.txtSubstitutionEdit?.background = resources.getDrawable(
                    R.drawable.black_background_with_corner_5,
                    null
                )
            }

            if (resource.data?.data?.getOrNull(0)?.substitutionSelection == SubstitutionChoice.USER_CHOICE.name) {
                txtSubstitutionTitle.text =
                    resource.data?.data?.getOrNull(0)?.substitutionInfo?.displayName
                selectionChoice = SubstitutionChoice.USER_CHOICE.name
                substitutionId =  resource.data?.data?.getOrNull(0)?.substitutionInfo?.id
            } else if (resource.data?.data?.getOrNull(0)?.substitutionSelection == SubstitutionChoice.NO.name) {
                txtSubstitutionTitle.text = getString(R.string.dont_substitute)
                selectionChoice = SubstitutionChoice.NO.name
                substitutionId = ""
            } else {
                txtSubstitutionTitle.text = getString(R.string.substitute_default)
                selectionChoice = SubstitutionChoice.SHOPPER_CHOICE.name
                substitutionId = ""
            }
            txtSubstitutionEdit?.text = getString(R.string.change)
        }
    }

    private fun openManageSubstitutionFragment(substiutionSelection: String?)  =
        ManageSubstitutionFragment.newInstance(substiutionSelection, commarceItemId, prodId, getSelectedSku()?.sku)


    private fun hideSubstitutionLayout() {
        binding?.productDetailOptionsAndInformation?.substitutionLayout?.root?.visibility = View.GONE
    }

    private fun ProductDetailsFragmentBinding.hideWriteAReview() {
        productDetailOptionsAndInformation.apply {
            leaveUsReview?.visibility = View.GONE
            writeAReviewLink.root.visibility = View.GONE
        }
    }

    private fun ProductDetailsFragmentBinding.ShowWriteAReview() {
        productDetailOptionsAndInformation.apply {
            leaveUsReview?.visibility = View.VISIBLE
            writeAReviewLink.root.visibility = View.VISIBLE
        }
    }

    private fun ProductDetailsFragmentBinding.hideRatingAndReview() {
        productDetailOptionsAndInformation.apply {
            headerCustomerReview?.visibility = View.GONE
            reviewDetailsInformation?.visibility = View.GONE
            customerReview.root.visibility = View.GONE
            rlViewMoreReview?.visibility = View.GONE
        }
    }

    private fun ProductDetailsFragmentBinding.showRatingAndReview() {
        productDetailOptionsAndInformation.apply {
            headerCustomerReview?.visibility = View.VISIBLE
            reviewDetailsInformation?.visibility = View.VISIBLE
            customerReview.root.visibility = View.VISIBLE
            rlViewMoreReview?.visibility = View.VISIBLE
        }
    }

    private fun ProductDetailsFragmentBinding.setReviewUI(ratingNReviewResponse: RatingReviewResponse) {
        ratingNReviewResponse.apply {
            reviewStatistics.apply {
                productDetailOptionsAndInformation.customerReview.apply {
                    ratingBar?.rating = averageRating
                    ratingLayout.ratingBarTop?.rating = averageRating
                    productDetailOptionsAndInformation.apply {
                        tvCustomerReviewCount?.text = resources.getQuantityString(
                            R.plurals.customer_review,
                            reviewCount,
                            reviewCount
                        )
                        val recommend = recommendedPercentage.split("%")
                        if (recommend.size == 2) {
                            tvRecommendPer.text = "${recommend[0]}% "
                            tvRecommendTxt.text = recommend[1]
                        }
                        if (reviewCount > 1)
                            btViewMoreReview?.text = resources.getQuantityString(
                                R.plurals.more_review,
                                (reviewCount - 1),
                                (reviewCount - 1)
                            )
                        else {
                            btViewMoreReview?.visibility = View.GONE
                        }
                    }
                    ratingLayout.tvTotalReviews?.text =
                        resources.getQuantityString(
                            R.plurals.no_review,
                            reviewCount,
                            reviewCount
                        )
                }
            }
            productDetailOptionsAndInformation.customerReview.apply {
                reviewHelpfulReport.tvReport?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                tvSkinProfile?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                productDetailOptionsAndInformation.tvRatingDetails?.paintFlags =
                    Paint.UNDERLINE_TEXT_FLAG
                if (reviews?.isNotEmpty() == true) {
                    reviews[0].apply {
                        tvName?.text = userNickname
                        if (isVerifiedBuyer)
                            tvVerifiedBuyer?.visibility = View.VISIBLE
                        else
                            tvVerifiedBuyer?.visibility = View.GONE
                        if (isStaffMember)
                            tvVerifiedStaffMember?.visibility = View.VISIBLE
                        else
                            tvVerifiedStaffMember?.visibility = View.GONE
                        ratingBar?.rating = rating
                        tvReviewHeading?.text = title
                        tvCustomerReview?.text = reviewText
                        tvReviewPostedOn?.text = syndicatedSource
                        tvDate?.text = submissionTime
                        productDetailOptionsAndInformation.customerReview.reviewHelpfulReport.tvLikes?.text =
                            totalPositiveFeedbackCount.toString()
                        setReviewAdditionalFields(additionalFields)
                        setSecondaryRatingsUI(secondaryRatings)
                        setReviewThumbnailUI(photos.thumbnails)
                        if (contextDataValue.isEmpty() && tagDimensions.isEmpty()) {
                            tvSkinProfile.visibility = View.GONE
                        }
                        if (RatingAndReviewUtil.likedReviews.contains(id.toString())) {
                            reviewHelpfulReport.ivLike.setImageResource(R.drawable.iv_like_selected)
                        }

                        if (RatingAndReviewUtil.reportedReviews.contains(id.toString())) {
                            productDetailOptionsAndInformation.customerReview.reviewHelpfulReport.apply {
                                tvReport.setTextColor(Color.RED)
                                tvReport.setText(resources.getString(R.string.reported))
                                tvReport?.setTypeface(tvReport.typeface, Typeface.BOLD)
                                tvReport.paintFlags = Paint.UNDERLINE_TEXT_FLAG
                            }
                        }
                    }
                } else {
                    productDetailOptionsAndInformation.apply {
                        customerReview.root.visibility = View.GONE
                        tvRatingDetails.visibility = View.GONE
                    }
                }
            }
        }

        productDetailOptionsAndInformation.customerReview.linearLayoutCustomerReview?.setOnClickListener {
            sendReviewDataToReviewDetailScreen(ratingNReviewResponse)
        }
    }

    private fun sendReviewDataToReviewDetailScreen(ratingNReviewResponse: RatingReviewResponse) {
        ScreenManager.presentReviewDetail(requireActivity(), ratingNReviewResponse)
    }

    private fun setReviewAdditionalFields(additionalFields: List<AdditionalFields>) {
        for (additionalField in additionalFields) {
            val rootView = LinearLayout(context)
            rootView.layoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            rootView.orientation = LinearLayout.HORIZONTAL

            val tvAdditionalFieldLabel = TextView(context)
            tvAdditionalFieldLabel.alpha = 0.5F
            val tvAdditionalFieldValue = TextView(context)
            tvAdditionalFieldValue.alpha = 0.5F
            val ivCircle = ImageView(context)
            val tvParam: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            tvParam.setMargins(25, 0, 0, 8)
            tvAdditionalFieldValue.layoutParams = tvParam
            val ivParam: LinearLayout.LayoutParams =
                LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            ivParam.setMargins(25, 15, 0, 0)
            ivCircle.layoutParams = ivParam
            if (Build.VERSION.SDK_INT < 23) {
                tvAdditionalFieldLabel.setTextAppearance(
                    getApplicationContext(),
                    R.style.opensans_regular_13_black
                );
                tvAdditionalFieldValue.setTextAppearance(
                    getApplicationContext(),
                    R.style.opensans_regular_13_black
                );
            } else {
                tvAdditionalFieldLabel.setTextAppearance(R.style.opensans_regular_13_black);
                tvAdditionalFieldValue.setTextAppearance(R.style.opensans_semi_bold_13_text_style);
            }
            tvAdditionalFieldLabel.text = additionalField.label
            ivCircle.setImageResource(R.drawable.ic_circle)
            tvAdditionalFieldValue.text = additionalField.valueLabel

            rootView.addView(tvAdditionalFieldLabel)
            rootView.addView(ivCircle)
            rootView.addView(tvAdditionalFieldValue)
            binding.productDetailOptionsAndInformation.customerReview.llAdditionalFields?.addView(
                rootView)
        }
    }

    private fun ProductDetailsFragmentBinding.setSecondaryRatingsUI(secondaryRatings: List<SecondaryRatings>) {
        productDetailOptionsAndInformation.customerReview.apply {
            rvSecondaryRatings.layoutManager = GridLayoutManager(requireContext(), 2)
            secondaryRatingAdapter = SecondaryRatingAdapter()
            rvSecondaryRatings.adapter = secondaryRatingAdapter
            secondaryRatingAdapter.setDataList(secondaryRatings)
        }
    }

    private fun ProductDetailsFragmentBinding.setReviewThumbnailUI(thumbnails: List<Thumbnails>) {
        productDetailOptionsAndInformation.customerReview.apply {
            rvThumbnail?.layoutManager = GridLayoutManager(requireContext(), 3)
            reviewThumbnailAdapter =
                ReviewThumbnailAdapter(requireContext(), this@ProductDetailsFragment)
            rvThumbnail?.adapter = reviewThumbnailAdapter
            thumbnailFullList = thumbnails
            if (thumbnails.size > 2) {
                reviewThumbnailAdapter.setDataList(thumbnailFullList.subList(0, 2))
            } else {
                reviewThumbnailAdapter.setDataList(thumbnailFullList)
            }
        }
    }

    override fun thumbnailClicked() {
        reviewThumbnailAdapter.setDataList(thumbnailFullList)
        reviewThumbnailAdapter.notifyDataSetChanged()
    }

    private fun ProductDetailsFragmentBinding.setBrandText(it: ProductDetails) {
        brandName.apply {
            if (!it.brandText.isNullOrEmpty()) {
                text = it.brandText
                visibility = View.VISIBLE
            }
        }
    }

    private fun getDefaultSku(otherSKUsList: HashMap<String, ArrayList<OtherSkus>>): OtherSkus? {
        otherSKUsList?.keys?.forEach { key ->
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
        context?.let {
            ProductViewPagerAdapter(it, imagesList, this@ProductDetailsFragment).apply {
                binding.productImagesViewPager?.let { pager ->
                    pager.adapter = this
                    binding.productImagesViewPagerIndicator.setViewPager(pager)
                }
            }
        }
    }

    override fun onSizeSelection(selectedSku: OtherSkus) {
        setSelectedSku(selectedSku)
        var size: String? = selectedSku.size
        binding.showSelectedSize(selectedSku)
        binding.updateUIForSelectedSKU(getSelectedSku())
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true)
                prepareDyChangeAttributeSizeRequestEvent(size, selectedSku.sku)
        }
    }

    private fun prepareDyChangeAttributeSizeRequestEvent(size: String?, sku: String?) {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress,config?.getDeviceModel())
        val context = Context(device,null,DY_CHANNEL)
        val properties = Properties(SIZE_ATTRIBUTE, size,CHANGE_ATTRIBUTE_DY_TYPE,null,null,null,null,null,null,sku,null,null,null,null,null,null,null,null)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,CHANGE_ATTRIBUTE,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareChangeAttributeRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareChangeAttributeRequestEvent)
    }

    override fun onColorSelection(selectedColor: String?, isFeature: Boolean) {
        setSelectedGroupKey(selectedColor)
        binding.showSelectedColor()
        if (hasSize) updateSizesOnColorSelection() else {
            setSelectedSku(otherSKUsByGroupKey[getSelectedGroupKey()]?.get(0))
            binding.updateUIForSelectedSKU(getSelectedSku())
        }
        updateAuxiliaryImages(getAuxiliaryImagesByGroupKey())

        if (!isFeature && isColorAppliedWithLiveCamera) {
            applyEffectOnLiveCamera()
        }
        if (!isFeature && isVtoImage) {
            applyVtoEffectOnImage()
        }
        if (productDetails?.lowStockIndicator ?: 0 > getSelectedSku()?.quantity ?: 0
            && !hasSize && getSelectedSku()?.quantity!! > 0 && AppConfigSingleton.lowStock?.isEnabled == true
        ) {
            binding.showLowStockForSelectedColor()
            binding.sizeColorSelectorLayout.colorPlaceholder?.text = ""
        } else {
            binding.hideLowStockFromSelectedColor()

        }
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true)
                prepareDyChangeAttributeRequestEvent(selectedColor, selectedSku?.sku)
        }
    }

    private fun prepareDyChangeAttributeRequestEvent(selectedColor: String?, sku: String?) {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress,config?.getDeviceModel())
        val context = Context(device,null,DY_CHANNEL)
        val properties = Properties(COLOR_ATTRIBUTE,selectedColor,CHANGE_ATTRIBUTE_DY_TYPE,null,null,null,null,null,null,sku,null,null,null,null,null,null,null,null)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,CHANGE_ATTRIBUTE,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute)
        val prepareChangeAttributeRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareChangeAttributeRequestEvent)
    }

    private fun applyEffectOnLiveCamera() {
        liveCameraViewModel?.applyVtoEffectOnLiveCamera(
            productDetails?.productId,
            getSelectedSku()?.sku
        )
        liveCameraViewModel?.selectedSkuResult?.observe(
            viewLifecycleOwner,
            Observer { result ->
                binding.applyColorVtoMappedResult(result)
            })
    }

    private fun applyVtoEffectOnImage() {
        vtoApplyEffectOnImageViewModel?.applyEffect(
            productDetails?.productId,
            getSelectedSku()?.sku
        )
        binding.getApplyResultSelectColor()

    }

    private fun ProductDetailsFragmentBinding.getApplyResultSelectColor() {

        vtoApplyEffectOnImageViewModel?.applyEffectImage?.observe(
            viewLifecycleOwner,
            Observer { result ->
                when {
                    result.equals(VTO_COLOR_NOT_MATCH) -> {
                        sizeColorSelectorLayout.colourUnavailableError?.visibility = View.VISIBLE
                        vtoLayout.apply {
                            imgVTORefresh?.visibility = View.GONE
                            imgDownloadVTO?.visibility = View.GONE
                            if (isPickedImageFromLiveCamera) {
                                imgVTOEffect?.setImageBitmap(takenOriginalPicture)
                            } else {
                                setBitmapFromUri(selectedImageUri)
                            }
                        }
                    }
                    null != result -> {
                        sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
                        vtoLayout.apply {
                            imgVTORefresh?.visibility = View.VISIBLE
                            imgDownloadVTO?.visibility = View.VISIBLE
                            if (!result.equals("")) {
                                imgVTOEffect?.setImageBitmap(result as Bitmap?)
                                saveVtoApplyImage = result as Bitmap?
                            }
                        }
                    }
                    else -> {
                        sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
                        vtoLayout.apply {
                            imgVTORefresh?.visibility = View.GONE
                            imgDownloadVTO?.visibility = View.GONE
                            if (isPickedImageFromLiveCamera) {
                                imgVTOEffect?.setImageBitmap(takenOriginalPicture)
                            } else {
                                setBitmapFromUri(uri)
                            }
                        }
                    }
                }
            })

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
                    if (getSelectedSku() == null) binding.updateUIForSelectedSKU(defaultSku) else binding.updateUIForSelectedSKU(
                        getSelectedSku()
                    )
                }
                else -> {
                    setSelectedSku(otherSKUsByGroupKey[getSelectedGroupKey()]?.get(index))
                    productSizeSelectorAdapter?.setSelection(getSelectedSku())
                    binding.updateUIForSelectedSKU(getSelectedSku())
                }
            }
            binding.showSelectedSize(selectedSku)

        }

    }

    private fun updateAddToCartButtonForSelectedSKU() {

        if (!SessionUtilities.getInstance().isUserAuthenticated || Utils.getPreferredDeliveryLocation() == null) {
            binding.showAddToCart()
            return
        }

        when (getSelectedSku()) {
            null -> binding.showAddToCart()
            else -> {
                getSelectedSku()?.quantity?.let {
                    when (it) {
                        0, -1 -> binding.showFindInStore()
                        else -> {
                            getSelectedQuantity()?.apply {
                                if (it < this)
                                    onQuantitySelection(1)
                            }
                            binding.showAddToCart()
                        }
                    }
                }
            }
        }

    }

    private fun prepareDyAddToCartRequestEvent() {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress,config?.getDeviceModel())
        val context = Context(device,null,DY_CHANNEL)
        val cartLinesValue: MutableList<Cart> = arrayListOf()
        val cart = Cart(getSelectedSku()?.sku, getSelectedQuantity(), getSelectedSku()?.price?.toString())
        cartLinesValue.add(cart)
        val properties = Properties(null,null,ADD_TO_CART_V1,null,getSelectedSku()?.price,ZAR,selectedQuantity,getSelectedSku()?.sku,getSelectedSku()?.colour,null,null,null,null,null,null,null,null,cartLinesValue)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,ADD_TO_CART,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareDyAddToCartRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareDyAddToCartRequestEvent)
    }

    private fun ProductDetailsFragmentBinding.showFindInStore() {
        productDetails?.isnAvailable?.toBoolean()?.apply {
            if (!this) {
                toCartAndFindInStoreLayout.root.visibility = View.GONE
                productDetailOptionsAndInformation.checkInStoreAvailability?.visibility = View.GONE
                return
            }
        }

        toCartAndFindInStoreLayout.root.visibility = View.VISIBLE
        toCartAndFindInStoreLayout.apply {
            groupAddToCartAction?.visibility = View.GONE
            findInStoreAction?.visibility = View.VISIBLE
        }
        if (hasColor) hideLowStockFromSelectedColor()
        if (hasSize) hideLowStockForSize()
    }

    private fun ProductDetailsFragmentBinding.showAddToCart() {
        toCartAndFindInStoreLayout.root.visibility = View.VISIBLE
        toCartAndFindInStoreLayout.apply {
            groupAddToCartAction?.visibility = View.VISIBLE
            findInStoreAction?.visibility = View.GONE
        }
        if (isAllProductsOutOfStock() && SessionUtilities.getInstance().isUserAuthenticated && Utils.getPreferredDeliveryLocation() != null) {
            showFindInStore()
        }
    }

    private fun ProductDetailsFragmentBinding.updateUIForSelectedSKU(otherSku: OtherSkus?) {
        otherSku?.let {
            priceLayout.apply {
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
            wfsShoptimiserProduct.addPrice(otherSkus = otherSku)
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
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_PDP_SELECT_QUANTITY,
                this
            )
        }
        setSelectedQuantity(quantity)
        binding.toCartAndFindInStoreLayout.quantityText?.text = quantity.toString()
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true)
                prepareDyChangeAttributeQuantityRequestEvent(quantity.toString(), selectedSku?.sku)
        }
    }

    private fun prepareDyChangeAttributeQuantityRequestEvent(quantity: String, sku: String?): PrepareChangeAttributeRequestEvent {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress,config?.getDeviceModel())
        val context = Context(device,null,DY_CHANNEL)
        val properties = Properties(QUANTITY_ATTRIBUTE,quantity,CHANGE_ATTRIBUTE_DY_TYPE,null,null,null,null,null,null,sku,null,null,null,null,null,null,null,null)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,CHANGE_ATTRIBUTE,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareChangeAttributeQuantityRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareChangeAttributeQuantityRequestEvent)
        return prepareChangeAttributeQuantityRequestEvent
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

        otherSKUsByGroupKey[groupKey]?.get(0)?.externalImageRefV2?.let {
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
                    auxiliaryImagesForGroupKey.add(entry.value.externalImageRefV2)
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
        if (Utils.getPreferredDeliveryLocation() == null) {
            activity?.apply {
                KotlinUtils.presentEditDeliveryGeoLocationActivity(
                    this,
                    REQUEST_SUBURB_CHANGE
                )
            }
        } else {
            updateStockAvailabilityLocation() // update pdp location.
            addItemToCart()
        }
    }

    override fun responseFailureHandler(response: Response) {
        if (response.code.equals(HTTP_EXPECTATION_FAILED_417)) {
            binding.getUpdatedValidateResponse()
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

    override fun onConfirmLocation() {
        //continue add to cart request
        addItemToCart()
    }

    override fun onSetNewLocation() {
        activity?.apply {
            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                this,
                REQUEST_SUBURB_CHANGE,
                KotlinUtils.getPreferredDeliveryType(),
                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
            )
        }
    }

    private fun updateStockAvailability(isDefaultRequest: Boolean) {
        storeIdForInventory = Utils.retrieveStoreId(productDetails?.fulfillmentType)
        when (storeIdForInventory.isNullOrEmpty()) {
            true -> showProductUnavailable()
            false -> {
                productDetails?.apply {
                    otherSkus?.let { list ->
                        val multiSKUs = list.joinToString(separator = "-") { it.sku.toString() }
                        productDetailsPresenter?.loadStockAvailability(
                            storeIdForInventory!!,
                            multiSKUs,
                            isDefaultRequest,
                            isUserBrowsing
                        )
                    }
                }
            }
        }

    }

    override fun onAddToCartSuccess(addItemToCartResponse: AddItemToCartResponse) {
        activity?.apply {
            if (this is BottomNavigationActivity) {
                addItemToCartResponse.data?.let {
                    if (it.size > 0) {
                        val intent = Intent()
                        intent.apply {
                            putExtra("addedToCartMessage", it[0].message)
                            putExtra("ItemsCount", getSelectedQuantity())
                            putExtra("ProductCountMap", Utils.toJson(it[0].productCountMap))
                        }
                        onActivityResult(
                            ADD_TO_CART_SUCCESS_RESULT,
                            ADD_TO_CART_SUCCESS_RESULT,
                            intent
                        )
                    }

                    if (isEnhanceSubstitutionFeatureEnable() == true && KotlinUtils.getDeliveryType()?.deliveryType == Delivery.DASH.type) {
                        triggerFirebaseEventForSubstitution(selectionChoice = selectionChoice)
                        if (selectionChoice  == SubstitutionChoice.USER_CHOICE.name) {
                            substitutionProductItem?.price?.let {
                                    price ->
                                triggerFirebaseEventForAddSubstitution(itemName = substitutionProductItem?.productName, itemId = substitutionProductItem?.productId, itemPrice = price)
                            }
                        }
                    }

                    /* assign  updated commarceItem id here */
                    addItemToCartResponse?.data?.getOrNull(0)?.substitutionInfoList?.forEach {
                        if (it.parentProductId == productDetails?.productId) {
                            commarceItemId = it.commerceItemId
                            return@forEach
                        }
                    }
                }
                addToCartEvent(productDetails)
            }
        }
        AppConfigSingleton.dynamicYieldConfig?.apply {
            if (isDynamicYieldEnabled == true) {
                prepareDyAddToCartRequestEvent()
                prepareSyncCartRequestEvent()
            }
        }
    }

    private fun prepareSyncCartRequestEvent() {
        val user = User(dyServerId,dyServerId)
        val session = Session(dySessionId)
        val device = Device(IPAddress, config?.getDeviceModel())
        val context = Context(device, null, DY_CHANNEL)
        val cartLinesValue: MutableList<Cart> = arrayListOf()
        val cart = Cart(getSelectedSku()?.sku, getSelectedQuantity(), getSelectedSku()?.price?.toString())
        cartLinesValue.add(cart)
        val properties = Properties(null,null,SYNC_CART_V1,null,null,
            Constants.CURRENCY_VALUE,null,null,null,null,null,null,null,null,null,null,null,cartLinesValue)
        val eventsDyChangeAttribute = Event(null,null,null,null,null,null,null,null,null,null,null,null,SYNC_CART,properties)
        val events = ArrayList<Event>()
        events.add(eventsDyChangeAttribute);
        val prepareDySyncCartRequestEvent = PrepareChangeAttributeRequestEvent(
            context,
            events,
            session,
            user
        )
        dyReportEventViewModel.createDyChangeAttributeRequest(prepareDySyncCartRequestEvent)
    }

    override fun onAddToCartError(addItemToCartResponse: AddItemToCartResponse) {
        if (addItemToCartResponse?.response?.code == AppConstant.RESPONSE_ERROR_CODE_1235) {
            KotlinUtils.showQuantityLimitErrror(
                activity?.supportFragmentManager,
                addItemToCartResponse?.response?.desc ?: "",
                "",
                context
            )
        }
    }

    //firebase event add_to_cart
    private fun addToCartEvent(productDetails: ProductDetails?) {
        val quantity = getSelectedQuantity()
        if (quantity != null && productDetails != null){
            FirebaseAnalyticsEventHelper.addToCart(productDetails, quantity)
        }
    }

    private fun addItemToShoppingList() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOPADDTOLIST,
                this
            )
        }

        if (getSelectedSku() == null) {
            if (getSelectedGroupKey().isNullOrEmpty())
                binding.requestSelectColor()
            else
                binding.requestSelectSize()
            return
        }

        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSigninActivity(activity,
                SSO_REQUEST_ADD_TO_SHOPPING_LIST,
                isUserBrowsing)
        } else if (getSelectedSku() != null) {
            activity?.apply {
                val listOfItems = ArrayList<AddToListRequest>(0)
                getSelectedSku()?.let {
                    listOfItems.add(
                        it.toAddToListRequest().apply {
                            quantity = "1"
                            isGWP = !productDetails?.freeGift.isNullOrEmpty()
                            size = getSelectedSku()?.size
                        }
                    )
                }
                binding.scrollView?.fullScroll(View.FOCUS_UP)
                val addToWishListEventData = AddToWishListFirebaseEventData(
                    products = listOfNotNull(productDetails?.toAnalyticItem()),
                    businessUnit = productDetails?.productType,
                    itemRating = productDetails?.averageRating)

                KotlinUtils.openAddToListPopup(
                    requireActivity(),
                    requireActivity().supportFragmentManager,
                    listOfItems,
                    eventData = addToWishListEventData
                )
            }
        } else {
            // Select size to continue
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
                    SET_DELIVERY_LOCATION_REQUEST_CODE -> {
                        activity?.apply {
                            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                                this,
                                REQUEST_SUBURB_CHANGE,
                                KotlinUtils.getPreferredDeliveryType(),
                                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                            )
                        }
                    }
                    FuseLocationAPISingleton.REQUEST_CHECK_SETTINGS -> {
                        findItemInStore()
                    }
                    REQUEST_SUBURB_CHANGE_FOR_STOCK, ShopToggleActivity.REQUEST_DELIVERY_TYPE -> {
                        val toggleFulfilmentResultWithUnsellable= getToggleFulfilmentResultWithUnSellable(data)
                        if(toggleFulfilmentResultWithUnsellable!=null){
                            unSellableFlowFromOnActivityResult=true
                            refreshScreen()
                            UnsellableAccess.navigateToUnsellableItemsFragment(ArrayList(toggleFulfilmentResultWithUnsellable.unsellableItemsList),
                                toggleFulfilmentResultWithUnsellable.deliveryType,confirmAddressViewModel,
                                binding.progressBar,this,parentFragmentManager,)
                        }
                        else {
                            updateAddtoCartWithNewToggleFullfillment()
                        }
                    }

                    REQUEST_SUBURB_CHANGE_FOR_LIQUOR -> {

                        updateStockAvailabilityLocation()

                        Utils.getPreferredDeliveryLocation()?.let {
                            if (!this.productDetails?.productType.equals(
                                    getString(R.string.food_product_type),
                                    ignoreCase = true
                                ) && KotlinUtils.getPreferredDeliveryType() == Delivery.CNC
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
                        // check if user has any location.
                        if (Utils.getPreferredDeliveryLocation() != null) {
                            // Continue with addTo cart flow
                            setBrowsingData()
                            updateStockAvailabilityLocation() // update pdp location.
                            addItemToCart()
                        } else {
                            // request cart summary to get the user's location.
                            productDetailsPresenter?.loadCartSummary()
                        }
                    }
                    SSO_REQUEST_ADD_TO_SHOPPING_LIST -> {
                        addItemToShoppingList()
                        //One time biometrics Walkthrough
                        activity?.apply { ScreenManager.presentBiometricWalkthrough(this) }
                    }
                    SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK -> {
                        activity?.apply {
                            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                                this,
                                REQUEST_SUBURB_CHANGE_FOR_STOCK,
                                KotlinUtils.getPreferredDeliveryType(),
                                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                            )
                        }
                    }
                    LOGIN_REQUEST_SUBURB_CHANGE -> {
                        activity?.apply {
                            KotlinUtils.presentEditDeliveryGeoLocationActivity(
                                this,
                                REQUEST_SUBURB_CHANGE_FOR_LIQUOR,
                                KotlinUtils.getPreferredDeliveryType(),
                                Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                            )
                        }
                    }
                    SSO_REQUEST_FOR_ENHANCE_SUBSTITUTION -> {
                        updateStockAvailability(true)
                    }
                    SSO_REQUEST_WRITE_A_REVIEW -> {
                        (activity as? BottomNavigationActivity)?.openWriteAReviewFragment(productDetails?.productName,productDetails?.externalImageRefV2, productDetails?.productId)
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
                binding.requestSelectColor()
            else
                binding.requestSelectSize()
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
        val arguments = HashMap<String, String>()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_ID] =
            productDetails?.productId
                ?: ""
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_NAME] =
            productDetails?.productName
                ?: ""
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.IN_STORE_AVAILABILITY,
            arguments,
            activity)
    }


    private fun checkRunTimePermissionForLocation(): Boolean {
        permissionUtils = PermissionUtils(requireActivity(), this)
        permissionUtils?.apply {
            val permissions = ArrayList<String>()
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            return checkAndRequestPermissions(permissions, 1)
        }
        return false
    }

    override fun permissionGranted(requestCode: Int) {
        findItemInStore()
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
        permissions: Array<String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionUtils?.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_PERMISSION_MEDIA -> {
                when {
                    grantResults.isEmpty() -> {
                        //Do nothing
                    }
                    grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                        if (isFromFile) {
                            pickPhotoFromFile.launch("image/*")
                        } else {

                            pickPhotoLauncher.launch("image/*")
                        }
                    }
                    else -> {
                        if (isFromFile) {
                            requireActivity().resources?.apply {
                                vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                                    this@ProductDetailsFragment,
                                    requireActivity(),
                                    getString(R.string.vto_file_access_error),
                                    getString(R.string.vto_file_error_description),
                                    getString(R.string.vto_change_setting)
                                )
                            }

                        } else {
                            requireActivity().resources?.apply {
                                vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                                    this@ProductDetailsFragment,
                                    requireActivity(),
                                    getString(R.string.vto_photo_library_access_error),
                                    getString(R.string.vto_photo_library_error_description),
                                    getString(R.string.vto_change_setting)
                                )
                            }

                        }
                    }
                }
            }
        }
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

    private fun stopLocationUpdate() {
        // stop location updates
        mFuseLocationAPISingleton?.apply {
            stopLocationUpdate()
        }

    }

    private fun ProductDetailsFragmentBinding.requestSelectSize() {
        activity?.apply {
            resources.displayMetrics?.let {
                val mid: Int =
                    it.heightPixels / 2 - sizeColorSelectorLayout.selectedSizePlaceholder.height
                ObjectAnimator.ofInt(scrollView, "scrollY", mid).setDuration(500).start()
            }
            sizeColorSelectorLayout.selectedSizePlaceholder?.let {
                it.setTextColor(Color.RED)
                it.postDelayed({
                    it.setTextColor(ContextCompat.getColor(this, R.color.black))
                }, 5000)
            }
        }
    }

    private fun ProductDetailsFragmentBinding.requestSelectColor() {
        activity?.apply {
            resources.displayMetrics?.let {
                val mid: Int = it.heightPixels / 2 - sizeColorSelectorLayout.colorPlaceholder.height
                ObjectAnimator.ofInt(scrollView, "scrollY", mid).setDuration(500).start()
            }
            sizeColorSelectorLayout.colorPlaceholder?.let {
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

        // TODO: Remove non-fatal exception below once APP2-65 is closed
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_ID,
            productDetails?.productId)
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_NAME,
            productDetails?.productName)
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.DELIVERY_LOCATION,
            KotlinUtils.getPreferredDeliveryAddressOrStoreName())
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.HAS_COLOR,
            hasColor.toString())
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.HAS_SIZE,
            hasSize.toString())
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.STORE_ID,
            Utils.retrieveStoreId(productDetails?.fulfillmentType) ?: "")
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.DELIVERY_TYPE,
            KotlinUtils.getPreferredDeliveryType().toString())
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.IS_USER_AUTHENTICATED,
            SessionUtilities.getInstance().isUserAuthenticated.toString())
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.PRODUCT_SKU,
            productDetails?.sku)
        setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.SELECTED_SKU_QUANTITY,
            getSelectedSku()?.quantity.toString())
        Utils.getLastSavedLocation()?.let {
            setCrashlyticsString(FirebaseManagerAnalyticsProperties.CrashlyticsKeys.LAST_KNOWN_LOCATION,
                "${it.latitude}, ${it.longitude}")
        }
        logException(Exception(FirebaseManagerAnalyticsProperties.CrashlyticsExceptionName.PRODUCT_DETAILS_FIND_IN_STORE))
    }

    override fun showProductDetailsLoading() {
        activity?.apply {
            showProgressBar()
            binding.viewsToHideOnProductLoading.visibility = View.GONE
            binding.toCartAndFindInStoreLayout.root.visibility = View.GONE
        }
    }

    override fun hideProductDetailsLoading() {
        activity?.apply {
            hideProgressBar()
            binding.viewsToHideOnProductLoading?.visibility = View.VISIBLE
            updateAddToCartButtonForSelectedSKU()
        }

        if (VirtualTryOnUtil.isVtoConfigAvailable()) {
            productDetails?.virtualTryOn?.let {
                showVTOTryItOn()
                showVtoTryItOnHint()
            }
        }

    }

    private fun showVtoTryItOnHint() {
        if (!AppInstanceObject.get().featureWalkThrough.showTutorials || AppInstanceObject.get().featureWalkThrough.isTryItOn)
            return
        (requireActivity() as? BottomNavigationActivity)?.let {

            it.walkThroughPromtView =
                WMaterialShowcaseView.Builder(
                    it,
                    TooltipDialog.Feature.VTO_TRY_IT,
                    true
                )
                    .setTarget(binding.imgVTOOpen)
                    .setTitle(R.string.try_on_intro_txt)
                    .setDescription(R.string.try_on_intro_desc)
                    .setActionText(R.string.got_it)
                    .hideImage()
                    .setAction(this@ProductDetailsFragment)
                    .hideFeatureTutorialsText()
                    .setArrowPosition(WMaterialShowcaseView.Arrow.TOP_LEFT)
                    .setMaskColour(
                        ContextCompat.getColor(
                            it,
                            R.color.semi_transparent_black
                        )
                    )
                    .build()
            it.walkThroughPromtView?.show(it)
        }

    }

    override fun onWalkthroughActionButtonClick(feature: TooltipDialog.Feature) {
        //Do Nothing
    }

    override fun onPromptDismiss(feature: TooltipDialog.Feature) {
        binding.imgVTOOpen?.setImageResource(R.drawable.ic_camera_vto)
    }


    override fun showProgressBar() {
        activity?.apply {
            isApiCallInProgress = true
            binding.progressBar?.visibility = View.VISIBLE
        }
    }

    override fun hideProgressBar() {
        activity?.apply {
            isApiCallInProgress = false
            binding.progressBar?.visibility = View.GONE
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
    private fun ProductDetailsFragmentBinding.showSelectedColor() {
        activity?.apply {
            getSelectedGroupKey()?.let {
                sizeColorSelectorLayout.colorPlaceholder?.setTextColor(ContextCompat.getColor(this,
                    R.color.black))
                sizeColorSelectorLayout.selectedColor?.text = "  -  $it"
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun ProductDetailsFragmentBinding.showSelectedSize(selectedSku: OtherSkus?) {
        if (productDetails?.lowStockIndicator ?: 0 > selectedSku?.quantity ?: 0
            && selectedSku?.quantity!! > 0 && AppConfigSingleton.lowStock?.isEnabled == true
        ) {
            showLowStockForSelectedSize()
            sizeColorSelectorLayout.selectedSizePlaceholder?.text = ""
        } else {
            hideLowStockForSize()
        }
        getSelectedSku().let {
            sizeColorSelectorLayout.selectedSize?.text = if (it != null) "  -  ${it.size}" else ""
            if (it != null)
                sizeColorSelectorLayout.selectedSizePlaceholder?.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
        }
    }

    override fun updateDeliveryLocation(launchNewToggleScreen: Boolean) {
        activity?.apply {
            when (SessionUtilities.getInstance().isUserAuthenticated) {
                true -> if (launchNewToggleScreen) {
                    launchShopToggleScreen()
                } else {
                    KotlinUtils.presentEditDeliveryGeoLocationActivity(
                        this,
                        REQUEST_SUBURB_CHANGE_FOR_STOCK,
                        KotlinUtils.getPreferredDeliveryType(),
                        Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
                    )
                }
                false -> ScreenManager.presentSSOSigninActivity(this,
                    SSO_REQUEST_FOR_SUBURB_CHANGE_STOCK, isUserBrowsing)
            }

        }
    }

    override fun updateStockAvailabilityLocation() {
        binding.apply {
            activity?.apply {
                //If user is not authenticated or Preferred DeliveryAddress is not available hide this view
                if (!SessionUtilities.getInstance().isUserAuthenticated || getDeliveryLocation() == null) {
                    deliveryLocationLayout.root.visibility = View.GONE
                    return
                } else
                    deliveryLocationLayout.root.visibility = View.VISIBLE

                getDeliveryLocation()?.fulfillmentDetails?.let {
                    deliveryLocationLayout.apply {
                        when (Delivery.getType(it.deliveryType)) {
                            Delivery.CNC -> {
                                currentDeliveryLocation.text =
                                    resources?.getString(R.string.store) + it.storeName?.let {
                                        convertToTitleCase(it)
                                    } ?: ""
                                defaultLocationPlaceholder.text =
                                    getString(R.string.collecting_from) + " "
                            }
                            Delivery.STANDARD -> {
                                currentDeliveryLocation.text =
                                    it.address?.address1?.let { convertToTitleCase(it) } ?: ""
                                defaultLocationPlaceholder.text =
                                    getString(R.string.delivering_to_pdp)
                            }
                            Delivery.DASH -> {
                                currentDeliveryLocation.text =
                                    it.address?.address1 ?: ""
                                defaultLocationPlaceholder.text =
                                    getString(R.string.dashing_to_space)
                            }
                            else -> {
                            }

                        }
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
        binding.toCartAndFindInStoreLayout.root.visibility = View.GONE
        updateAddToCartButtonForSelectedSKU()
        //hideProgressBar()
    }

    private fun showMoreColors() {
        productColorSelectorAdapter?.apply {
            showMoreColors()
            binding.sizeColorSelectorLayout.moreColor?.visibility = View.INVISIBLE
        }
    }

    override fun loadPromotionalImages() {
        binding.apply {
            val images = ArrayList<String>()
            activity?.apply {
                productDetails?.promotionImages?.let {
                    if (!it.save.isNullOrEmpty()) images.add(it.save ?: "")
                    if (!it.wRewards.isNullOrEmpty()) images.add(it.wRewards ?: "")
                    if (!it.vitality.isNullOrEmpty()) images.add(it.vitality ?: "")
                    if (!it.newImage.isNullOrEmpty()) images.add(it.newImage ?: "")
                    if (!it.reduced.isNullOrEmpty()) images.add(it.reduced ?: "")
                    if (!it.wList.isNullOrEmpty()) images.add(it.wList ?: "")
                }

                priceLayout.promotionalImages?.removeAllViews()

                mFreeGiftPromotionalImage?.let { freeGiftImage -> images.add(freeGiftImage) }

                val promoImages = productDetails?.promotionImages
                images.forEach { image ->
                    when (image) {
                        promoImages?.reduced -> {
                            val width = deviceWidth() / 5
                            PromotionalImageBinding.inflate(layoutInflater).let { view ->
                                val promotionImageView = view.promotionImage
                                promotionImageView?.apply {
                                    adjustViewBounds = true
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    layoutParams?.width = width
                                    ImageManager.setPictureOverrideWidthHeight(
                                        view.promotionImage,
                                        image
                                    )
                                    priceLayout.promotionalImages.addView(view.root)
                                }
                            }
                        }
                        promoImages?.save -> {
                            val width = deviceWidth() / 10
                            PromotionalImageBinding.inflate(layoutInflater).let { view ->
                                val promotionImageView = view.promotionImage
                                promotionImageView.apply {
                                    adjustViewBounds = true
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    layoutParams?.width = width
                                    ImageManager.setPictureOverrideWidthHeight(
                                        view.promotionImage,
                                        image
                                    )
                                    priceLayout.promotionalImages.addView(view.root)
                                }
                            }
                        }
                        else -> {
                            PromotionalImageBinding.inflate(layoutInflater).let { view ->
                                ImageManager.loadImage(view.promotionImage, image)
                                priceLayout.promotionalImages.addView(view.root)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (null != makeupCamera) {
            job?.cancel()
        }
        isLiveCameraResumeState = true
        UpdateScreenLiveData.removeObservers(viewLifecycleOwner)
    }

    override fun onResume() {
        super.onResume()
        if (isLiveCameraResumeState && isLiveCameraOpened && (null != makeupCamera)) {
            val cameraMonitor =
                CameraMonitor(requireActivity(), makeupCamera, lifecycle)
            cameraMonitor.startCamera()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(DELAY_1000_MS)
                job = detectFaceLiveCamera()
            }
        }
        activity?.apply {
            Utils.setScreenName(this,
                FirebaseManagerAnalyticsProperties.ScreenNames.PRODUCT_DETAIL)
        }
        binding.updateReportLikeStatus()
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

    private fun productOutOfStockErrorMessage(isClickOnChangeButton:Boolean = false) {
        AppConfigSingleton.outOfStock?.apply {
            if (isOutOfStockEnabled == true && productDetails?.productType.equals(getString(R.string.food_product_type))) {
                binding.pdpOutOfStockTag.visibility = View.VISIBLE
                binding.productImagesViewPager.alpha = 0.5f
            } else {
                if (!isOutOfStockFragmentAdded || isClickOnChangeButton) {
                    isOutOfStockFragmentAdded = true
                    updateAddToCartButtonForSelectedSKU()
                    try {
                        activity?.supportFragmentManager?.beginTransaction()?.apply {
                            val productDetailsFindInStoreDialog =
                                ProductDetailsFindInStoreDialog.newInstance(
                                    this@ProductDetailsFragment
                                )
                            productDetailsFindInStoreDialog.show(
                                this,
                                ProductDetailsFindInStoreDialog::class.java.simpleName
                            )
                        }
                    } catch (ex: IllegalStateException) {
                        logException(ex)
                    }
                }
            }
        }
    }

    private fun getDeliveryLocation(): ShoppingDeliveryLocation? {
        var userLocation: ShoppingDeliveryLocation? = null
        if (SessionUtilities.getInstance().isUserAuthenticated)
            userLocation = Utils.getPreferredDeliveryLocation()
        return userLocation
    }

    override fun onOutOfStockDialogDismiss() {
        if (isOutOfStock_502) {
            isOutOfStock_502 = false
            if (childFragmentManager.backStackEntryCount > 0) {
                childFragmentManager.popBackStack()
            } else
                activity?.onBackPressed()
        } else if (productDetails?.otherSkus.isNullOrEmpty())
            activity?.onBackPressed()
    }

    override fun setUniqueIds() {
        binding.apply {
            resources?.apply {
                productLayout.contentDescription = getString(R.string.pdp_layout)
                productImagesViewPagerIndicator.contentDescription =
                    getString(R.string.store_card_image)
                openCart.root.contentDescription = getString(R.string.pdp_layout)
                productName.contentDescription = getString(R.string.pdp_textViewProductName)
                priceLayout.root.contentDescription = getString(R.string.pdp_textViewPrice)
                binding.sizeColorSelectorLayout.apply {
                    colorPlaceholder.contentDescription =
                        getString(R.string.pdp_textViewColourPlaceHolder)
                    selectedColor.contentDescription = getString(R.string.pdp_textSelectedColour)
                    colorSelectorRecycleView.contentDescription =
                        getString(R.string.pdp_colorSelectorRecycleView)
                }
                binding.toCartAndFindInStoreLayout.apply {
                    addToCartAction.contentDescription = getString(R.string.pdp_buttonAddToCart)
                    quantitySelector.contentDescription = getString(R.string.pdp_quantitySelector)
                    quantityText.contentDescription = getString(R.string.pdp_quantitySelected)
                }
                sizeColorSelectorLayout.root.contentDescription =
                    getString(R.string.pdp_sizeColourSelectorLayout)
                binding.sizeColorSelectorLayout.apply {
                    sizeSelectorRecycleView.contentDescription =
                        getString(R.string.pdp_sizeSelectorRecycleView)
                    selectedSizePlaceholder.contentDescription =
                        getString(R.string.pdp_selectedSizePlaceholder)
                    selectedSize.contentDescription = getString(R.string.pdp_textViewSelectedSize)
                }
                deliveryLocationLayout.root.contentDescription =
                    getString(R.string.pdp_deliveryLocationLayout)
                binding.deliveryLocationLayout.apply {
                    stockAvailabilityPlaceholder.contentDescription =
                        getString(R.string.pdp_stockAvailabilityPlaceholder)
                    currentDeliveryLocation.contentDescription =
                        getString(R.string.pdp_txtCurrentDeliveryLocation)
                    defaultLocationPlaceholder.contentDescription =
                        getString(R.string.pdp_defaultLocationPlaceholder)
                    editDeliveryLocation.contentDescription =
                        getString(R.string.pdp_buttoneditDeliveryLocationn)
                }
                productDetailOptionsAndInformation.root.contentDescription =
                    getString(R.string.pdp_productDetailOptionsAndInformationLayout)
                binding.productDetailOptionsAndInformation.apply {
                    headerProductOptions.contentDescription =
                        getString(R.string.pdp_headerProductOptionsLayout)
                    checkInStoreAvailability.contentDescription =
                        getString(R.string.pdp_checkInStoreAvailabilityLayout)
                    buttonView.contentDescription = getString(R.string.pdp_buttonView)
                    addToShoppingList.contentDescription =
                        getString(R.string.pdp_addToShoppingListLayout)
                    headerProductInformation.contentDescription =
                        getString(R.string.pdp_headerProductInformationLayout)
                    productDetailsInformation.contentDescription =
                        getString(R.string.pdp_productDetailsInformationLayout)
                    nutritionalInformation.contentDescription =
                        getString(R.string.pdp_productIngredientsInformationLayout)
                    productIngredientsInformation.contentDescription =
                        getString(R.string.pdp_nutritionalInformationLayout)
                }
            }
        }
    }

    private fun reloadFragment() {
        val currentFragment = activity?.supportFragmentManager?.findFragmentByTag(TAG)
        val fragmentTransaction: FragmentTransaction? =
            activity?.supportFragmentManager?.beginTransaction()
        if (fragmentTransaction != null && currentFragment != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                fragmentTransaction.detach(currentFragment).commitNow()
                fragmentTransaction.attach(currentFragment).commitNow()
            } else
                fragmentTransaction.detach(this).attach(this).commit()
        }
    }

    override fun clearSelectedOnLocationChange() {
        if (!(!hasColor && !hasSize)) {
            setSelectedSku(null)
            binding.sizeColorSelectorLayout.selectedSize?.text = ""
            binding.sizeColorSelectorLayout.selectedColor?.text = ""
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
        this.updateDeliveryLocation(launchNewToggleScreen = false)
    }

    override fun onChangeDeliveryOptionFromNewToggleFulfilment() {
        updateDeliveryLocation(launchNewToggleScreen = true)
    }

    private fun launchShopToggleScreen() {
        Intent(requireActivity(), ShopToggleActivity::class.java).apply {
            startActivityForResult(this, ShopToggleActivity.REQUEST_DELIVERY_TYPE)
        }
    }

    override fun onFindInStore() {
        this.findItemInStore()
    }

    override fun openChangeFulfillmentScreen() {
        this.updateDeliveryLocation(launchNewToggleScreen = false)
    }

    override fun clearStockAvailability() {
        productDetails?.otherSkus?.forEach {
            it.quantity = -1
        }
        loadSizeAndColor()
    }

    override fun shareProduct() {
        activity?.apply {
            Utils.triggerFireBaseEvents(
                FirebaseManagerAnalyticsProperties.SHOP_PDP_NATIVE_SHARE, hashMapOf(
                    Pair(
                        FirebaseManagerAnalyticsProperties.PropertyNames.PRODUCT_ID,
                        productDetails?.productId
                            ?: ""
                    )
                ), this
            )
            val message =
                AppConfigSingleton.productDetailsPage?.shareItemMessage + " " + productDetails?.productId?.let {
                    AppConfigSingleton.productDetailsPage?.shareItemURITemplate?.replace(
                        "{product_id}",
                        it,
                        true
                    )
                }
            val shareIntent = Intent()
            shareIntent.apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, message)
            }
            startActivity(shareIntent)
            val arguments = HashMap<String, String>()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.ITEM_ID] =
                productDetails?.productId
                    ?: ""
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.CONTENT_TYPE] = message

            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.SHARE,
                arguments,
                activity)
        }
    }

    override fun onGetRatingNReviewSuccess(ratingNReview: RatingAndReviewData) {
        hideProgressBar()
        if (ratingNReview.data.isNotEmpty()) {
            binding.showRatingAndReview()
            binding.setReviewUI(ratingNReview.data[0])
            ratingReviewResponse = ratingNReview.data[0]
            binding.scrollView?.post {
                binding.scrollView?.fullScroll(View.FOCUS_DOWN)
            }
        } else
            binding.hideRatingAndReview()
    }

    override fun onGetRatingNReviewFailed(response: Response, httpCode: Int) {
        binding.hideRatingAndReview()
    }

    /**
     * Conditions to show liquor popup
     * This should be checked before inventory call
     * 1. isLiquor flag from service response. /wfs/app/v4/searchSortAndFilterV2
     * 2. Current suburb doesn't match up with config suburbs.
     * 3. It is showing for the first time
     */
    private fun showLiquorDialog() {

        liquorDialog = activity?.let { activity -> Dialog(activity) }
        liquorDialog?.apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            val view = layoutInflater.inflate(R.layout.liquor_info_dialog, null)
            val close = view.findViewById<Button>(R.id.close)
            val setSuburb = view.findViewById<TextView>(R.id.setSuburb)
            close?.setOnClickListener { dismiss() }
            setSuburb?.setOnClickListener {
                dismiss()
                if (!SessionUtilities.getInstance().isUserAuthenticated) {
                    ScreenManager.presentSSOSigninActivity(activity,
                        LOGIN_REQUEST_SUBURB_CHANGE,
                        isUserBrowsing)
                } else {
                    activity?.apply {
                        KotlinUtils.presentEditDeliveryGeoLocationActivity(
                            this,
                            REQUEST_SUBURB_CHANGE_FOR_LIQUOR,
                            KotlinUtils.getPreferredDeliveryType(),
                            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address?.placeId
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

    private fun handlePermissionAction(action: PermissionAction?) {
        when (action) {
            PermissionAction.StoragePermissionsRequested -> PermissionUtil.requestStoragePermission(
                this,
                REQUEST_PERMISSION_MEDIA
            )
            else -> null
        }
    }

    private fun checkStoragePermission() {
        if (!PermissionUtil.hasStoragePermission(requireContext())) {
            permissionViewModel.requestStoragePermissions()
        } else {
            if (isFromFile) {
                pickPhotoFromFile.launch("image/*")
            } else {
                pickPhotoLauncher.launch("image/*")
            }
        }
    }

    private val pickPhotoLauncher =
        registerForActivityResult(PickImageGalleryContract()) { uri ->
            uri?.let {
                checkUriType(uri)
            }
        }
    private val pickPhotoFromFile = registerForActivityResult(PickImageFileContract()) { uri ->
        uri?.let {
            getPickedImageFile.launch(uri)
        }
    }
    private val getPickedImageFile = registerForActivityResult(ImageResultContract()) { uri ->
        uri?.let {
            checkUriType(uri)
        }
    }

    private fun checkUriType(uri: Uri?) {
        val cR = requireActivity().contentResolver
        val mime = MimeTypeMap.getSingleton()
        val type = mime.getExtensionFromMimeType(cR.getType(uri!!))
        if (type.equals("jpg") || type.equals("png")) {
            binding.setPickedImage(uri, null, false)
        } else {
            requireContext().apply {
                vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                    this@ProductDetailsFragment,
                    requireActivity(),
                    getString(R.string.vto_invalid_file_access_error),
                    getString(R.string.vto_invalid_file_type_error_description),
                    getString(R.string.try_again)
                )
            }
        }
    }


    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { isPicked ->
            binding.apply {
                if (isPicked) {
                    setPickedImage(uri, null, false)
                } else {
                    vtoLayout.root.visibility = View.GONE
                    share?.visibility = View.VISIBLE
                    productImagesViewPagerIndicator?.visibility = View.VISIBLE
                    openCart.root.visibility = View.VISIBLE
                    backArrow?.visibility = View.VISIBLE
                    productImagesViewPager?.visibility = View.VISIBLE
                    imgVTOOpen?.visibility = View.VISIBLE
                }
            }
        }

    private val requestSinglePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                if (liveCamera) {
                    // open live camera
                    binding.liveCameraViewHandle()
                    isLiveCamera = true
                    isColorAppliedWithLiveCamera = true
                    isRefreshImageEffectLiveCamera = true
                    binding.openPfLiveCamera()
                    binding.moveColorSelectionLayout()
                } else {
                    openDefaultCamera()
                }

            } else {
                //Can’t Access Camera permission
                requireContext().apply {
                    vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                        this@ProductDetailsFragment,
                        requireActivity(),
                        getString(R.string.vto_camera_access_error),
                        getString(R.string.vto_error_description),
                        getString(R.string.vto_change_setting)
                    )
                }
            }
        }

    private fun ProductDetailsFragmentBinding.liveCameraViewHandle() {
        vtoLayout.root.visibility = View.VISIBLE
        share?.visibility = View.GONE
        productImagesViewPagerIndicator?.visibility = View.GONE
        openCart.root.visibility = View.GONE
        backArrow?.visibility = View.GONE
        productImagesViewPager?.visibility = View.GONE
        vtoLayout.imgDownloadVTO.visibility = View.GONE
        imgVTOOpen?.visibility = View.GONE
    }


    private fun ProductDetailsFragmentBinding.openPfLiveCamera() {
        vtoLayout.apply {
            showLightingTipsFirstTime()
            SdkUtility.initSdk(
                requireActivity(),
                object : PfSDKInitialCallback {
                    override fun onInitialized() {
                        MakeupCam.create(
                            cameraSurfaceView,
                            object : MakeupCam.CreateCallback {
                                override fun onSuccess(
                                    makeupCam: MakeupCam,
                                ) {
                                    makeupCamera = makeupCam
                                    comparisonView?.init(makeupCamera)
                                    liveCameraViewModel?.liveCameraVtoApplier(
                                        makeupCamera, productDetails?.productId,
                                        getSelectedSku()?.sku
                                    )
                                    liveCameraViewModel?.colorMappedResult?.observe(
                                        viewLifecycleOwner,
                                        Observer { result ->
                                            applyColorVtoMappedResult(result)
                                        })
                                    handleLiveCamera()
                                }

                                override fun onFailure(
                                    throwable: Throwable,
                                ) {
                                    handleException(throwable)
                                }
                            })
                    }

                    override fun onFailure(
                        throwable: Throwable?,
                    ) {
                        retakeCamera?.visibility = View.GONE
                        imgVTOSplit?.visibility = View.GONE
                        captureImage?.visibility = View.GONE
                        imgVTORefresh?.visibility = View.GONE
                        requireContext().apply {
                            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                                this@ProductDetailsFragment,
                                requireActivity(),
                                getString(R.string.vto_generic_error),
                                getString(R.string.vto_generic_error_description),
                                getString(R.string.try_again)
                            )
                        }
                    }
                })
        }
    }

    private fun ProductDetailsFragmentBinding.handleLiveCamera() {
        vtoLayout.apply {
            cameraSurfaceView?.visibility = View.VISIBLE
            val cameraMonitor =
                CameraMonitor(requireActivity(), makeupCamera, lifecycle)
            lifecycle.addObserver(cameraMonitor)
            isLiveCameraOpened = true
            cameraSurfaceView?.scaleType = CameraView.ScaleType.CENTER_CROP
            initCoroutine()
            viewLifecycleOwner.lifecycleScope.launch {
                delay(DELAY_1500_MS)
                job = detectFaceLiveCamera()
            }
        }
    }

    private fun detectFaceLiveCamera(): Job {
        return coroutineScope.launch {
            while (isActive) {
                delay(DELAY_500_MS)
                val getFaceCount =
                    makeupCamera?.getCurrentFrameInfo(MakeupCam.FrameInfo.OPTION_FACE_RECT)

                if (getFaceCount?.faceRect!!.isEmpty() && (!isFaceNotDetect)) {
                    binding.showFaceNotDetectLiveCamera(true)
                    isFaceNotDetect = true
                } else
                    binding.showFaceNotDetectLiveCamera(false)
                isFaceNotDetect = false
                isFaceDetect = true

            }
        }
    }

    private fun ProductDetailsFragmentBinding.showFaceNotDetectLiveCamera(isFaceNotDetect: Boolean) {
        vtoLayout.apply {
            if (isFaceNotDetect) {
                noFaceDetected?.visibility = View.VISIBLE
                retakeCamera?.visibility = View.GONE
                imgVTOSplit?.visibility = View.GONE
                captureImage?.visibility = View.GONE
                imgVTORefresh?.visibility = View.GONE
                scrollView?.setScrollingEnabled(true)
                if (comparisonView?.isCompareModeEnable() == true) {
                    vtoDividerLayout?.visibility = View.GONE
                    isDividerVtoEffect = false
                    comparisonView?.leaveComparisonMode()
                }

            } else {
                noFaceDetected?.visibility = View.GONE
                if (!isColorNotMatch) {
                    imgVTOSplit?.visibility = View.VISIBLE
                    if (!isDividerVtoEffect) {
                        captureImage?.visibility = View.VISIBLE

                    }
                }
                if (comparisonView?.isCompareModeEnable() == false && !isColorNotMatch) {
                    captureImage?.visibility = View.VISIBLE
                    imgVTORefresh?.visibility = View.VISIBLE
                    imgVTOSplit?.setImageResource(R.drawable.ic_vto_split_screen)
                }
            }
        }
    }

    private fun initCoroutine() {
        job = Job()
        coroutineScope = CoroutineScope(Dispatchers.Main + job)
    }

    private fun ProductDetailsFragmentBinding.applyColorVtoMappedResult(result: Any?) {
        vtoLayout.apply {
            when (result) {
                VTO_COLOR_NOT_MATCH -> {
                    sizeColorSelectorLayout.colourUnavailableError?.visibility = View.VISIBLE
                    imgVTORefresh?.visibility = View.GONE
                    imgVTOSplit?.visibility = View.GONE
                    captureImage?.visibility = View.GONE
                    imgDownloadVTO?.visibility = View.GONE
                    liveCameraViewModel?.clearLiveCameraEffect()
                    isColorNotMatch = true
                    if (isDividerVtoEffect) {
                        comparisonView?.leaveComparisonMode()
                        vtoDividerLayout?.visibility = View.GONE
                        scrollView?.setScrollingEnabled(true)
                    }
                }
                VTO_COLOR_LIVE_CAMERA -> {
                    sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
                    imgVTORefresh?.visibility = View.VISIBLE
                    imgVTOSplit?.visibility = View.VISIBLE
                    imgDownloadVTO?.visibility = View.GONE
                    if (isDividerVtoEffect) {
                        captureImage?.visibility = View.GONE
                    } else {
                        captureImage?.visibility = View.VISIBLE
                    }
                    isColorNotMatch = false
                    if (isDividerVtoEffect) {
                        imgVTORefresh?.visibility = View.GONE
                        scrollView?.setScrollingEnabled(false)
                        comparisonView?.enterComparisonMode()
                    }
                }
            }
        }
    }


    private fun openDefaultCamera() {

        val photoFile = File.createTempFile(
            "IMG_",
            ".jpg",
            requireActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        )
        uri = FileProvider.getUriForFile(
            requireActivity(),
            "${requireActivity().packageName}.provider",
            photoFile
        )
        takePhoto.launch(uri)

    }


    private fun ProductDetailsFragmentBinding.setPickedImage(
        uri: Uri?,
        captureLiveCameraImg: Bitmap?,
        isFromLiveCamera: Boolean,
    ) {
        if (!isFromLiveCamera) {
            moveColorSelectionLayout()
        }
        isLiveCameraOpened = false
        vtoApplyEffectOnImageViewModel?.setApplier(
            uri, productDetails?.productId,
            getSelectedSku()?.sku, captureLiveCameraImg, isFromLiveCamera
        )
        showLightingTipsFirstTime()
        setChangePickedImage()
        vtoLayout.root.visibility = View.VISIBLE
        share?.visibility = View.GONE
        productImagesViewPagerIndicator?.visibility = View.GONE
        openCart.root.visibility = View.GONE
        backArrow?.visibility = View.GONE
        productImagesViewPager?.visibility = View.GONE
        vtoLayout.apply {
            captureImage?.visibility = View.GONE
            imgVTOSplit?.visibility = View.GONE
            noFaceDetected?.visibility = View.GONE
            isVtoImage = true
            uri?.let {
                selectedImageUri = it
                imgVTOEffect?.setPhotoUri(it)
            }
        }
        if (!isObserveImageData) {
            isObserveImageData = true
            getApplyResult()
        }
    }

    private fun ProductDetailsFragmentBinding.setChangePickedImage() {
        vtoLayout.apply {
            when {
                isFromFile -> {
                    changeImage?.visibility = View.GONE
                    retakeCamera?.visibility = View.GONE
                    changeImageFiles?.visibility = View.VISIBLE
                    imgVTOOpen?.visibility = View.GONE
                }
                isPhotoPickedFromDefaultCamera -> {
                    changeImage?.visibility = View.GONE
                    retakeCamera?.visibility = View.VISIBLE
                    changeImageFiles?.visibility = View.GONE
                    imgVTOOpen?.visibility = View.GONE
                }
                isPhotoPickedFromGallery -> {
                    changeImage?.visibility = View.VISIBLE
                    retakeCamera?.visibility = View.GONE
                    changeImageFiles?.visibility = View.GONE
                    imgVTOOpen?.visibility = View.GONE
                }
            }
        }
    }

    private fun showLightingTipsFirstTime() {
        viewLifecycleOwner.lifecycleScope.launch {
            delay(DELAY_1000_MS)
            try {
                dataPrefViewModel?.isLightingTips?.observe(
                    viewLifecycleOwner,
                    Observer { lightingTips ->
                        if (lightingTips) {
                            vtoBottomSheetDialog.showBottomSheetDialog(
                                this@ProductDetailsFragment,
                                requireActivity(),
                                false
                            )
                        }
                        dataPrefViewModel?.disableLighting(false)
                    })
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun ProductDetailsFragmentBinding.getApplyResult() {
        vtoApplyEffectOnImageViewModel?.applyEffectResult?.observe(
            viewLifecycleOwner,
            Observer { result ->
                vtoLayout.apply {
                    when {
                        result.equals(VTO_FACE_NOT_DETECT) -> {
                            noFaceDetected?.visibility = View.VISIBLE
                            imgVTORefresh?.visibility = View.GONE
                            imgDownloadVTO?.visibility = View.GONE
                            sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
                            setBitmapFromUri(selectedImageUri)
                        }
                        result.equals(VTO_COLOR_NOT_MATCH) -> {
                            sizeColorSelectorLayout.colourUnavailableError?.visibility =
                                View.VISIBLE
                            imgVTORefresh?.visibility = View.GONE
                            imgDownloadVTO?.visibility = View.GONE
                            setBitmapFromUri(selectedImageUri)
                        }
                        result.equals(SDK_INIT_FAIL) -> {
                            isVtoSdkInitFail = true
                            vtoImageLoadFail()

                        }
                        result.equals(VTO_FAIL_IMAGE_LOAD) -> {
                            isVtoSdkInitFail = false
                            vtoImageLoadFail()
                        }
                        else -> {
                            sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
                            noFaceDetected?.visibility = View.GONE
                            imgVTORefresh?.visibility = View.VISIBLE
                            imgDownloadVTO?.visibility = View.VISIBLE
                            imgVTOEffect?.setImageBitmap(result as Bitmap?)
                            saveVtoApplyImage = result as Bitmap?
                        }
                    }
                }
            })
    }

    private fun ProductDetailsFragmentBinding.vtoImageLoadFail() {
        vtoLayout.apply {
            noFaceDetected?.visibility = View.GONE
            sizeColorSelectorLayout.colourUnavailableError?.visibility = View.GONE
            imgVTORefresh?.visibility = View.GONE
            imgDownloadVTO?.visibility = View.GONE
            requireContext().apply {
                vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                    this@ProductDetailsFragment,
                    requireActivity(),
                    getString(R.string.vto_generic_error),
                    getString(R.string.vto_generic_error_description),
                    getString(R.string.try_again)
                )
            }
        }
    }

    private fun setBitmapFromUri(uri: Uri?) {
        uri?.let {
            try {
                requireActivity().contentResolver.openInputStream(it)
                    .use { imageStream ->
                        val bitmap = BitmapFactory.decodeStream(imageStream)
                        val matrix: Matrix =
                            SdkUtility.getRotationMatrixByExif(
                                requireActivity().contentResolver,
                                it
                            )
                        val selectedImage =
                            Bitmap.createBitmap(
                                bitmap,
                                0,
                                0,
                                bitmap.width,
                                bitmap.height,
                                matrix,
                                true
                            )
                        if (bitmap != selectedImage) {
                            bitmap.recycle()
                        }
                        binding.vtoLayout.imgVTOEffect?.setImageBitmap(selectedImage)

                    }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    private fun clearEffect() {
        if (isRefreshImageEffectLiveCamera) {
            clearLiveCameraEffect()
        } else {
            binding.clearImageEffect()
        }
    }

    private fun ProductDetailsFragmentBinding.clearImageEffect() {
        if (isTakePicture) {
            isTakePicture = false
            binding.vtoLayout.imgVTOEffect?.setImageBitmap(takenOriginalPicture)
            setPickedImage(null, takenOriginalPicture, true)
        } else {
            vtoApplyEffectOnImageViewModel?.clearEffect()
            vtoApplyEffectOnImageViewModel?.clearEffectImage?.observe(
                viewLifecycleOwner,
                Observer { bitmap ->
                    if (null != bitmap) {
                        binding.vtoLayout.imgVTOEffect?.setImageBitmap(bitmap)
                    } else {
                        setBitmapFromUri(selectedImageUri)
                    }
                })
        }
        productColorSelectorAdapter?.clearSelection()

    }

    private fun clearLiveCameraEffect() {
        liveCameraViewModel?.clearLiveCameraEffect()
        productColorSelectorAdapter?.clearSelection()
    }

    override fun tryAgain() {
        when {
            isVtoSdkInitFail -> binding.closeVto()
            isFromFile -> pickPhotoFromFile.launch("image/*")
            isPhotoPickedFromGallery -> pickPhotoLauncher.launch("image/*")
            else -> binding.closeVto()
        }
    }

    override fun openLiveCamera() {
        isPhotoPickedFromDefaultCamera = false
        isPhotoPickedFromGallery = false
        liveCamera = true
        requestSinglePermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    override fun openCamera() {
        isPhotoPickedFromDefaultCamera = true
        isPhotoPickedFromGallery = false
        requestSinglePermissionLauncher.launch(Manifest.permission.CAMERA)
        liveCamera = false
    }

    override fun openGallery() {
        isPhotoPickedFromDefaultCamera = false
        isPhotoPickedFromGallery = true
        isFromFile = false
        checkStoragePermission()
        handlePermission()
    }

    override fun browseFiles() {
        isPhotoPickedFromDefaultCamera = false
        isPhotoPickedFromGallery = false
        isFromFile = true
        checkStoragePermission()
        handlePermission()

    }

    private fun handlePermission() {
        permissionViewModel.actions.observe(viewLifecycleOwner,
            Observer { handlePermissionAction(it) })
    }


    //this will select the first color from colors list
    private fun selectDefaultColor(): OtherSkus? {
        productColorSelectorAdapter?.setColorSelection(0)
        return getSelectedSku()
    }

    private fun ProductDetailsFragmentBinding.moveColorSelectionLayout() {
        selectDefaultColor()
        (sizeColorSelectorLayout.root.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topToBottom = R.id.space
            sizeColorSelectorLayout.root.layoutParams = it
            sizeColorSelectorLayout.divider1?.visibility = View.GONE
        }
        (styleBy.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topToBottom = R.id.sizeColorSelectorLayout
            styleBy?.layoutParams = it
        }
        (deliveryLocationLayout.root.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topToBottom = R.id.freeGiftWithPurchaseLayout
            deliveryLocationLayout.root.layoutParams = it
        }

        isColorSelectionLayoutOnTop = true
    }

    private fun ProductDetailsFragmentBinding.resetColorSelectionLayout() {
        (sizeColorSelectorLayout.root.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topToBottom = R.id.freeGiftWithPurchaseLayout
            sizeColorSelectorLayout.root.layoutParams = it
            sizeColorSelectorLayout.divider1?.visibility = View.VISIBLE
        }
        (styleBy?.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topToBottom = R.id.space
            styleBy?.layoutParams = it
        }
        (deliveryLocationLayout.root.layoutParams as ConstraintLayout.LayoutParams).let {
            it.topToBottom = R.id.sizeColorSelectorLayout
            deliveryLocationLayout.root.layoutParams = it
        }
        isColorSelectionLayoutOnTop = false
    }

    private fun handleException(e: Any?) {
        logException(e)
    }

    /**
     * Show low stock for selected size
     * This method used for show low stock indicator when user select size or product have single size
     * lowStockThreshold > quantity
     */
    private fun ProductDetailsFragmentBinding.showLowStockForSelectedSize() {
        if (hasColor) {
            hideLowStockFromSelectedColor()
        }
        sizeColorSelectorLayout.apply {
            (selectedSize?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.startToEnd = R.id.layoutLowStockIndicator
                it.topToTop = R.id.layoutLowStockIndicator
                it.bottomToBottom = R.id.layoutLowStockIndicator
                layoutLowStockIndicator.root.visibility = View.VISIBLE
                selectedSizePlaceholder?.visibility = View.GONE
                selectedSize?.layoutParams = it
                layoutLowStockIndicator?.txtLowStockIndicator?.text =
                    AppConfigSingleton.lowStock?.lowStockCopy
            }
            (sizeSelectorRecycleView?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.layoutLowStockIndicator
                sizeSelectorRecycleView?.layoutParams = it
            }
            (sizeGuide?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToTop = R.id.layoutLowStockIndicator
                it.bottomToBottom = R.id.layoutLowStockIndicator
                sizeGuide?.layoutParams = it
            }
        }
    }

    /**
     *  This method used to Hide low stock indicator when
     *  use selected size have not low stock
     *  not have lowStockThreshold > quantity
     */
    private fun ProductDetailsFragmentBinding.hideLowStockForSize() {
        sizeColorSelectorLayout.apply {
            selectedSizePlaceholder?.text =
                requireContext().getString(R.string.product_placeholder_selected_size)
            (selectedSize?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.startToEnd = R.id.selectedSizePlaceholder
                it.topToTop = R.id.selectedSizePlaceholder
                it.bottomToBottom = R.id.selectedSizePlaceholder
                selectedSize?.layoutParams = it
                layoutLowStockIndicator.root.visibility = View.GONE
                selectedSizePlaceholder?.visibility = View.VISIBLE
            }
            (sizeSelectorRecycleView?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.selectedSizePlaceholder
                sizeSelectorRecycleView?.layoutParams = it
            }
            (sizeGuide?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToTop = R.id.selectedSizePlaceholder
                it.bottomToBottom = R.id.selectedSizePlaceholder
                sizeGuide?.layoutParams = it
            }
        }
    }


    /**
     * Show low stock for selected color
     * This method used for show low stock when selected color have
     * lowStockThreshold > quantity
     */
    private fun ProductDetailsFragmentBinding.showLowStockForSelectedColor() {
        sizeColorSelectorLayout.apply {
            (selectedColor?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.startToEnd = R.id.layoutLowStockColor
                it.topToTop = R.id.layoutLowStockColor
                it.bottomToBottom = R.id.layoutLowStockColor
                selectedColor?.layoutParams = it
                layoutLowStockColor.root.visibility = View.VISIBLE
                layoutLowStockColor.txtLowStockIndicator.text =
                    AppConfigSingleton.lowStock?.lowStockCopy
                colorPlaceholder?.visibility = View.GONE
            }
            (colorSelectorRecycleView?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.layoutLowStockColor
                colorSelectorRecycleView?.layoutParams = it
            }
            (moreColor?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToTop = R.id.layoutLowStockColor
                it.bottomToBottom = R.id.layoutLowStockColor
                moreColor?.layoutParams = it
            }
        }
    }

    /**
     * Hide low stock from selected color
     * This method used hide low stock when selected color have not
     * not have lowStockThreshold > quantity
     */
    private fun ProductDetailsFragmentBinding.hideLowStockFromSelectedColor() {
        sizeColorSelectorLayout.apply {
            colorPlaceholder?.text = requireContext().getString(R.string.selected_colour)
            (selectedColor?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.startToEnd = R.id.colorPlaceholder
                it.topToTop = R.id.colorPlaceholder
                it.bottomToBottom = R.id.colorPlaceholder
                selectedColor?.layoutParams = it
                layoutLowStockColor.root.visibility = View.GONE
                colorPlaceholder?.visibility = View.VISIBLE
            }
            (colorSelectorRecycleView?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToBottom = R.id.selectedColor
                colorSelectorRecycleView?.layoutParams = it
            }
            (moreColor?.layoutParams as ConstraintLayout.LayoutParams).let {
                it.topToTop = R.id.selectedColor
                it.bottomToBottom = R.id.selectedColor
                moreColor?.layoutParams = it
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            binding.scrollView.startScrollerTask()
        }
        return false
    }

    private fun showRatingDetailsDailog() {
        val dialog = ratingReviewResponse?.let { RatingDetailDialog(it) }
        activity?.apply {
            this@ProductDetailsFragment.childFragmentManager.beginTransaction()
                .let { fragmentTransaction ->
                    dialog?.show(
                        fragmentTransaction,
                        RatingDetailDialog::class.java.simpleName
                    )
                }
        }
    }

    private fun viewSkinProfileDialog() {
        val dialog = ratingReviewResponse?.reviews?.get(0)?.let { SkinProfileDialog(it) }
        activity?.apply {
            this@ProductDetailsFragment.childFragmentManager.beginTransaction()
                .let { fragmentTransaction ->
                    dialog?.show(
                        fragmentTransaction,
                        SkinProfileDialog::class.java.simpleName
                    )
                }
        }
    }

    private fun navigateToMoreReviewsScreen() {
        ScreenManager.presentRatingAndReviewDetail(activity, prodId)
        RatingAndReviewUtil.likedReviews.clear()
        RatingAndReviewUtil.reportedReviews.clear()
    }

    private fun likeButtonClicked() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity)
        } else {
            lifecycleScope.launch {
                showProgressBar()
                try {
                    val response = moreReviewViewModel.reviewFeedback(
                        ReviewFeedback(
                            ratingReviewResponse?.reviews?.get(0)?.id.toString(),
                            SessionUtilities.getInstance().jwt.AtgId.asString,
                            KotlinUtils.REWIEW,
                            KotlinUtils.HELPFULNESS,
                            KotlinUtils.POSITIVE,
                            null
                        )
                    )
                    hideProgressBar()
                    if (response.httpCode == 200) {
                        binding.productDetailOptionsAndInformation.customerReview.reviewHelpfulReport.ivLike.setImageResource(
                            R.drawable.iv_like_selected)
                        RatingAndReviewUtil.likedReviews.add(ratingReviewResponse?.reviews?.get(0)?.id.toString())
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    hideProgressBar()
                    if (e is HttpException && e.code() != 502) {
                        activity?.supportFragmentManager?.let { fragmentManager ->
                            Utils.showGeneralErrorDialog(
                                fragmentManager,
                                getString(R.string.statement_send_email_false_desc)
                            )
                        }
                    }
                }

            }
        }
    }

    private fun navigateToReportReviewScreen() {
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            ScreenManager.presentSSOSignin(activity)
        } else {
            ScreenManager.presentReportReview(activity,
                ratingReviewResponse?.reportReviewOptions as ArrayList<String>?,
                ratingReviewResponse?.reviews?.get(0)
            )
        }
    }

    override fun foodProductNotAvailableForCollection() {
        activity?.apply {
            if (dialogInstance != null && !dialogInstance.isVisible)
                dialogInstance.show(
                    this@ProductDetailsFragment.childFragmentManager,
                    FoodProductNotAvailableForCollectionDialog::class.java.simpleName
                )
        }
    }

    private val onScrollStoppedListener = object: LockableNestedScrollViewV2.OnScrollStoppedListener {
        override fun onScrollStopped() {
            if(!isAdded){
                return
            }
            val visible = binding.scrollView.isViewVisible(binding.productDetailOptionsAndInformation.layoutRecommendationContainer.root)
            if(visible){
                recommendationViewModel.parentPageScrolledToRecommendation()
            }
            if (!binding.scrollView.canScrollVertically(1)) {
                loadRatingAndReviews()
            }
        }
    }

    private fun loadRatingAndReviews() {
        if (!isRnRAPICalled) {
            productDetails?.isRnREnabled?.let {
                if (productDetails?.isRnREnabled == true && RatingAndReviewUtil.isRatingAndReviewConfigavailbel())
                    productDetails?.productId?.let {
                        productDetailsPresenter?.loadRatingNReview(it, 1, 0)
                        isRnRAPICalled = true
                        showProgressBar()
                        RatingAndReviewUtil.reportedReviews.clear()
                        RatingAndReviewUtil.likedReviews.clear()
                    }
            }
        }
    }


    override fun openManageSubstituion() {
        (activity as? BottomNavigationActivity)?.pushFragment(
            ManageSubstitutionFragment.newInstance(selectionChoice, commarceItemId, prodId, getSelectedSku()?.sku))
    }

    private fun substitutionEditButtonClick() {
        if (SessionUtilities.getInstance().isUserAuthenticated) {
            if (isAllProductsOutOfStock()) {
                productOutOfStockErrorMessage(true)
            } else {
                (activity as? BottomNavigationActivity)?.pushFragment(
                    ManageSubstitutionFragment.newInstance(selectionChoice, commarceItemId, prodId, getSelectedSku()?.sku))
            }
        } else {
            ScreenManager.presentSSOSignin(activity, SSO_REQUEST_FOR_ENHANCE_SUBSTITUTION)
        }
    }
    private fun refreshScreen(){
        if(isVisible) {
            UpdateScreenLiveData.observe(viewLifecycleOwner) {
                if (it == UnsellableAccess.updateUnsellableLiveData) {
                    updateAddtoCartWithNewToggleFullfillment()
                    UpdateScreenLiveData.value = UnsellableAccess.resetUnsellableLiveData
                }
            }
        }
    }

    private fun updateAddtoCartWithNewToggleFullfillment(){

        updateStockAvailabilityLocation()
        showSubstituteItemCell(true, substitutionProductItem)

        Utils.getPreferredDeliveryLocation()?.let {
            if (!this.productDetails?.productType.equals(
                    getString(R.string.food_product_type),
                    ignoreCase = true
                ) && (KotlinUtils.getPreferredDeliveryType() == Delivery.DASH)
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

    private fun listenerForUnsellable(){
        setFragmentResultListener(UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) { _, _ ->
            // Proceed with add to cart as we have moved unsellable items to List.
            if(unSellableFlowFromOnActivityResult) {
                updateAddtoCartWithNewToggleFullfillment()
                unSellableFlowFromOnActivityResult=false
            }
            else {
                onConfirmLocation()
            }
        }

        setFragmentResultListener(CustomBottomSheetDialogFragment.DIALOG_BUTTON_DISMISS_RESULT) { requestKey, bundle ->
            val resultCode =
                bundle.getString(CustomBottomSheetDialogFragment.DIALOG_BUTTON_CLICK_RESULT)
            if (resultCode == UnsellableUtils.ADD_TO_LIST_SUCCESS_RESULT_CODE) {
                // Proceed with add to cart as we have moved unsellable items to List.
                if(unSellableFlowFromOnActivityResult) {
                    updateAddtoCartWithNewToggleFullfillment()
                    unSellableFlowFromOnActivityResult=false
                }
                else {
                    onConfirmLocation()
                }
            }
        }
    }
}
