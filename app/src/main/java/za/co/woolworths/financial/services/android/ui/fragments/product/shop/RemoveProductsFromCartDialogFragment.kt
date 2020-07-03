package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.remove_items_from_cart_fragment.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.dto.CommerceItem
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.ui.adapters.ItemsListToRemoveFromCartAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class RemoveProductsFromCartDialogFragment : WBottomSheetDialogFragment() {

    private var listener: IRemoveProductsFromCartDialog? = null
    private var isItemsRemoved = false

    interface IRemoveProductsFromCartDialog {
        fun onOutOfStockProductsRemoved()
    }

    private var commerceItems: ArrayList<CommerceItem>? = null

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
        try {
            listener = parentFragment as IRemoveProductsFromCartDialog?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
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
        removeItems?.setOnClickListener { removeItemsFromCart() }
        cancel?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { dismissAllowingStateLoss() }
        }
        showListItems()
    }

    private fun showListItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = ItemsListToRemoveFromCartAdapter(it) }
    }

    private fun removeItemsFromCart() {
        showRemoveItemsProgressBar()
        commerceItems?.forEach {
            removeItem(it.commerceItemInfo.commerceId)
        }
    }

    private fun removeItem(commerceId: String) {
        OneAppService.removeCartItem(commerceId).enqueue(CompletionHandler(object : IResponseListener<ShoppingCartResponse> {
            override fun onSuccess(shoppingCartResponse: ShoppingCartResponse?) {
                onItemRemoved(commerceId)
            }
        }, ShoppingCartResponse::class.java))
    }

    private fun onItemRemoved(commerceId: String) {
        commerceItems?.find { it.commerceItemInfo.commerceId == commerceId }?.isItemRemoved = true

        if (commerceItems?.filter { commerceItem -> !commerceItem.isItemRemoved }.isNullOrEmpty()) {
            hideRemoveItemsProgressBar()
            isItemsRemoved = true
            dismissAllowingStateLoss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isItemsRemoved) {
            listener?.onOutOfStockProductsRemoved()
        } else {
            activity?.onBackPressed()
        }
    }

    fun hideRemoveItemsProgressBar() {
        progressRemoveItems?.visibility = View.INVISIBLE
        activity?.apply {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

    fun showRemoveItemsProgressBar() {
        progressRemoveItems?.visibility = View.VISIBLE
        activity?.apply {
            window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        }
    }

}