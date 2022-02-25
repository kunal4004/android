package za.co.woolworths.financial.services.android.geolocation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.geolocation_deliv_click_collect.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import za.co.woolworths.financial.services.android.geolocation.network.apihelper.GeoLocationApiHelper
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.GeoLocationViewModelFactory
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK

/**
 * Created by Kunal Uttarwar on 24/02/22.
 */
class GeolocationDeliveryAddressConfirmationFragment : Fragment() {

    private lateinit var confirmAddressViewModel: ConfirmAddressViewModel

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
    }

    companion object {
        fun newInstance() = GeolocationDeliveryAddressConfirmationFragment()
    }

    private fun setUpViewModel() {
        confirmAddressViewModel = ViewModelProvider(
            this,
            GeoLocationViewModelFactory(GeoLocationApiHelper())
        ).get(ConfirmAddressViewModel::class.java)
    }

    private fun initView() {
        getDeliveryDetailsFromValidateLocation("EiRMb3R1cyBSaXZlciwgQ2FwZSBUb3duLCBTb3V0aCBBZnJpY2EiLiosChQKEgm7_uOL90PMHRGhcHCGx9_rrRIUChIJ1-4miA9QzB0Rh6ooKPzhf2g")
    }

    private fun getDeliveryDetailsFromValidateLocation(placeId: String) {
        if (placeId.isNullOrEmpty())
            return

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            try {
                val validateLocationResponse = confirmAddressViewModel.getValidateLocation(placeId, -33.9228, 18.4233)
                progressBar.visibility = View.GONE
                if (validateLocationResponse != null) {
                    when (validateLocationResponse.httpCode) {
                        HTTP_OK -> {

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
}