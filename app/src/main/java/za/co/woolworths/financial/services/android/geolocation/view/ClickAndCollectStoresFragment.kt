package za.co.woolworths.financial.services.android.geolocation.view

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import kotlinx.android.synthetic.main.fragment_click_and_collect_stores.*
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.geo_location_delivery_address.*
import kotlinx.android.synthetic.main.no_connection.view.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.view.DeliveryAddressConfirmationFragment.Companion.VALIDATE_RESPONSE
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.geolocation.viewmodel.StoreLiveData
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FirebaseManager
import javax.inject.Inject

@AndroidEntryPoint
class ClickAndCollectStoresFragment : DialogFragment(), OnMapReadyCallback,
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher, VtoTryAgainListener {

    private lateinit var mapFragment: SupportMapFragment
    private var mValidateLocationResponse: ValidateLocationResponse? = null
    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel
    private var dataStore: Store? = null
    private var bundle: Bundle? = null
    private  var validateLocationResponse: ValidateLocationResponse? = null
    private var placeId: String? = null
    private var isComingFromConfirmAddress: Boolean? = false
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    companion object {
        fun newInstance(validateLocationResponse: ValidateLocationResponse?) =
            ClickAndCollectStoresFragment().withArgs {
                putSerializable(VALIDATE_RESPONSE, validateLocationResponse)
            }
        var IS_FROM_STORE_LOCATOR = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen);
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            placeId = this.getString(DeliveryAddressConfirmationFragment.KEY_PLACE_ID, "")
            isComingFromConfirmAddress = getBoolean(ConfirmAddressFragment.IS_COMING_CONFIRM_ADD,false)
            mValidateLocationResponse =
                getSerializable(VALIDATE_RESPONSE) as ValidateLocationResponse
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_click_and_collect_stores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViewModel()
        tvConfirmStore?.setOnClickListener(this)
        ivCross?.setOnClickListener(this)
        btChange?.setOnClickListener(this)
        etEnterNewAddress?.addTextChangedListener(this)
        dialog?.window
            ?.attributes?.windowAnimations = R.style.DialogFragmentAnimation
        mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        if (isComingFromConfirmAddress == true) {
            placeId?.let {
                if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
                    getDeliveryDetailsFromValidateLocation(it)
                    noClickAndCollectConnectionLayout?.no_connection_layout?.visibility = View.GONE
                } else {
                    noClickAndCollectConnectionLayout?.no_connection_layout?.visibility = View.VISIBLE
                }
            }
        } else {
            setAddressUI(mValidateLocationResponse?.validatePlace?.stores,
                mValidateLocationResponse)
        }
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.uiSettings?.setAllGesturesEnabled(false)
        val addressStorList = mValidateLocationResponse?.validatePlace?.stores
        showFirstFourLocationInMap(addressStorList, googleMap)
    }

    private fun showFirstFourLocationInMap(addressStorList: List<Store>?, googleMap: GoogleMap?) {

        addressStorList?.let {
            for (i in 0..3) {
                googleMap?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            addressStorList?.get(i)?.latitude!!,
                            addressStorList?.get(i)?.longitude!!
                        )
                    ).icon(BitmapFromVector(requireContext(), R.drawable.pin))
                )
                googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(
                    addressStorList.get(i)?.latitude!!,
                    addressStorList.get(i)?.longitude!!
                 ), 11f))
            }
        }
    }

    private fun BitmapFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        val vectorDrawable: Drawable? = ContextCompat.getDrawable(context, vectorResId)
        vectorDrawable?.apply {
            setBounds(
                0,
                0,
                vectorDrawable.intrinsicWidth,
                vectorDrawable.intrinsicHeight
            )
        }

        val bitmap: Bitmap? = vectorDrawable?.intrinsicWidth?.let {
            Bitmap.createBitmap(
                it,
                vectorDrawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = bitmap?.let { Canvas(it) }
        if (canvas != null) {
            vectorDrawable?.draw(canvas)
        }
        return BitmapDescriptorFactory.fromBitmap(bitmap)
    }

    private fun setAddressUI(
        address: List<Store>?,
        mValidateLocationResponse: ValidateLocationResponse?
    ) {
        tvStoresNearMe?.text = resources.getString(R.string.near_stores, address?.size)
        tvAddress?.text = mValidateLocationResponse?.validatePlace?.placeDetails?.address1
        setStoreList(address)
    }

    private fun setStoreList(address: List<Store>?) {
        rvStoreList.layoutManager =
            activity?.let { activity -> LinearLayoutManager(activity) }
        rvStoreList.adapter = activity?.let { activity ->
            StoreListAdapter(
                activity,
                address,
                this
            )
        }
        rvStoreList.adapter?.notifyDataSetChanged()
    }

    override fun onStoreSelected(mStore: Store?) {
        dataStore = mStore
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
                navigateToFulfillmentScreen()
            }
            R.id.ivCross -> {
               dismiss()
            }
            R.id.btChange -> {
                IS_FROM_STORE_LOCATOR = true
                findNavController().navigate(
                    R.id.action_clickAndCollectStoresFragment_to_confirmAddressLocationFragment,
                    bundleOf("bundle" to bundle)
                )
            }
        }
    }

    private fun navigateToFulfillmentScreen() {
        if (IS_FROM_STORE_LOCATOR) {
            dataStore?.let { StoreLiveData.value = it }
            bundle?.putString(
                DeliveryAddressConfirmationFragment.KEY_PLACE_ID, placeId)
            IS_FROM_STORE_LOCATOR = false
            findNavController().navigate(
                R.id.action_clickAndCollectStoresFragment_to_deliveryAddressConfirmationFragment,
                bundleOf("bundle" to bundle)
            )
        } else {
            dataStore?.let { StoreLiveData.value = it }
            dismiss()
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        // not required
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        // not required
    }

    override fun afterTextChanged(s: Editable?) {
        val list = ArrayList<Store>()
        mValidateLocationResponse?.validatePlace?.stores?.let {
            for (store in it) {
                if (store.storeName?.contains(s.toString(), true) == true) {
                    list.add(store)
                }
            }
        }
        setStoreList(list)
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return
        viewLifecycleOwner.lifecycleScope.launch {
            clickCollectProgress?.visibility = View.VISIBLE
            try {
                validateLocationResponse =
                    confirmAddressViewModel.getValidateLocation(placeId)
                clickCollectProgress?.visibility = View.GONE
                geoDeliveryView?.visibility = View.VISIBLE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse?.httpCode) {
                        AppConstant.HTTP_OK -> {
                            setAddressUI(validateLocationResponse?.validatePlace?.stores, validateLocationResponse)
                        }
                        else -> {
                         showErrorDialog()
                        }
                    }
                }
            } catch (e: HttpException) {
                FirebaseManager.logException(e)
                clickCollectProgress?.visibility = View.GONE
                showErrorDialog()
            }
        }
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun showErrorDialog() {
        requireActivity().resources?.apply {
            vtoErrorBottomSheetDialog.showErrorBottomSheetDialog(
                this@ClickAndCollectStoresFragment,
                requireActivity(),
                getString(R.string.vto_generic_error),
                "",
                getString(R.string.retry_label)
            )
        }
    }
    override fun tryAgain() {
        if(confirmAddressViewModel.isConnectedToInternet(requireActivity()))
        placeId?.let { getDeliveryDetailsFromValidateLocation(it) }
    }

}