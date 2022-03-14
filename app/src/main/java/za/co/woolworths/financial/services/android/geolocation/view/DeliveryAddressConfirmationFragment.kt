package za.co.woolworths.financial.services.android.geolocation.view


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
import kotlinx.android.synthetic.main.geo_location_delivery_address.*
import kotlinx.android.synthetic.main.layout_laocation_not_available.view.*
import kotlinx.android.synthetic.main.no_collection_store_fragment.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
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
import za.co.woolworths.financial.services.android.ui.activities.click_and_collect.EditDeliveryLocationActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity.INDEX_PRODUCT
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.click_and_collect.UnsellableItemsFragment
import za.co.woolworths.financial.services.android.util.*
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.wenum.Delivery

/**
 * Created by Kunal Uttarwar on 24/02/22.
 */
class DeliveryAddressConfirmationFragment : Fragment(), View.OnClickListener {

    private var isUnSellableItemsRemoved: Boolean? = false
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var placeId: String? = null
    private var latitude: String? = null
    private var longitude: String? = null
    private  var validateLocationResponse: ValidateLocationResponse? = null
    private var deliveryType: String? = STANDARD_DELIVERY
    private var mStoreName: String? = null
    private var mStoreId: String? = null
    private var bundle: Bundle? = null

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
        bundle = arguments?.getBundle("bundle")

        bundle?.apply {
            latitude = getString(KEY_LATITUDE, "")
            longitude = this.getString(KEY_LONGITUDE, "")
            placeId = this.getString(KEY_PLACE_ID, "")
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgDelBack -> {
                activity?.onBackPressed()
                deliveryType = AppConstant.EMPTY_STRING
            }
            R.id.editDelivery -> {

                if (deliveryType.equals(CLICK_AND_COLLECT)) {

                    bundle?.putSerializable(
                        VALIDATE_RESPONSE, validateLocationResponse)

                    findNavController().navigate(
                        R.id.action_deliveryAddressConfirmationFragment_to_clickAndCollectStoresFragment,
                        bundleOf("bundle" to bundle)
                    )
                    return
                }

                if (deliveryType.equals(STANDARD_DELIVERY)) {
                    navigateToConfirmAddressScreen()
                    return
                }
            }
            R.id.btnConfirmAddress -> {
                if (SessionUtilities.getInstance().isUserAuthenticated) {
                    // sign in user :  make confirm api call and store response in cache navigate to shop tab
                    sendConfirmLocation()
                } else {
                    /*  not sign in user
                      Don’t make confirm place API
                      Cache placeDetails and Store objects from validate place API
                       Next time when user logins from anywhere in app
                       and if above data available in cache make confirm place API
                       using using above details
                       And clear the cache then navigate to shop tab
                      */

                    // navigate to shop tab
                    (activity as? BottomNavigationActivity)?.clearStack()
                    (activity as? BottomNavigationActivity)?.getBottomNavigationById()
                        ?.setCurrentItem(INDEX_PRODUCT);
                }
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
                deliveryType = CLICK_AND_COLLECT
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else
                    openCollectionTab()
            }
            R.id.geoDeliveryTab -> {
                deliveryType = STANDARD_DELIVERY
                if (progressBar?.visibility == View.VISIBLE)
                    return
                else
                    opengeoDeliveryTab()
            }
        }
    }

    private fun navigateToConfirmAddressScreen() {
        findNavController().navigate(
            R.id.action_deliveryAddressConfirmationFragment_to_confirmDeliveryLocationFragment
        )
    }

    private fun sendConfirmLocation() {

      if (validateLocationResponse?.validatePlace?.unSellableCommerceItems?.isEmpty() == false) {
            // show unsellable items
          validateLocationResponse?.validatePlace?.unSellableCommerceItems?.let {
              navigateToUnsellableItemsFragment(it)
              if (isUnSellableItemsRemoved == false) {
                  return
              }
          }
      } else {
          if (placeId == null) {
              return
          }
          val confirmLocationAddress = ConfirmLocationAddress(placeId)
          var confirmLocationRequest = ConfirmLocationRequest("", confirmLocationAddress, "",)
          if (deliveryType.equals(STANDARD_DELIVERY)) {
              confirmLocationRequest =
                  ConfirmLocationRequest(STANDARD, confirmLocationAddress)
          }
          if (deliveryType.equals(CLICK_AND_COLLECT)) {
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

                              confirmLocationResponse?.orderSummary?.fulfillmentDetails?.let {
                                  Utils.savePreferredDeliveryLocation(
                                      ShoppingDeliveryLocation(
                                          confirmLocationResponse?.orderSummary?.fulfillmentDetails
                                      )
                                  )
                              }

                            if (KotlinUtils.IS_COMING_FROM_CHECKOUT) {

                                /*now refactor UI from here to selct tab*/



                            } else {
                                    // navigate to shop/list/cart tab
                                activity?.setResult(KotlinUtils.GEO_REQUEST_CODE)
                                activity?.finish()
                            }
                          }
                          else -> {
                              // navigate to shop tab with error sceanario
                              activity?.setResult(EditDeliveryLocationActivity.REQUEST_CODE)
                              activity?.finish()
                          }
                      }
                  }
              } catch (e: HttpException) {
                  e.printStackTrace()
                  progressBar?.visibility = View.GONE
                  // navigate to shop tab with error sceanario
                  activity?.setResult(EditDeliveryLocationActivity.REQUEST_CODE)
                  activity?.finish()
              }
          }
      }
    }

    companion object {
        val KEY_LATITUDE = "latitude"
        val KEY_LONGITUDE = "longitude"
        val KEY_PLACE_ID = "placeId"
        val DELIVERY_TYPE = "deliveryType"
        val ADDRESS = "address"
        val VALIDATE_RESPONSE = "ValidateResponse"
        private const val STANDARD_DELIVERY = "StandardDelivery"
        private const val CLICK_AND_COLLECT = "CLICKANDCOLLECT"
        private const val STANDARD = "Standard"
        private const val CNC = "CnC"

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
        StoreLiveData.observe(viewLifecycleOwner,{
            geoDeliveryText?.text = HtmlCompat.fromHtml(getString(R.string.collecting_from_geo,it?.storeName), HtmlCompat.FROM_HTML_MODE_LEGACY)
            editDelivery?.text = bindString(R.string.edit)
            btnConfirmAddress?.isEnabled = true
            btnConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
            mStoreName = it?.storeName.toString()
            mStoreId = it?.storeId.toString()
        })
        isUnSellableItemsRemoved()
        placeId?.let {
            getDeliveryDetailsFromValidateLocation(it) }
    }

    private fun opengeoDeliveryTab() {
        geoDeliveryTab?.setBackgroundResource(R.drawable.bg_geo_selected_tab)
        geoDeliveryTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        geoCollectTab?.setBackgroundResource(R.drawable.bg_geo_unselected_tab)
        geoCollectTab?.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_444444))
        deliveryBagIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delivery_circle))
        btnConfirmAddress?.isEnabled = true
        btnConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        editDelivery?.text = getString(R.string.edit)
        updateDeliveryDetails()
    }

    private fun openCollectionTab() {
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
                            opengeoDeliveryTab()
                        }
                        else -> {
                            /*TODO Error sceanario*/
                        }
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                progressBar?.visibility = View.GONE
                /*TODO Error sceanario*/
            }
        }
    }

    private fun updateDeliveryDetails() {

        if (validateLocationResponse?.validatePlace?.stores?.isEmpty() == true) {
            no_conn_layout?.visibility = View.VISIBLE
            geoDeliveryView?.visibility = View.GONE
            no_loc_layout?.visibility = View.GONE
            no_conn_layout?.img_close?.setOnClickListener(this)
            no_conn_layout?.btn_change_location?.setOnClickListener(this)
            return
        }

        if (validateLocationResponse?.validatePlace?.deliverable == false) {
            no_loc_layout?.visibility = View.VISIBLE
            geoDeliveryView?.visibility = View.GONE
            no_loc_layout?.img_no_loc?.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delivery_truck))
            no_loc_layout?.btn_no_loc_change_location?.setOnClickListener(this)
            return
        }
        geoDeliveryView?.visibility = View.VISIBLE
        geoDeliveryText?.text = HtmlCompat.fromHtml(getString(R.string.delivering_to_geo,validateLocationResponse?.validatePlace?.placeDetails?.address1), HtmlCompat.FROM_HTML_MODE_LEGACY)
        imgDelIcon?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),R.drawable.ic_delivery_geo))
        //TODO: Need to add fee value from responce.
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
        if (!earliestFoodDate.isNullOrEmpty() && !earliestFashionDate.isNullOrEmpty()) {
            productsAvailableValue?.text = bindString(R.string.all)
            itemLimitValue?.text = bindString(R.string.unlimited)
        }

        if (earliestFoodDate.isNullOrEmpty() && !earliestFashionDate.isNullOrEmpty()) {
            productsAvailableValue?.text = bindString(R.string.fashion_beauty)
            val otherMaxQuantity =
                validateLocationResponse?.validatePlace?.quantityLimit?.otherMaximumQuantity?.toString()
            if (otherMaxQuantity.isNullOrEmpty())
                itemLimitValue?.text = bindString(R.string.unlimited)
            else
                itemLimitValue?.text = otherMaxQuantity
        }

        if (!earliestFoodDate.isNullOrEmpty() && earliestFashionDate.isNullOrEmpty()) {
            productsAvailableValue?.text = bindString(R.string.food)
            itemLimitValue?.text =
                validateLocationResponse?.validatePlace?.quantityLimit?.foodMaximumQuantity?.toString()
        }
    }

    private fun updateCollectionDetails() {
        if (validateLocationResponse?.validatePlace?.deliverable == false) {
            no_loc_layout?.visibility = View.VISIBLE
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
        //TODO: Need to add fee from responce with condition
        feeLabel?.visibility = View.GONE
        feeValue?.visibility = View.GONE
        feeValue?.text = ""
        if (StoreLiveData.value?.storeName.isNullOrEmpty()) {
            geoDeliveryText?.text = bindString(R.string.where_do_you_want_to_collect)
            editDelivery?.text = bindString(R.string.choose)
            btnConfirmAddress?.isEnabled = false
            btnConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.color_A9A9A9))
        } else {

            geoDeliveryText?.text = HtmlCompat.fromHtml(getString(R.string.collecting_from_geo,mStoreName), HtmlCompat.FROM_HTML_MODE_LEGACY)
            editDelivery?.text = bindString(R.string.edit)
            btnConfirmAddress?.isEnabled = true
            btnConfirmAddress?.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

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
        productsAvailableValue?.text = bindString(R.string.food)
        itemLimitValue?.text =
            validateLocationResponse?.validatePlace?.quantityLimit?.foodMaximumQuantity?.toString()
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
         }
        )
    }
}


