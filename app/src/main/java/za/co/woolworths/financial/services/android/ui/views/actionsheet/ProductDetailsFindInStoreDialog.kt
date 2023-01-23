package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.NoQuantityFindStoreFragmentBinding
import za.co.woolworths.financial.services.android.ui.fragments.product.shop.ProductNotAvailableForCollectionDialog.IProductNotAvailableForCollectionDialogListener

/**
 * Created by Kunal Uttarwar on 07/12/22.
 */
class ProductDetailsFindInStoreDialog(private val productDetailsListener: IProductNotAvailableForCollectionDialogListener?) :
    WBottomSheetDialogFragment() {

    private lateinit var binding: NoQuantityFindStoreFragmentBinding

    companion object {
        fun newInstance(productDetailsListener: IProductNotAvailableForCollectionDialogListener?) =
            ProductDetailsFindInStoreDialog(productDetailsListener)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = NoQuantityFindStoreFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnNavigateToFindInStore?.setOnClickListener {
            dismiss()
            productDetailsListener?.onFindInStore()
        }
        binding.tvChangeFulfillment?.setOnClickListener {
            dismiss()
            productDetailsListener?.openChangeFulfillmentScreen()
        }
    }
}