package za.co.woolworths.financial.services.android.geolocation.view


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.geo_location_delivery_address.*
import kotlinx.android.synthetic.main.layout_laocation_not_available.view.*
import kotlinx.android.synthetic.main.no_collection_store_fragment.view.*
import kotlinx.android.synthetic.main.no_connection.*
import kotlinx.android.synthetic.main.no_connection_layout.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.checkout.service.network.Address
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.*
import za.co.woolworths.financial.services.android.checkout.viewmodel.WhoIsCollectingDetails
import za.co.woolworths.financial.services.android.common.convertToTitleCase
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.model.response.ConfirmLocationAddress
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationRequest
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.StoreLiveData
import za.co.woolworths.financial.services.android.geolocation.viewmodel.UnSellableItemsLiveData
import za.co.woolworths.financial.services.android.models.dto.Province
import za.co.woolworths.financial.services.android.models.dto.ShoppingDeliveryLocation
import za.co.woolworths.financial.services.android.models.dto.Suburb
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.models.network.StorePickupInfoBody
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.CartFragment
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.CNC
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DEFAULT_ADDRESS
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.DELIVERY_TYPE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.FULLFILLMENT_REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CHECKOUT
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_CNC_SELETION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_FROM_SLOT_SELECTION
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LATITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_LONGITUDE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.REQUEST_CODE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.SAVED_ADDRESS_RESPONSE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.STANDARD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.STANDARD_DELIVERY
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.VALIDATE_RESPONSE
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.wenum.Delivery
import javax.inject.Inject

/**
 * Created by Kunal Uttarwar on 24/02/22.
 */
@AndroidEntryPoint
class DeliveryAddressConfirmationFragment : Fragment(), View.OnClickListener, VtoTryAgainListener {

    private var isUnSellableItemsRemoved: Boolean? = false
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var placeId: String? = null
    private var isComingFromSlotSelection: Boolean = false
    private var isComingFromCheckout: Boolean = false
    private var latitude: String? = null
    private var longitude: String? = null
    private  var validateLocationResponse: ValidateLocationResponse? = null
    private var deliveryType: String? = STANDARD_DELIVERY
    private var mStoreName: String? = null
    private var mStoreId: String? = null
    private var bundle: Bundle? = null
    private var defaultAddress: Address? = null
    private var savedAddressResponse: SavedAddressResponse? = null
    private var whoIsCollecting: WhoIsCollectingDetails? = null
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.geo_location_delivery_address, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)

        bundle?.apply {
            latitude = getString(KEY_LATITUDE, "")
            longitude = this.getString(KEY_LONGITUDE, "")
            placeId = this.getString(KEY_PLACE_ID, "")
            isComingFromSlotSelection = this.getBoolean(IS_COMING_FROM_SLOT_SELECTION, false)
            isComingFromCheckout = this.getBoolean(IS_COMING_FROM_CHECKOUT, false)
             //added this condition during the app Upgrade
          when(this.getString(DELIVERY_TYPE, Delivery.STANDARD.name))
          {
              Delivery.STANDARD.name->{
                  deliveryType=Delivery.STANDARD.name
              }
              Delivery.CNC.name->{
                  deliveryType=Delivery.CNC.name
              }
            else->{
                  deliveryType=Delivery.STANDARD.name
              }
          }


            getString(CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS)?.let {
                whoIsCollecting =
                    Gson().fromJson(it, object : TypeToken<WhoIsCollectingDetails>() {}.type)
            }
            if (this.containsKey(DEFAULT_ADDRESS)
                &&  this.getSerializable(DEFAULT_ADDRESS) != null) {
                defaultAddress =
                    this.getSerializable(DEFAULT_ADDRESS) as Address
            }

            if (bundle?.containsKey(SAVED_ADDRESS_RESPONSE) == true
                && this.getSerializable(SAVED_ADDRESS_RESPONSE) != null) {
                savedAddressResponse =  this.getSerializable(SAVED_ADDRESS_RESPONSE) as SavedAddressResponse
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgDelBack -> {
                activity?.onBackPressed()
            }
            R.id.editDelivery -> {
                if (deliveryType.equals(Delivery.CNC.name, true)) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.SHOP_CLICK_COLLECT_EDIT,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CLICK_COLLECT_EDIT
                        ),
                        activity)
                    bundle?.putSerializable(
                        VALIDATE_RESPONSE, validateLocationResponse)
                    bundle?.putBoolean(
                        IS_COMING_CONFIRM_ADD, false)
                    findNavController().navigate(
                        R.id.action_deliveryAddressConfirmationFragment_to_clickAndCollectStoresFragment,
                        bundleOf(BUNDLE to bundle)
                    )
                    return
                }

                if (deliveryType.equals(Delivery.STANDARD.name, true)) {
                    Utils.triggerFireBaseEvents(
                        FirebaseManagerAnalyticsProperties.SHOP_STANDARD_EDIT,
                        hashMapOf(
                            FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                    FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_STANDARD_EDIT
                        ),
                        activity)
                    navigateToConfirmAddressScreen()
                    return
                }
            }
            R.id.btnConfirmAddress -> {
                sendConfirmLocation()
            }

            R.id.btn_no_loc_change_location -> {
                navigateToConfirmAddressScreen()
            }

            R.id.btn_change_location -> {
                navigateToConfirmAddressScreen()
            }

            R.id.img_close -> {
                (activity as? BottomNavigationActivity)?.popFragment()
            }
            R.id.geoCollectTab -> {
                deliveryType = Delivery.CNC.name
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else
                    openCollectionTab()
            }
            R.id.geoDeliveryTab -> {
                deliveryType = Delivery.STANDARD.name
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else
                    openGeoDeliveryTab()
            }

        }
    }

    private fun navigateToConfirmAddressScreen() {
        bundle?.putBoolean(IS_COMING_FROM_CHECKOUT, isComingFromCheckout)
        bundle?.putBoolean(IS_COMING_FROM_SLOT_SELECTION, isComingFromSlotSelection)
        bundle?.putString(DELIVERY_TYPE, deliveryType)
        findNavController().navigate(
            R.id.action_deliveryAddressConfirmationFragment_to_confirmDeliveryLocationFragment,
            bundleOf(BUNDLE to bundle)
        )
    }

    private fun sendConfirmLocation() {
        var unSellableCommerceItems: MutableList<UnSellableCommerceItem>? = null
        when (deliveryType) {
            Delivery.STANDARD.name -> {
                unSellableCommerceItems =
                        validateLocationResponse?.validatePlace?.unSellableCommerceItems
            }
            Delivery.CNC.name -> {
                validateLocationResponse?.validatePlace?.stores?.forEach {
                    if (it.storeId.equals(mStoreId)) {
                        unSellableCommerceItems = it.unSellableCommerceItems
                    }
                }
            }
        }
        if (unSellableCommerceItems?.isNullOrEmpty() == false && isUnSellableItemsRemoved == false) {
            // show unsellable items
            unSellableCommerceItems?.let {
                navigateToUnsellableItemsFragment(it)
            }

        } else {
          if (placeId == null) {
              return
          }
          val confirmLocationAddress = ConfirmLocationAddress(placeId)
          var confirmLocationRequest = ConfirmLocationRequest("", confirmLocationAddress, "")
          if (deliveryType.equals(Delivery.STANDARD.name)) {
              confirmLocationRequest =
                  ConfirmLocationRequest(STANDARD, confirmLocationAddress)
          }
          if (deliveryType.equals(Delivery.CNC.name)) {
              confirmLocationRequest =
                  ConfirmLocationRequest(CNC, confirmLocationAddress, mStoreId)
          }

          lifecycleScope.launch {
              progressBar?.visibility = View.VISIBLE
              try {
                  val confirmLocationResponse =
                      confirmAddressViewModel.postConfirmAddress(confirmLocationRequest)
                  progressBar?.visibility = View.GONE
                  if (confirmLocationResponse != null) {
                      when (confirmLocationResponse.httpCode) {
                          HTTP_OK -> {
                              // save details in cache
                              if (SessionUtilities.getInstance().isUserAuthenticated) {
                                  Utils.savePreferredDeliveryLocation(
                                      ShoppingDeliveryLocation(
                                          confirmLocationResponse.orderSummary?.fulfillmentDetails
                                      )
                                  )
                                  if (KotlinUtils.getAnonymousUserLocationDetails() != null)
                                      KotlinUtils.clearAnonymousUserLocationDetails()
                              } else {
                                  KotlinUtils.saveAnonymousUserLocationDetails(
                                      ShoppingDeliveryLocation(
                                          confirmLocationResponse.orderSummary?.fulfillmentDetails
                                      )
                                  )
                              }

                            savedAddressResponse?.defaultAddressNickname = defaultAddress?.nickname

                              if (deliveryType == Delivery.STANDARD.name) {
                                  Utils.triggerFireBaseEvents(
                                      FirebaseManagerAnalyticsProperties.SHOP_STANDARD_CONFIRM,
                                      hashMapOf(
                                          FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                                  FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_STANDARD_CONFIRM
                                      ),
                                      activity)
                              }
                            if (isComingFromCheckout) {
                                if (deliveryType == Delivery.STANDARD.name) {
                                    if (isComingFromSlotSelection) {
                                        /*Naviagate to slot selection page with updated saved address*/

                                        val checkoutActivityIntent = Intent(
                                            activity,
                                            CheckoutActivity::class.java
                                        )
                                        checkoutActivityIntent.putExtra(
                                            CheckoutAddressConfirmationFragment.SAVED_ADDRESS_KEY,
                                            savedAddressResponse
                                        )
                                        checkoutActivityIntent.putExtra(
                                            CheckoutAddressManagementBaseFragment.GEO_SLOT_SELECTION,
                                            true
                                        )
                                        activity?.apply {
                                            startActivityForResult(
                                                checkoutActivityIntent,
                                                FULLFILLMENT_REQUEST_CODE
                                            )

                                            overridePendingTransition(
                                                R.anim.slide_from_right,
                                                R.anim.slide_out_to_left
                                            )

                                        }
                                        activity?.finish()
                                    }
                                } else if (isComingFromSlotSelection) {
                                        if (whoIsCollecting!=null) {
                                            StorePickupInfoBody().apply {
                                                firstName = whoIsCollecting?.recipientName
                                                primaryContactNo = whoIsCollecting?.phoneNumber
                                                storeId = mStoreId
                                                vehicleModel = whoIsCollecting?.vehicleModel ?: ""
                                                vehicleColour = whoIsCollecting?.vehicleColor ?: ""
                                                vehicleRegistration = whoIsCollecting?.vehicleRegistration ?: ""
                                                taxiOpted = whoIsCollecting?.isMyVehicle != true
                                                deliveryType = Delivery.CNC.name
                                                address = ConfirmLocationAddress(validateLocationResponse?.validatePlace?.placeDetails?.placeId)
                                            }
                                            startCheckoutActivity(Utils.toJson(whoIsCollecting))
                                        } else {
                                                // Navaigate to who is collecting
                                            bundle?.putBoolean(
                                               IS_COMING_FROM_CNC_SELETION, true)
                                            findNavController().navigate(
                                                R.id.action_deliveryAddressConfirmationFragment_to_geoCheckoutCollectingFragment,
                                            bundleOf(BUNDLE to bundle))

                                        }
                                    }

                            } else {
                                // navigate to shop/list/cart tab
                                activity?.setResult(Activity.RESULT_OK)
                                activity?.finish()
                            }
                          }
                      }
                  }
              } catch (e: HttpException) {
                  e.printStackTrace()
                  progressBar?.visibility = View.GONE
                  // navigate to shop tab with error sceanario
                  activity?.setResult(REQUEST_CODE)
                  activity?.finish()
              }
          }
      }
    }

    private fun startCheckoutActivity(toJson: String) {
        val checkoutActivityIntent = Intent(activity, CheckoutActivity::class.java)
        checkoutActivityIntent.putExtra(
            CheckoutReturningUserCollectionFragment.KEY_COLLECTING_DETAILS,
            toJson
        )
        activity?.let {
            startActivityForResult(
                checkoutActivityIntent,
                CartFragment.REQUEST_PAYMENT_STATUS
            )

            it.overridePendingTransition(
                R.anim.slide_from_right,
                R.anim.slide_out_to_left
            )
        }
        activity?.finish()
    }

    companion object {

        fun newInstance(latitude: String, longitude: String, placesId: String) =
            DeliveryAddressConfirmationFragment().withArgs {
                putString(KEY_LATITUDE, latitude)
                putString(KEY_LONGITUDE, longitude)
                putString(KEY_PLACE_ID, placesId)
            }

        @JvmStatic
        fun newInstance(placesId: String?, deliveryType: Delivery? = Delivery.STANDARD) =
            DeliveryAddressConfirmationFragment().withArgs {
                putString(KEY_PLACE_ID, placesId)
                putString(DELIVERY_TYPE, deliveryType.toString())
            }
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun initView() {
        imgDelBack?.setOnClickListener(this)
        editDelivery?.setOnClickListener(this)
        btnConfirmAddress?.setOnClickListener(this)
        geoDeliveryTab?.setOnClickListener(this)
        geoCollectTab?.setOnClickListener(this)
        geoDeliveryTab?.isEnabled = true
        geoCollectTab?.isEnabled = true
        StoreLiveData.observe(viewLifecycleOwner,{
            if (it?.storeName != null) {
                geoDeliveryText?.text =
                    HtmlCompat.fromHtml(getString(R.string.collecting_from_geo, it?.storeName?.let { convertToTitleCase(it) }),
                        HtmlCompat.FROM_HTML_MODE_LEGACY)
                itemLimitValue?.text  = it?.quantityLimit?.foodMaximumQuantity.toString()
                editDelivery?.text = bindString(R.string.choose)
                btnConfirmAddress?.isEnabled = true
                btnConfirmAddress?.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                mStoreName = it?.storeName.toString()
                mStoreId = it?.storeId.toString()
            }
        })
        isUnSellableItemsRemoved()
        placeId?.let {
            if (confirmAddressViewModel.isConnectedToInternet(requireActivity())){
                getDeliveryDetailsFromValidateLocation(it)
                connectionLayout?.no_connection_layout?.visibility = View.GONE
            }
            else {
                connectionLayout?.no_connection_layout?.visibility = View.VISIBLE
        }
        }
        no_connection_layout?.btnRetry?.setOnClickListener{
            initView()
        }
    }

    private fun openGeoDeliveryTab() {
        deliveryType = Delivery.STANDARD.name

        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOP_DELIVERY,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_DELIVERY
            ),
            activity)

        geoDeliveryTab?.setBackgroundResource(R.drawable.bg_geo_selected_tab)
        geoDeliveryTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        geoCollectTab?.setBackgroundResource(R.drawable.bg_geo_unselected_tab)
        geoCollectTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_444444))
        deliveryBagIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delivery_geo_circle))
        btnConfirmAddress?.isEnabled = true
        btnConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        editDelivery?.text = getString(R.string.edit)
        updateDeliveryDetails()
    }

    private fun openCollectionTab() {
        deliveryType = Delivery.CNC.name

        Utils.triggerFireBaseEvents(
            FirebaseManagerAnalyticsProperties.SHOP_CLICK_COLLECT,
            hashMapOf(
                FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                        FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CLICK_COLLECT
            ),
            activity)
        geoCollectTab?.setBackgroundResource(R.drawable.bg_geo_selected_tab)
        geoCollectTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        geoDeliveryTab?.setBackgroundResource(R.drawable.bg_geo_unselected_tab)
        geoDeliveryTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_444444))
        deliveryBagIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_bag_circle))
        updateCollectionDetails()
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return

        lifecycleScope.launch {
            progressBar?.visibility = View.VISIBLE
            try {
                validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(placeId)
                progressBar?.visibility = View.GONE
                geoDeliveryView?.visibility = View.VISIBLE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        HTTP_OK -> {
                            if (deliveryType.equals(Delivery.STANDARD.name, true)) {
                                openGeoDeliveryTab()
                            } else {
                                openCollectionTab()
                            }
                        }
                        else -> {
                            showErrorDialog()

                        }
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun updateDeliveryDetails() {
        if (validateLocationResponse?.validatePlace?.deliverable == false) {
            no_loc_layout?.visibility = View.VISIBLE
            geoDeliveryView?.visibility = View.GONE
            no_loc_layout?.txt_no_loc_title?.text  = getString(R.string.no_location_delivery)
            no_loc_layout?.img_no_loc?.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delivery_truck))
            no_loc_layout?.btn_no_loc_change_location?.setOnClickListener(this)
            return
        }
        geoDeliveryView?.visibility = View.VISIBLE
        geoDeliveryText?.text = HtmlCompat.fromHtml(getString(R.string.delivering_to_geo,validateLocationResponse?.validatePlace?.placeDetails?.address1), HtmlCompat.FROM_HTML_MODE_LEGACY)
        imgDelIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
            R.drawable.ic_delivery_geo))
        setProductStatus()

        feeLabel?.visibility = View.GONE
        feeValue?.visibility = View.GONE
        feeValue?.text = ""
        val earliestFoodDate =
            validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty()) {
            earliestDeliveryDateLabel?.visibility = View.GONE
            earliestDeliveryDateValue?.visibility = View.GONE
        } else {
            earliestDeliveryDateLabel?.visibility = View.VISIBLE
            earliestDeliveryDateValue?.visibility = View.VISIBLE
            earliestDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFoodDate)

        }

        val earliestFashionDate =
            validateLocationResponse?.validatePlace?.firstAvailableOtherDeliveryDate
        if (earliestFashionDate.isNullOrEmpty()) {
            earliestFashionDeliveryDateLabel?.visibility = View.GONE
            earliestFashionDeliveryDateValue?.visibility = View.GONE
        } else {
            earliestFashionDeliveryDateValue?.text =
                WFormatter.getFullMonthWithDate(earliestFashionDate)
            earliestFashionDeliveryDateLabel?.visibility = View.VISIBLE
            earliestFashionDeliveryDateValue?.visibility = View.VISIBLE
        }
        itemLimitValue?.text = bindString(R.string.unlimited)

    }

    private fun setProductStatus() {
        productsAvailableValue?.apply {
            if (deliveryType == Delivery.STANDARD.name) {
                validateLocationResponse?.validatePlace?.let {
                    when {
                        !it.firstAvailableFoodDeliveryDate.isNullOrEmpty()
                                && it.firstAvailableOtherDeliveryDate.isNullOrEmpty() -> {
                            this.text = getString(R.string.food)

                        }
                        it.firstAvailableFoodDeliveryDate.isNullOrEmpty()
                                && !it.firstAvailableOtherDeliveryDate.isNullOrEmpty() -> {
                            this.text = getString(R.string.fashion_beauty_geo)
                        }
                        else -> {
                            this.text = getString(R.string.all)
                        }
                    }
                }
            } else {
                this.text = getString(R.string.food)
            }
        }
    }

    private fun updateCollectionDetails() {

        if (validateLocationResponse?.validatePlace?.stores?.isEmpty() == true) {
            no_conn_layout?.visibility = View.VISIBLE
            geoDeliveryView?.visibility = View.GONE
            no_loc_layout?.visibility = View.GONE
            no_loc_layout?.txt_no_loc_title?.text  = getString(R.string.no_location_collection)
            geoCollectTab?.visibility = View.GONE
            geoDeliveryTab?.visibility = View.GONE
            no_conn_layout?.img_close?.setOnClickListener(this)
            no_conn_layout?.btn_change_location?.setOnClickListener(this)
            return
        }

        if (validateLocationResponse?.validatePlace?.deliverable == false) {
            no_loc_layout?.visibility = View.VISIBLE
            no_loc_layout?.txt_no_loc_title?.text  = getString(R.string.no_location_collection)
            geoDeliveryView?.visibility = View.GONE
            no_loc_layout?.img_no_loc?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.shoppingbag
                )
            )
            no_loc_layout?.btn_no_loc_change_location?.setOnClickListener(this)
            return
        }

        geoDeliveryView?.visibility = View.VISIBLE
        imgDelIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_basket))
        setProductStatus()
        feeLabel?.visibility = View.GONE
        feeValue?.visibility = View.GONE
        feeValue?.text = ""
        setGeoDeliveryTextForCnc()

        val earliestFoodDate =
            validateLocationResponse?.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty()) {
            earliestDeliveryDateLabel?.visibility = View.GONE
            earliestDeliveryDateValue?.visibility = View.GONE
        } else {
            earliestDeliveryDateLabel?.visibility = View.VISIBLE
            earliestDeliveryDateValue?.visibility = View.VISIBLE
            earliestDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFoodDate)
        }
        earliestFashionDeliveryDateLabel?.visibility = View.GONE
        earliestFashionDeliveryDateValue?.visibility = View.GONE
        itemLimitValue?.text = getNearestStoreItemLimit(validateLocationResponse?.validatePlace?.stores)
    }

    private fun setGeoDeliveryTextForCnc() {
        if (!StoreLiveData.value?.storeName.isNullOrEmpty()) {
            geoDeliveryText?.text = HtmlCompat.fromHtml(
                getString(R.string.collecting_from_geo, mStoreName?.let { convertToTitleCase(it) }),
                HtmlCompat.FROM_HTML_MODE_LEGACY
            )
            editDelivery?.text = bindString(R.string.choose)
            btnConfirmAddress?.isEnabled = true
            btnConfirmAddress?.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        } else if (Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.address != null) {

            Utils.getPreferredDeliveryLocation()?.fulfillmentDetails?.let {
                if (it.storeName.equals("null") || it.storeName.isNullOrEmpty()) {
                    whereToCollect()
                } else {
                    mStoreName=it.storeName
                    mStoreId=it.storeId
                    geoDeliveryText?.text = HtmlCompat.fromHtml(
                        getString(R.string.collecting_from_geo, it.storeName?.let { convertToTitleCase(it) }),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                    )
                    editDelivery?.text = bindString(R.string.choose)
                    btnConfirmAddress?.isEnabled = true
                    btnConfirmAddress?.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.black
                        )
                    )
                }
            }
        } else {
            whereToCollect()
        }
    }

    private fun whereToCollect() {
        geoDeliveryText?.text = HtmlCompat.fromHtml(
            getString(R.string.where_do_you_want_to_collect, ""),
            HtmlCompat.FROM_HTML_MODE_LEGACY
        )
        mStoreId = ""
        editDelivery?.text = bindString(R.string.choose)
        btnConfirmAddress?.isEnabled = false
        btnConfirmAddress?.setBackgroundColor(
            ContextCompat.getColor(
                requireActivity(),
                R.color.color_A9A9A9
            )
        )

    }

    private fun getNearestStore(stores: List<Store>?): String? {
        var shortestDistance: Store? = null
        if (!stores.isNullOrEmpty()) {
            shortestDistance = stores.minByOrNull {
                it.distance!!
            }
        }
        return shortestDistance?.storeName
    }

    private fun getNearestStoreId(stores: List<Store>?): String? {
        var shortestDistance: Store? = null
        if (!stores.isNullOrEmpty()) {
            shortestDistance = stores.minByOrNull {
                it.distance!!
            }
        }
        return shortestDistance?.storeId
    }

    private fun getNearestStoreItemLimit(stores: List<Store>?): String? {
        var shortestDistance: Store? = null
        if (!stores.isNullOrEmpty()) {
            shortestDistance = stores.minByOrNull {
                it.distance!!
            }
        }
        return shortestDistance?.quantityLimit?.foodMaximumQuantity.toString()
    }

    override fun onStop() {
        super.onStop()
        StoreLiveData.value?.storeName = null
        StoreLiveData.value?.storeId = null
        StoreLiveData.postValue(null)
    }

    /**
     * This function is to navigate to Unsellable Items screen.
     * @param [unSellableCommerceItems] list of items that are not deliverable in the selected location
     * @param [deliverable] boolean flag to determine if provided list of items are deliverable
     *
     * @see [Suburb]
     * @see [Province]
     * @see [UnSellableCommerceItem]
     */
    private fun navigateToUnsellableItemsFragment(
        unSellableCommerceItems: MutableList<UnSellableCommerceItem>
    ) {
        findNavController()?.navigate(
            R.id.action_deliveryAddressConfirmationFragment_to_geoUnsellableItemsFragment,
            bundleOf(
                UnsellableItemsFragment.KEY_ARGS_BUNDLE to bundleOf(
                    UnsellableItemsFragment.KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS to Utils.toJson(unSellableCommerceItems),
                )
            )
        )
    }

    private fun isUnSellableItemsRemoved() {
        UnSellableItemsLiveData.observe(viewLifecycleOwner, {
            isUnSellableItemsRemoved = it
            if (isUnSellableItemsRemoved == true) {
                sendConfirmLocation()
                UnSellableItemsLiveData.value = false
            }
         }
        )
    }

    private fun showErrorDialog() {
        geoDeliveryTab?.isEnabled = false
        geoCollectTab?.isEnabled = false
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@DeliveryAddressConfirmationFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }

    override fun tryAgain() {
       initView()
    }
}


