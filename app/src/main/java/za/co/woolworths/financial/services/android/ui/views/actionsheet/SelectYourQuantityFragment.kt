package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.databinding.SelectYourQuantityFragmentBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.cart.view.SubstitutionChoice
import za.co.woolworths.financial.services.android.contracts.IProductListing
import za.co.woolworths.financial.services.android.enhancedSubstitution.util.isEnhanceSubstitutionFeatureAvailable
import za.co.woolworths.financial.services.android.models.dto.AddItemToCart
import za.co.woolworths.financial.services.android.ui.adapters.SelectQuantityAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class SelectYourQuantityFragment(private val productListing: IProductListing?) : WBottomSheetDialogFragment() {

    private lateinit var binding: SelectYourQuantityFragmentBinding
    private var mAddItemToCart: AddItemToCart? = null

    companion object {
        private const val QUANTITY_IN_STOCK = "QUANTITY_IN_STOCK"
        fun newInstance(addItemToCart: AddItemToCart, productListing: IProductListing?) = SelectYourQuantityFragment(productListing).withArgs {
            putString(QUANTITY_IN_STOCK, Gson().toJson(addItemToCart))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val addItemToCart = arguments?.getString(QUANTITY_IN_STOCK, "")
        mAddItemToCart = Gson().fromJson(addItemToCart, AddItemToCart::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SelectYourQuantityFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.initQuantityItem()
    }

    private fun SelectYourQuantityFragmentBinding.initQuantityItem() {
        val selectQuantityAdapter = SelectQuantityAdapter { selectedQuantity: Int -> quantityItemClicked(selectedQuantity) }
        val quantityInStock = mAddItemToCart?.quantity ?: 0
        rclSelectYourQuantity?.apply {
            layoutManager = activity?.let { activity -> LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false) }
            layoutParams?.height = (Resources.getSystem()?.displayMetrics?.heightPixels
                    ?: 0) / (if (quantityInStock <= 2) 4 else 3)
            adapter = selectQuantityAdapter
        }
        selectQuantityAdapter.setItem(quantityInStock)

        btnCancelQuantity?.setOnClickListener { dismiss() }
    }

    private fun quantityItemClicked(quantity: Int) {
        mAddItemToCart?.apply {
            productListing?.addFoodProductTypeToCart(
                if (isEnhanceSubstitutionFeatureAvailable()) {
                    AddItemToCart(productId, catalogRefId, quantity, SubstitutionChoice.SHOPPER_CHOICE.name, "")
                } else {
                    AddItemToCart(productId, catalogRefId, quantity)
                }
            )
        }
        dismiss()
    }
}