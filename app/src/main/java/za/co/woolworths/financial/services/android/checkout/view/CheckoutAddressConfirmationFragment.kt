package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.checkout_address_confirmation.*
import kotlinx.android.synthetic.main.checkout_address_confirmation_delivery.*
import za.co.woolworths.financial.services.android.checkout.service.network.SavedAddressResponse
import za.co.woolworths.financial.services.android.checkout.view.adapter.CheckoutAddressConfirmationListAdapter
import za.co.woolworths.financial.services.android.util.Utils


/**
 * Created by Kunal Uttarwar on 16/06/21.
 */
class CheckoutAddressConfirmationFragment : Fragment() {

    var savedAddress: SavedAddressResponse? = null
    var checkoutAddressConfirmationListAdapter: CheckoutAddressConfirmationListAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.checkout_address_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val bundle = arguments?.getBundle("bundle")
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
                CheckoutAddressConfirmationListAdapter(savedAddress)
            addItemDecoration(decoration)
            layoutManager = activity?.let { LinearLayoutManager(it) }
            checkoutAddressConfirmationListAdapter?.let { adapter = it }
        }

        deliveryTab.setOnClickListener {
            it.setBackgroundResource(R.drawable.delivery_round_btn_white)
            collectionTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
            addressConfirmationDelivery.visibility = View.VISIBLE
        }
        collectionTab.setOnClickListener {
            it.setBackgroundResource(R.drawable.delivery_round_btn_white)
            deliveryTab.setBackgroundResource(R.drawable.rounded_view_grey_tab_bg)
            addressConfirmationDelivery.visibility = View.GONE
        }
    }
}