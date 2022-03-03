package za.co.woolworths.financial.services.android.geolocation.view

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
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
import za.co.woolworths.financial.services.android.ui.activities.dashboard.BottomNavigationActivity
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
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.databinding.adapters.TextViewBindingAdapter
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.model.BitmapDescriptor


class ClickAndCollectStoresFragment : Fragment(), OnMapReadyCallback,
    StoreListAdapter.OnStoreSelected, View.OnClickListener, TextWatcher {

    private lateinit var mapFragment: SupportMapFragment
    private var mValidateLocationResponse: ValidateLocationResponse? = null
    private var mStore: Store? = null

    companion object {

        private const val VALIDATE_RESPONSE = "VALIDATE_LOCATION_RESPONSE"

        fun newInstance(validateLocationResponse: ValidateLocationResponse?) =
            ClickAndCollectStoresFragment().withArgs {
                putSerializable(VALIDATE_RESPONSE, validateLocationResponse)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.apply {
            arguments?.apply {
                mValidateLocationResponse =
                    getSerializable(VALIDATE_RESPONSE) as ValidateLocationResponse?
            }
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
        ivCross.setOnClickListener {
            (activity as? BottomNavigationActivity)?.popFragment()
        }
        etEnterNewAddress.addTextChangedListener(this)
        mapFragment = childFragmentManager
            .findFragmentById(R.id.mapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
        setAddressUI(mValidateLocationResponse?.validatePlace?.stores, mValidateLocationResponse)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        val addressStorList = mValidateLocationResponse?.validatePlace?.stores
        showFirstFourLocationInMap(addressStorList, googleMap)
    }

    private fun showFirstFourLocationInMap(addressStorList: List<Store>?, googleMap: GoogleMap?) {
        addressStorList?.let {
            for (i in 0..3) {
                googleMap?.addMarker(
                    MarkerOptions().position(
                        LatLng(
                            addressStorList.get(i).latitude!!,
                            addressStorList.get(i).longitude!!
                        )
                    ).icon(BitmapFromVector(requireContext(), R.drawable.pin))
                )
                googleMap?.animateCamera(CameraUpdateFactory.zoomTo(18.0f))
                googleMap?.moveCamera(CameraUpdateFactory.newLatLng(LatLng(
                    addressStorList.get(i).latitude!!,
                    addressStorList.get(i).longitude!!
                )));
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
        tvStoresNearMe.text = resources.getString(R.string.near_stores, address?.size)
        tvAddress.text = mValidateLocationResponse?.validatePlace?.placeDetails?.address1

        btChange.setOnClickListener {
            (activity as? BottomNavigationActivity)?.pushFragment(
                ConfirmAddressFragment.newInstance())
        }

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

    override fun onStoreSelected(store: Store?) {
        this.mStore = store
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.tvConfirmStore -> {
              /*TODO : start GeoLocationDeliveryAddressCnfirmation Fragment with mStore object */
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

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

}