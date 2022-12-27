package za.co.woolworths.financial.services.android.ui.fragments.product.shop

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.FoodProductNotAvailableFromFbhStoreDialogBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class FoodProductNotAvailableForCollectionDialog : WBottomSheetDialogFragment() {

    private var listener: IProductNotAvailableForCollectionDialogListener? = null
    private lateinit var binding: FoodProductNotAvailableFromFbhStoreDialogBinding
    interface IProductNotAvailableForCollectionDialogListener {
        fun onChangeDeliveryOption()
        fun onFindInStore()
    }

    companion object {
        fun newInstance() = FoodProductNotAvailableForCollectionDialog()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            listener = parentFragment as? IProductNotAvailableForCollectionDialogListener?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FoodProductNotAvailableFromFbhStoreDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       binding.apply {
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
}