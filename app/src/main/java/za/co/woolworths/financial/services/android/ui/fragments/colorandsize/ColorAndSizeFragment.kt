package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentColorAndSizeBinding
import com.google.gson.Gson
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.WProductDetail
import za.co.woolworths.financial.services.android.ui.activities.product.ProductInformationActivity
import za.co.woolworths.financial.services.android.ui.extension.underline
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

interface ColorAndSizeBottomSheetListener {
    fun setSelectedSku(selectedSku: OtherSkus)
    fun onCancelColorAndSize()
}

class ColorAndSizeFragment : WBottomSheetDialogFragment(), ColorAndSizeListener {

    private var colorAdapter: ColorAdapter? = null
    private var sizeAdapter: SizeAdapter? = null
    private lateinit var colorAndSizeBottomSheetListener: ColorAndSizeBottomSheetListener
    private var _binding: FragmentColorAndSizeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ColorAndSizeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.uiSizeState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when (state) {
                    UiState.Loading -> setSizeLayoutVisibility(GONE)
                    is UiState.Success -> {
                        if (state.data.isEmpty() || !state.isAvailable) {
                            setSizeLayoutVisibility(GONE)
                            return@onEach
                        }
                        initSizeList(state.isAvailable, state.defaultSelection, state.sizeGuideId, state.data)
                        setSizeLayoutVisibility(VISIBLE)
                    }
                }
            }.launchIn(lifecycleScope)

        viewModel.uiColorState
            .flowWithLifecycle(lifecycle = lifecycle, Lifecycle.State.STARTED)
            .onEach { state ->
                when (state) {
                    UiState.Loading -> setColorLayoutVisibility(GONE)
                    is UiState.Success -> {
                        if (state.data.isEmpty() || !state.isAvailable) {
                            setColorLayoutVisibility(GONE)
                            return@onEach
                        }
                        initColorList(state.data)
                        setColorLayoutVisibility(VISIBLE)
                    }
                }
            }.launchIn(lifecycleScope)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentColorAndSizeBinding.inflate(inflater, container, false)
        return _binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvConfirmButton.setOnClickListener {
            viewModel.selectedSku?.let { it1 -> colorAndSizeBottomSheetListener.setSelectedSku(it1) }
            dismiss()
        }
    }

    private fun initSizeList(
        hasSize: Boolean,
        defaultSelection: Boolean,
        sizeGuideId: String?,
        sizeList: List<OtherSkus>
    ) {
        if (!isAdded) return

        binding.sizeColorSelectorLayout.apply {
            sizeAdapter = SizeAdapter(
                requireActivity(),
                ArrayList(sizeList),
                listener = this@ColorAndSizeFragment
            )
            sizeSelectorRecycleView.apply {
                adapter = sizeAdapter
                layoutManager = GridLayoutManager(activity, 4)
            }

            val isSizeGuideApplicable = (!sizeGuideId.isNullOrEmpty() && hasSize)
            if (isSizeGuideApplicable) {
                sizeGuide.apply {
                    underline()
                    visibility = VISIBLE
                    setOnClickListener {
                        viewModel.productItem?.let { productDetails ->
                            onSizeGuideClick(
                                productDetails
                            )
                        }
                    }
                }
            }
            if (!defaultSelection) {
                binding.tvConfirmButton.isEnabled = false
                binding.tvConfirmButton.alpha = 0.5f
                return
            }

            binding.tvConfirmButton.isEnabled = true
            binding.tvConfirmButton.alpha = 1f

            if (sizeList.size == 1) {
                sizeList.getOrNull(0)?.let {
                    sizeAdapter?.setSelection(sizeList.getOrNull(0))
                    onSizeSelection(it)
                }
            } else {
                viewModel.selectedSku?.let { selected ->
                    val index: Int = sizeList.indexOfFirst { it ->
                        it.size.equals(selected.size, true)
                    }

                    when (index) {
                        -1 -> {
                            val otherSku: OtherSkus? =
                                sizeList.filter { it.quantity > 0 }.getOrNull(0)
                            viewModel.selectedSku = otherSku
                            sizeAdapter?.apply {
                                viewModel.selectedSku?.let { setSelection(it) } ?: clearSelection()
                            }
                        }
                        else -> {
                            viewModel.selectedSku = (sizeList.getOrNull(index))
                            sizeAdapter?.setSelection(viewModel.selectedSku)
                        }
                    }
                    showSelectedSize()
                }
            }
        }
    }

    private fun onSizeGuideClick(productItem: ProductDetails) {
        activity?.apply {
            val intent = Intent(this, ProductInformationActivity::class.java)
            intent.putExtra(
                ProductInformationActivity.PRODUCT_DETAILS,
                Utils.toJson(productItem)
            )
            intent.putExtra(
                ProductInformationActivity.PRODUCT_INFORMATION_TYPE,
                ProductInformationActivity.ProductInformationType.SIZE_GUIDE
            )
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left)
        }
    }

    override fun onSizeSelection(selectedSku: OtherSkus) {
        binding.tvConfirmButton.isEnabled = true
        binding.tvConfirmButton.alpha = 1f
        viewModel.selectedSku = selectedSku
        showSelectedSize()
    }

    private fun initColorList(colorList: List<OtherSkus>) {
        if (!isAdded) return

        binding.sizeColorSelectorLayout.apply {
            colorPlaceholder.text = requireContext().getString(R.string.color)
            divider1.visibility = GONE

            val spanCount = Utils.calculateNoOfColumns(activity, 55F)

            colorAdapter = ColorAdapter(
                colorsList = colorList,
                listener = this@ColorAndSizeFragment,
                spanCount = spanCount,
                selectedSku = viewModel.selectedSku
            ).apply {
                colorSelectorRecycleView.layoutManager =
                    GridLayoutManager(requireContext(), spanCount)
                colorSelectorRecycleView.adapter = this
                showSelectedColor()
            }

            if (colorList.size > spanCount) {
                with(moreColor) {
                    visibility = VISIBLE
                    text = requireContext().getString(
                        R.string.product_details_color_count,
                        colorList.size.minus(spanCount)
                    )
                    setOnClickListener { onMoreColorClick(spanCount) }
                }
            }
        }
    }

    private fun onMoreColorClick(spanCount: Int) {

        colorAdapter?.apply {
            if (itemCount > spanCount) { //Already expanded
                //Show less
                binding.sizeColorSelectorLayout.moreColor.text =
                    requireContext().getString(
                        R.string.product_details_color_count, itemCount.minus(spanCount)
                    )
                showLess()
            } else { // already collapsed
                showMoreColors()
                binding.sizeColorSelectorLayout.moreColor.text = requireContext()
                    .getString(R.string.show_less)
            }
        }
    }

    private fun showSelectedColor() {
        requireActivity().apply {
            viewModel.selectedSku.let {
                binding.sizeColorSelectorLayout.colorPlaceholder.setTextColor(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
                binding.sizeColorSelectorLayout.selectedColor.text = buildSpannedString {
                    append(CONST_HYPHEN_SEPARATOR)
                    val groupKey = it?.colour
                    append(if (groupKey.isNullOrEmpty()) "N/A" else groupKey)
                }
            }
        }
    }

    private fun setSizeLayoutVisibility(visibility: Int) {
        if (!isAdded) return
        with(binding.sizeColorSelectorLayout) {
            sizeSelectorLayout.visibility = visibility
            divider2.visibility = if(colorSelectorLayout.visibility == VISIBLE) visibility else GONE
        }
    }

    private fun setColorLayoutVisibility(visibility: Int) {
        if (!isAdded) return
        with(binding.sizeColorSelectorLayout) {
            colorSelectorLayout.visibility = visibility
            divider1.visibility = GONE // always gone as per UI
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        colorAndSizeBottomSheetListener.onCancelColorAndSize()
        dismiss()
    }

    companion object {
        private const val ARG_PRODUCT_ITEM = "productItem"
        private const val CONST_HYPHEN_SEPARATOR = "  -  "

        fun getInstance(
            listener: ColorAndSizeBottomSheetListener,
            productItem: WProductDetail,
        ): ColorAndSizeFragment {
            val bundle = Bundle().apply {
                putString(ARG_PRODUCT_ITEM, Gson().toJson(productItem))
            }
            return ColorAndSizeFragment().apply {
                arguments = bundle
                colorAndSizeBottomSheetListener = listener
            }
        }
    }

    override fun onColorSelection(selectedColor: OtherSkus?, isFromVto: Boolean) {
        viewModel.updateSizesOnColorSelection(selectedColor)
        showSelectedColor()
        viewModel.selectedSku?.let { sizeAdapter?.setSelection(it) } ?: sizeAdapter?.clearSelection()
        showSelectedSize()
    }

    private fun showSelectedSize() {
        viewModel.selectedSku?.let {
            binding.sizeColorSelectorLayout.selectedSize.text = buildSpannedString {
                append(CONST_HYPHEN_SEPARATOR)
                append(it.size)
            }
            binding.sizeColorSelectorLayout.selectedSizePlaceholder.setTextColor(
                ContextCompat.getColor(requireContext(), R.color.black)
            )
        }
    }
}