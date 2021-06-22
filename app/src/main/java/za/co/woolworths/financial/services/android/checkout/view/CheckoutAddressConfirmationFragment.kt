package za.co.woolworths.financial.services.android.checkout.view

import android.content.Intent
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import za.co.woolworths.financial.services.android.checkout.interactor.CheckoutAddAddressNewUserInteractor
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutAddAddressNewUserApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.CheckoutMockApiHelper
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.checkout.viewmodel.CheckoutAddAddressNewUserViewModel
import za.co.woolworths.financial.services.android.checkout.viewmodel.ViewModelFactory
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 16/06/21.
 */
class CheckoutAddressConfirmationFragment : Fragment(), View.OnClickListener {

    var savedAddress: SavedAddressResponse? = null
    var checkoutAddressConfirmationListAdapter: CheckoutAddressConfirmationListAdapter? = null
    private lateinit var checkoutAddAddressNewUserViewModel: CheckoutAddAddressNewUserViewModel
    private var navController: NavController? = null

    companion object{
        const val UPDATE_SAVED_ADDRESS_REQUEST_KEY = "updateSavedAddress"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_address_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (navController == null)
            navController = Navigation.findNavController(view)
        setupViewModel()
        initView()
        addFragmentResultListener()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments?.getBundle("bundle")
        updateSavedAddress(bundle)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.deliveryTab -> {
                deliveryTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
                collectionTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
                addressConfirmationDelivery.visibility = View.VISIBLE
            }
            R.id.collectionTab -> {
                collectionTab.setBackgroundResource(R.drawable.delivery_round_btn_white)
                deliveryTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
                addressConfirmationDelivery.visibility = View.GONE
            }
            R.id.plusImgAddAddress, R.id.addNewAddressTextView -> {
                startActivity(Intent(activity, CheckoutActivity::class.java))
            }
            R.id.btnCheckOut -> {
                if (checkoutAddressConfirmationListAdapter?.checkedItemPosition == -1)
                    addNewAddressErrorMsg.visibility = View.VISIBLE
            }
        }
    }

    private fun addFragmentResultListener() {
        // Use the Kotlin extension in the fragment-ktx artifact
        setFragmentResultListener(UPDATE_SAVED_ADDRESS_REQUEST_KEY) { requestKey, bundle ->
            updateSavedAddress(bundle)
            initView()
        }
    }

    private fun updateSavedAddress(bundle: Bundle?) {
        bundle?.apply {
            if (containsKey("savedAddress")) {
                val addressString = getString("savedAddress")
                if (!addressString.isNullOrEmpty() && !addressString.equals("null", true))
                    savedAddress =
                        (Utils.jsonStringToObject(
                            getString("savedAddress"),
                            SavedAddressResponse::class.java
                        ) as? SavedAddressResponse)!!
            }
        }
    }

    private fun initView() {

        val decoration: ItemDecoration = object : ItemDecoration() {
            override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                super.onDraw(c, parent!!, state!!)
                //empty because to remove divider
            }
        }

        saveAddressRecyclerView?.apply {
            checkoutAddressConfirmationListAdapter =
                CheckoutAddressConfirmationListAdapter(
                    savedAddress,
                    checkoutAddAddressNewUserViewModel,
                    viewLifecycleOwner, navController
                )
            addItemDecoration(decoration)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutAddressConfirmationListAdapter?.let { adapter = it }
        }
        deliveryTab.setOnClickListener(this)
        collectionTab.setOnClickListener(this)
        plusImgAddAddress.setOnClickListener(this)
        addNewAddressTextView.setOnClickListener(this)
    }

    private fun setupViewModel() {
        checkoutAddAddressNewUserViewModel = ViewModelProviders.of(
            this,
            ViewModelFactory(
                CheckoutAddAddressNewUserInteractor(
                    CheckoutAddAddressNewUserApiHelper(),
                    CheckoutMockApiHelper()
                )
            )
        ).get(CheckoutAddAddressNewUserViewModel::class.java)
    }
}