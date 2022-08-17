package za.co.woolworths.financial.services.android.checkout.view

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_collection_dates_bottom_sheet_dialog.*
import za.co.woolworths.financial.services.android.checkout.service.network.Week
import za.co.woolworths.financial.services.android.checkout.view.CheckoutReturningUserCollectionFragment.Companion.REQUEST_KEY_SELECTED_COLLECTION_DATE
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.picker.WheelView

class CollectionDatesBottomSheetDialog : WBottomSheetDialogFragment(),
    WheelView.OnItemSelectedListener<Any> {

    private var selectedPosition: Int = 0
    private var dataList: ArrayList<Week> = ArrayList(0)

    companion object {
        const val ARGS_KEY_COLLECTION_DATES = "ARGS_KEY_COLLECTION_DATES"
        const val ARGS_KEY_SELECTED_POSITION = "ARGS_KEY_SELECTED_POSITION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getSerializable(ARGS_KEY_COLLECTION_DATES)?.apply {
            dataList = (this as? ArrayList<*> ?: ArrayList<Week>(0)) as ArrayList<Week>
            selectedPosition = 0
        }
        selectedPosition = arguments?.getInt(ARGS_KEY_SELECTED_POSITION, 0) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(
            R.layout.fragment_collection_dates_bottom_sheet_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tvCancelButton?.paintFlags = Paint.UNDERLINE_TEXT_FLAG
        tvCancelButton?.setOnClickListener {
            dismissAllowingStateLoss()
        }
        tvConfirmCollectionDate?.setOnClickListener {
            if (selectedPosition < 0 || selectedPosition >= dataList.size) {
                return@setOnClickListener
            }
            setFragmentResult(
                REQUEST_KEY_SELECTED_COLLECTION_DATE,
                bundleOf(
                    REQUEST_KEY_SELECTED_COLLECTION_DATE to dataList?.get(selectedPosition),
                    ARGS_KEY_SELECTED_POSITION to selectedPosition
                )
            )
            dismissAllowingStateLoss()
        }

        collectionDatePickerWheelView?.apply {
            data = dataList.map { it.date }
            selectedItemPosition = selectedPosition
        }
        collectionDatePickerWheelView?.onItemSelectedListener = this
    }

    override fun onItemSelected(wheelView: WheelView<Any>?, data: Any?, position: Int) {
        when (wheelView?.id) {
            R.id.collectionDatePickerWheelView -> {
                selectedPosition = position
            }
        }
    }
}