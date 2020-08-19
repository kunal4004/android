package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.content.DialogInterface
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.product_not_available_for_collection_dialog.*
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class ProductNotAvailableForCollectionDialog : WBottomSheetDialogFragment() {

    private var listener: IProductNotAvailableForCollectionDialogListener? = null

    interface IProductNotAvailableForCollectionDialogListener {
        fun onChangeDeliveryOption()
        fun onFindInStore()
    }

    companion object {
        fun newInstance() = ProductNotAvailableForCollectionDialog()
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
        return inflater.inflate(R.layout.product_not_available_for_collection_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        findInStore?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        findInStore?.setOnClickListener {
            listener?.onFindInStore()
            dismissAllowingStateLoss()
        }

        changeDeliveryOption?.setOnClickListener {
            listener?.onChangeDeliveryOption()
            dismissAllowingStateLoss()
        }

    }
}