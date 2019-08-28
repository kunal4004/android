package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.no_quantity_find_store_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.product.grid.ProductListingFindInStore

class ProductListingFindInStoreNoQuantityFragment : WBottomSheetDialogFragment() {

    private var mProductListingFindInStore: ProductListingFindInStore? = null
    private var mQuantityInStock: Int? = 0

    companion object {
        private const val QUANTITY_IN_STOCK = "QUANTITY_IN_STOCK"
        fun newInstance() = ProductListingFindInStoreNoQuantityFragment().withArgs {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mQuantityInStock = getInt(QUANTITY_IN_STOCK, 0)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.no_quantity_find_store_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            mProductListingFindInStore = ProductListingFindInStore(this)
            btnNavigateToFindInStore?.setOnClickListener { mProductListingFindInStore?.startLocationUpdate() }
        }
    }

    override fun onDetach() {
        super.onDetach()
        finishActivity()
    }

    private fun finishActivity() {

    }
}