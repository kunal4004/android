package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentClickAndCollectStoresBinding
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.geolocation.network.model.Store
import za.co.woolworths.financial.services.android.geolocation.network.model.ValidateLocationResponse
import za.co.woolworths.financial.services.android.geolocation.view.adapter.StoreListAdapter
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.maps.DynamicMapDelegate
import za.co.woolworths.financial.services.android.ui.views.maps.model.DynamicMapMarker
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.BUNDLE
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_COMING_CONFIRM_ADD
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.IS_FROM_STORE_LOCATOR
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.KEY_PLACE_ID
import za.co.woolworths.financial.services.android.util.BundleKeysConstants.Companion.VALIDATE_RESPONSE
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseManager
import za.co.woolworths.financial.services.android.util.binding.BaseDialogFragmentBinding
import javax.inject.Inject

@AndroidEntryPoint
class ClickAndCollectStoresFragment : BaseDialogFragmentBinding<FragmentClickAndCollectStoresBinding>(FragmentClickAndCollectStoresBinding::inflate), DynamicMapDelegate,
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher, VtoTryAgainListener {

    private var mValidateLocationResponse: ValidateLocationResponse? = null
    private var dataStore: Store? = null
    private var bundle: Bundle? = null
    private var validateLocationResponse: ValidateLocationResponse? = null
    private var placeId: String? = null
    private var isComingFromConfirmAddress: Boolean? = false
    @Inject
    lateinit var vtoErrorBottomSheetDialog: VtoErrorBottomSheetDialog

    val confirmAddressViewModel: ConfirmAddressViewModel by activityViewModels()

    companion object {
        fun newInstance(bundle: Bundle?) =
            ClickAndCollectStoresFragment().withArgs {
                this.putBundle(BUNDLE, bundle)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle(BUNDLE)
        bundle?.apply {
            placeId = this.getString(KEY_PLACE_ID, "")
            isComingFromConfirmAddress = getBoolean(IS_COMING_CONFIRM_ADD,false)
            if(containsKey(VALIDATE_RESPONSE)){
                getSerializable(VALIDATE_RESPONSE)?.let {
                    mValidateLocationResponse =
                        it as ValidateLocationResponse
                }

            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            dynamicMapView?.initializeMap(savedInstanceState, this@ClickAndCollectStoresFragment)
            tvConfirmStore?.setOnClickListener(this@ClickAndCollectStoresFragment)
            ivCross?.setOnClickListener(this@ClickAndCollectStoresFragment)
            btChange?.setOnClickListener(this@ClickAndCollectStoresFragment)
            etEnterNewAddress?.addTextChangedListener(this@ClickAndCollectStoresFragment)
            dialog?.window
                ?.attributes?.windowAnimations = R.style.DialogFragmentAnimation
            if (isComingFromConfirmAddress == true) {
                placeId?.let {
                    if (confirmAddressViewModel.isConnectedToInternet(requireActivity())) {
                        getDeliveryDetailsFromValidateLocation(it)
                        noClickAndCollectConnectionLayout?.noConnectionLayout?.visibility =
                            View.GONE
                    } else {
                        noClickAndCollectConnectionLayout?.noConnectionLayout?.visibility =
                            View.VISIBLE
                    }
                }
            } else {
                setAddressUI(
                    mValidateLocationResponse?.validatePlace?.stores,
                    mValidateLocationResponse
                )
            }
        }
    }

    private fun showFirstFourLocationInMap(addressStoreList: List<Store>?) {
        addressStoreList?.let {
            for (i in 0..3) {
                binding.dynamicMapView?.addMarker(
                    requireContext(),
                    latitude = addressStoreList?.get(i)?.latitude,
                    longitude = addressStoreList?.get(i)?.longitude,
                    icon = R.drawable.pin
                )
            }
        }
        //after plotting all the markers pointing the camera to nearest store
        val store:Store?=addressStoreList?.get(0)
        store?.let{
            binding.dynamicMapView?.moveCamera(
                latitude = it.latitude,
                longitude =it.longitude,
                zoom = 11f
            )
        }
    }

    private fun setAddressUI(
        address: List<Store>?,
        mValidateLocationResponse: ValidateLocationResponse?
    ) {
        binding.apply {
            tvStoresNearMe?.text = resources.getString(R.string.near_stores, address?.size)
            tvAddress?.text =
                KotlinUtils.capitaliseFirstLetter(mValidateLocationResponse?.validatePlace?.placeDetails?.address1)
            setStoreList(address)
        }
    }

    private fun setStoreList(address: List<Store>?) {
        binding.apply {
            rvStoreList.layoutManager =
                activity?.let { activity -> LinearLayoutManager(activity) }
            rvStoreList.adapter = activity?.let { activity ->
                StoreListAdapter(
                    activity,
                    address,
                    this@ClickAndCollectStoresFragment
                )
            }
            rvStoreList.adapter?.notifyDataSetChanged()
        }
    }

    override fun onStoreSelected(mStore: Store?) {
        dataStore = mStore
        binding.tvConfirmStore?.isEnabled = true
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.SHOP_CONFIRM_STORE,
                    hashMapOf(
                        FirebaseManagerAnalyticsProperties.PropertyNames.ACTION_LOWER_CASE to
                                FirebaseManagerAnalyticsProperties.PropertyValues.ACTION_VALUE_SHOP_CONFIRM_STORE
                    ),
                    activity)
                navigateToFulfillmentScreen()
            }
            R.id.ivCross -> {
               dismiss()
            }
            R.id.btChange -> {
                IS_FROM_STORE_LOCATOR = true
                findNavController().navigate(
                    R.id.action_clickAndCollectStoresFragment_to_confirmAddressLocationFragment,
                    bundleOf(BUNDLE to bundle)
                )
            }
        }
    }

    private fun navigateToFulfillmentScreen() {
        if (IS_FROM_STORE_LOCATOR) {
            dataStore?.let {
                bundle?.putString(
                    KEY_PLACE_ID, placeId
                )
                IS_FROM_STORE_LOCATOR = false
                setFragmentResult(
                    DeliveryAddressConfirmationFragment.STORE_LOCATOR_REQUEST_CODE,
                    bundleOf(BUNDLE to it))
            }
            findNavController().navigate(
                R.id.action_clickAndCollectStoresFragment_to_deliveryAddressConfirmationFragment,
                bundleOf(BUNDLE to bundle)
            )
        } else {
            dataStore?.let {
                setFragmentResult(
                    DeliveryAddressConfirmationFragment.STORE_LOCATOR_REQUEST_CODE,
                    bundleOf(BUNDLE to it))
            }
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
                if (store.storeName?.contains(s.toString(), true) == true || store.storeAddress?.contains(s.toString(), true)==true) {
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
            binding.apply {
                clickCollectProgress?.visibility = View.VISIBLE
                try {
                    validateLocationResponse =
                        confirmAddressViewModel.getValidateLocation(placeId)
                    clickCollectProgress?.visibility = View.GONE
                    if (validateLocationResponse != null) {
                        when (validateLocationResponse?.httpCode) {
                            AppConstant.HTTP_OK -> {
                                setAddressUI(
                                    validateLocationResponse?.validatePlace?.stores,
                                    validateLocationResponse
                                )
                            }
                            else -> {
                                showErrorDialog()
                            }
                        }
                    }
                } catch (e: Exception) {
                    FirebaseManager.logException(e)
                    clickCollectProgress?.visibility = View.GONE
                    showErrorDialog()
                } catch (e: JsonSyntaxException) {
                    FirebaseManager.logException(e)
                    clickCollectProgress?.visibility = View.GONE
                    showErrorDialog()
                }
            }
        }
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

    override fun onMapReady() {
        binding.dynamicMapView?.setAllGesturesEnabled(false)
        showFirstFourLocationInMap(mValidateLocationResponse?.validatePlace?.stores)
    }

    override fun onMarkerClicked(marker: DynamicMapMarker) { }

    override fun onResume() {
        super.onResume()
        binding.dynamicMapView?.onResume()
    }

    override fun onPause() {
        binding.dynamicMapView?.onPause()
        super.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.dynamicMapView?.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.dynamicMapView?.onSaveInstanceState(outState)
    }

    override fun onDestroyView() {
        binding.dynamicMapView?.onDestroy()
        super.onDestroyView()
    }
}