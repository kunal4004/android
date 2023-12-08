package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.ProductNotAvailableForCollectionDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class ProductNotAvailableForCollectionDialog : WBottomSheetDialogFragment() {

    private lateinit var binding: ProductNotAvailableForCollectionDialogBinding
    private var listener: IProductNotAvailableForCollectionDialogListener? = null

    interface IProductNotAvailableForCollectionDialogListener {
        fun onChangeDeliveryOption()
        fun onChangeDeliveryOptionFromNewToggleFulfilment()
        fun onFindInStore()
        fun openChangeFulfillmentScreen()
    }

    companion object {
        var dialogInstance = ProductNotAvailableForCollectionDialog()
        fun newInstance() = dialogInstance
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = parentFragment as IProductNotAvailableForCollectionDialogListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ProductNotAvailableForCollectionDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            findInStore?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            findInStore?.setOnClickListener {
                listener?.onFindInStore()
                dismissAllowingStateLoss()
            }

            changeDeliveryOption?.setOnClickListener {
                listener?.onChangeDeliveryOptionFromNewToggleFulfilment()
                dismissAllowingStateLoss()
            }
        }
    }
}