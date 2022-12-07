package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.databinding.SelectYourQuantityFragmentBinding
import za.co.woolworths.financial.services.android.ui.adapters.SelectQuantityAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class QuantitySelectorFragment(private val listener: IQuantitySelector?) : WBottomSheetDialogFragment() {


    interface IQuantitySelector {
        fun onQuantitySelection(quantity: Int)
    }

    private lateinit var binding: SelectYourQuantityFragmentBinding
    private var quantityInStock: Int? = 0

    companion object {
        private const val QUANTITY_IN_STOCK = "QUANTITY_IN_STOCK"
        fun newInstance(quantity: Int, listener: IQuantitySelector?) = QuantitySelectorFragment(listener).withArgs {
            putInt(QUANTITY_IN_STOCK, quantity)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        quantityInStock = arguments?.getInt(QUANTITY_IN_STOCK, 0)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = SelectYourQuantityFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initQuantityItem()
    }

    private fun initQuantityItem() {
        val selectQuantityAdapter = SelectQuantityAdapter { selectedQuantity: Int -> quantityItemClicked(selectedQuantity) }

        with(binding) {
            quantityInStock?.let {
                val dividerFactor = when (it) {
                    1 -> 11
                    2 -> 6
                    3 -> 4
                    else -> 3
                }

                rclSelectYourQuantity?.apply {
                    layoutManager = activity?.let { activity ->
                        LinearLayoutManager(
                            activity,
                            LinearLayoutManager.VERTICAL,
                            false
                        )
                    }
                    layoutParams?.height = (Resources.getSystem()?.displayMetrics?.heightPixels
                        ?: 0) / dividerFactor
                    adapter = selectQuantityAdapter
                }
                selectQuantityAdapter.setItem(it)
            }

            btnCancelQuantity?.setOnClickListener { dismiss() }
        }
    }

    private fun quantityItemClicked(quantity: Int) {
        listener?.onQuantitySelection(quantity)
        dismiss()
    }
}