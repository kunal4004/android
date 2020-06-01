package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.remove_items_from_cart_fragment.*
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.ui.adapters.ItemsListToRemoveFromCartAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

class RemoveProductsFromCartDialogFragment : WBottomSheetDialogFragment() {

    var commerceItems: ArrayList<CommerceItem>? = null

    companion object {
        fun newInstance(commerceItems: ArrayList<CommerceItem>) = RemoveProductsFromCartDialogFragment().withArgs {
            putString("ITEMS_LIST", Gson().toJson(commerceItems))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            commerceItems = Gson().fromJson(getString("ITEMS_LIST"), object : TypeToken<List<CommerceItem>>() {}.type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.remove_items_from_cart_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rcvItemsList?.setOnTouchListener { v, event ->
            v?.parent?.requestDisallowInterceptTouchEvent(true)
            v?.onTouchEvent(event)
            true
        }
        showListItems()
    }

    private fun showListItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = ItemsListToRemoveFromCartAdapter(it) }
    }
}