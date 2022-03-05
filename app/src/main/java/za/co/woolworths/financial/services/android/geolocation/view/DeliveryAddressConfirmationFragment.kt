package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.geolocation_deliv_click_collect.*
import kotlinx.android.synthetic.main.geolocation_deliv_click_collect.deliveryTab
import kotlinx.android.synthetic.main.layout_laocation_not_available.view.*
import kotlinx.android.synthetic.main.no_collection_store_fragment.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.StoreLiveData
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.WFormatter

/**
 * Created by Kunal Uttarwar on 24/02/22.
 */
class DeliveryAddressConfirmationFragment : Fragment(), View.OnClickListener {

    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var placeId: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private lateinit var validateLocationResponse: ValidateLocationResponse
    private var deliveryType: String = STANDARD_DELIVERY
    private var mStoreName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        return inflater.inflate(R.layout.geolocation_deliv_click_collect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            arguments?.apply {
                placeId =
                    getString(KEY_PLACE_ID)
                latitude = getDouble(KEY_LATITUDE)
                longitude = getDouble(KEY_LONGITUDE)
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.geoloc_deliv_click_back -> {
                activity?.onBackPressed()
                deliveryType = AppConstant.EMPTY_STRING
            }
            R.id.geoloc_clickNCollectEditChangetv -> {
                if (deliveryType.equals(CLICK_AND_COLLECT)) {
                    (activity as? BottomNavigationActivity)?.pushFragment(ClickAndCollectStoresFragment.newInstance(validateLocationResponse))
                    return
                }

                if (deliveryType.equals(STANDARD_DELIVERY)) {
                    (activity as? BottomNavigationActivity)?.pushFragment(ConfirmAddressFragment.newInstance())
                    return
                }
            }
            R.id.btnConfirmAddress -> {

            }

            R.id.btn_no_loc_change_location -> {
                (activity as? BottomNavigationActivity)?.pushFragment(ConfirmAddressFragment.newInstance())
            }

            R.id.btn_change_location -> {
                (activity as? BottomNavigationActivity)?.pushFragment(ConfirmAddressFragment.newInstance())
            }

            R.id.img_close -> {
                (activity as? BottomNavigationActivity)?.popFragment()
            }
            R.id.geocollectionTab -> {
                deliveryType = CLICK_AND_COLLECT
                if (progressBar.visibility == View.VISIBLE)
                    return
                else
                    openCollectionTab()
            }
            R.id.deliveryTab -> {
                deliveryType = STANDARD_DELIVERY
                if (progressBar.visibility == View.VISIBLE)
                    return
                else
                    openDeliveryTab()
            }
        }
    }

    companion object {
        private val KEY_LATITUDE = "latitude"
        private val KEY_LONGITUDE = "longitude"
        private val KEY_PLACE_ID = "placeId"
        private val DELIVERY_TYPE = "deliveryType"


        private const val STANDARD_DELIVERY = "StandardDelivery"
        private const val CLICK_AND_COLLECT = "CLICKANDCOLLECT"


        fun newInstance(latitude: String, longitude: String, placesId: String) =
            DeliveryAddressConfirmationFragment().withArgs {
                putString(KEY_LATITUDE, latitude)
                putString(KEY_LONGITUDE, longitude)
                putString(KEY_PLACE_ID, placesId)
            }

        @JvmStatic
        fun newInstance(placesId: String?, deliveryType: String? = "Standard") =
            DeliveryAddressConfirmationFragment().withArgs {
                putString(KEY_PLACE_ID, placesId)
                putString(DELIVERY_TYPE, deliveryType)
            }
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun initView() {
        geoloc_deliv_click_back?.setOnClickListener(this)
        geoloc_clickNCollectEditChangetv?.setOnClickListener(this)
        btnConfirmAddress?.setOnClickListener(this)
        deliveryTab?.setOnClickListener(this)
        geocollectionTab?.setOnClickListener(this)
        StoreLiveData.observe(viewLifecycleOwner,{
            geoloc_clickNCollectValue?.text = it?.storeName
            mStoreName = it?.storeName.toString()

        })
        placeId?.let { getDeliveryDetailsFromValidateLocation(it) }
    }

    private fun openDeliveryTab() {
        deliveryTab.setBackgroundResource(R.drawable.delivery_round_btn_black)
        deliveryTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        geocollectionTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
        geocollectionTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.offer_title))
        updateDeliveryDetails()
    }

    private fun openCollectionTab() {
        geocollectionTab.setBackgroundResource(R.drawable.delivery_round_btn_black)
        geocollectionTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        deliveryTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
        deliveryTab.setTextColor(ContextCompat.getColor(requireContext(), R.color.offer_title))
        updateCollectionDetails()
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(placeId, latitude, longitude)
                progressBar.visibility = View.GONE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse.httpCode) {
                        HTTP_OK -> {
                            openDeliveryTab()
                        }
                        else -> {

                        }
                    }
                }
            } catch (e: HttpException) {
                e.printStackTrace()
                progressBar.visibility = View.GONE
            }
        }
    }

    private fun updateDeliveryDetails() {

        if (validateLocationResponse?.validatePlace?.stores?.isEmpty() == true) {
            no_conn_layout?.visibility = View.VISIBLE
            main_layout?.visibility = View.GONE
            no_loc_layout?.visibility = View.GONE
            geoloc_deliv_clickLayout?.visibility = View.GONE
            geoloc_deliv_click_back?.visibility = View.GONE
            no_conn_layout?.img_close?.setOnClickListener(this)
            no_conn_layout?.btn_change_location?.setOnClickListener(this)
            return
        }

        if (validateLocationResponse?.validatePlace?.deliverable == false) {
            no_loc_layout?.visibility = View.VISIBLE
            main_layout?.visibility = View.GONE
            /*TODO: Set image as Per standard delivery or CNC*/
            if (deliveryType == STANDARD_DELIVERY) {
                no_loc_layout?.img_no_loc?.setImageDrawable(ContextCompat.getDrawable(requireActivity(), R.drawable.ic_delivery_truck))
            } else {
                no_loc_layout?.img_no_loc?.setImageDrawable(ContextCompat.getDrawable(requireActivity(),
                    R.drawable.shoppingbag
                ))
            }
            no_loc_layout?.btn_no_loc_change_location?.setOnClickListener(this)
            return
        }


        geolocDeliveryDetailsLayout.visibility = View.VISIBLE
        geoloc_clickNCollectTitle.text = bindString(R.string.delivering_to)
        icon_deliv_click.background = bindDrawable(R.drawable.icon_delivery)
        feeLayout.visibility = View.GONE
        feeValue?.text = ""//validateLocationResponse.validatePlace
        geoloc_clickNCollectValue?.text =
            validateLocationResponse?.validatePlace?.placeDetails?.address1

        val earliestFoodDate =
            validateLocationResponse.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty())
            earliestDeliveryDateLayout.visibility = View.GONE
        else {
            earliestDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFoodDate)
            earliestDeliveryDateLayout.visibility = View.VISIBLE
        }

        val earliestFashionDate =
            validateLocationResponse.validatePlace?.firstAvailableOtherDeliveryDate
        if (earliestFashionDate.isNullOrEmpty())
            earliestFashionDeliveryDateLayout.visibility = View.GONE
        else {
            earliestFashionDeliveryDateLayout.visibility = View.VISIBLE
            earliestFashionDeliveryDateValue?.text =
                WFormatter.getFullMonthWithDate(earliestFashionDate)
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
        geolocDeliveryDetailsLayout.visibility = View.VISIBLE
        geoloc_clickNCollectTitle.text = bindString(R.string.collecting_from)
        icon_deliv_click.background = bindDrawable(R.drawable.shoppingbag)
        feeLayout.visibility = View.GONE
        feeValue?.text = ""//validateLocationResponse.validatePlace
        val storeName = getNearestStore(validateLocationResponse?.validatePlace?.stores)
        if (storeName.isNullOrEmpty())
            geoloc_clickNCollectValue?.text = ""
        else
            geoloc_clickNCollectValue?.text = if (mStoreName != null) mStoreName else storeName

        val earliestFoodDate =
            validateLocationResponse.validatePlace?.firstAvailableFoodDeliveryDate
        if (earliestFoodDate.isNullOrEmpty())
            earliestDeliveryDateLayout.visibility = View.GONE
        else {
            earliestDeliveryDateLayout.visibility = View.VISIBLE
            earliestDeliveryDateValue?.text = WFormatter.getFullMonthWithDate(earliestFoodDate)
        }
        earliestFashionDeliveryDateLayout.visibility = View.GONE
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
}


