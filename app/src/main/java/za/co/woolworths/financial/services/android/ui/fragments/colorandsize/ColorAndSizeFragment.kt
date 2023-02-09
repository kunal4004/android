package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.buildSpannedString
import androidx.recyclerview.widget.GridLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentColorAndSizeBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.OtherSkus
import za.co.woolworths.financial.services.android.models.dto.ProductDetails
import za.co.woolworths.financial.services.android.models.dto.WProductDetail
import za.co.woolworths.financial.services.android.ui.fragments.product.utils.ColourSizeVariants
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

    private var productItem: ProductDetails? = null
    private var selectedSku: OtherSkus? = null
    private var defaultSku: OtherSkus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            productItem = Gson().fromJson(
                getString(ARG_PRODUCT_ITEM, null),
                ProductDetails::class.java
            )?.apply {
                defaultSku = getDefaultSku(otherSkus, sku)
                selectedSku = defaultSku
            }
        }
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
            selectedSku?.let { it1 -> colorAndSizeBottomSheetListener.setSelectedSku(it1) }
            dismiss()
        }

        initColorList()
        initSizeList()
    }

    private fun initSizeList() {
        if (!isAdded) return

        val sizeList = getSizeListByColor(selectedSku?.colour)
        setSizeLayoutVisibility(sizeList)
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

            if (sizeList.size == 1) {
                sizeList.getOrNull(0)?.let {
                    sizeAdapter?.setSelection(sizeList.getOrNull(0))
                    onSizeSelection(it)
                }
            }
        }
    }

    override fun onSizeSelection(selectedSku: OtherSkus) {
        this@ColorAndSizeFragment.selectedSku = selectedSku
        showSelectedSize(selectedSku)
    }

    private fun initColorList() {
        if (!isAdded) return

        binding.sizeColorSelectorLayout.apply {
            colorPlaceholder.text = requireContext().getString(R.string.color)

            val spanCount = Utils.calculateNoOfColumns(activity, 55F)
            val colorsList: List<OtherSkus> = getDistinctColors(productItem?.otherSkus)
            setColorLayoutVisibility(colorsList)

            colorAdapter = ColorAdapter(
                colorsList = colorsList,
                listener = this@ColorAndSizeFragment,
                spanCount = spanCount,
                selectedSku = defaultSku
            ).apply {
                colorSelectorRecycleView.layoutManager =
                    GridLayoutManager(requireContext(), spanCount)
                colorSelectorRecycleView.adapter = this
                showSelectedColor()
            }

            colorsList.size.let {
                if (it > spanCount) {
                    val moreColorCount = it - spanCount
                    moreColor.text =
                        requireContext().getString(
                            R.string.product_details_color_count,
                            moreColorCount
                        )
                    moreColor.visibility = VISIBLE
                    moreColor.setOnClickListener {
                        onShowMoreClick(spanCount)
                    }
                }
            }

            colorSelectorLayout.visibility = VISIBLE
            divider1.visibility = GONE
        }
    }

    private fun onShowMoreClick(spanCount: Int) {

        if ((colorAdapter?.itemCount ?: 0) > spanCount) { //Already expanded
            //Show less
            val moreColorCount = colorAdapter?.itemCount?.minus(spanCount)
            moreColorCount?.let {// Show + more count
                binding.sizeColorSelectorLayout.moreColor.text =
                    requireContext().getString(
                        R.string.product_details_color_count,
                        moreColorCount
                    )
            }
            colorAdapter?.showLess()

        } else { // already collapsed
            colorAdapter?.showMoreColors()
            binding.sizeColorSelectorLayout.moreColor.text = requireContext()
                .getString(R.string.show_less)
        }
    }

    private fun getDistinctColors(otherSkus: List<OtherSkus>?): List<OtherSkus> {
        val commonColorSku: List<OtherSkus> = otherSkus?.sortedBy { it.colour }?.distinctBy {
            it.colour ?: it.size
        } ?: listOf()
        return commonColorSku
    }

    private fun showSelectedColor() {
        requireActivity().apply {
            selectedSku.let {
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

    private fun getDefaultSku(otherSku: List<OtherSkus>?, sku: String?): OtherSkus? {
        val defaultSku = otherSku?.filter {
            it.sku == sku
        }
        return defaultSku?.get(0)
    }

    private fun setSizeLayoutVisibility(sizeList: List<OtherSkus>) {
        if (!isAdded) return
        with(binding.sizeColorSelectorLayout) {
            sizeSelectorLayout.visibility =
                if (sizeList.isEmpty()) GONE else VISIBLE
        }
    }

    private fun setColorLayoutVisibility(colorsList: List<OtherSkus>) {
        if (!isAdded) return
        with(binding.sizeColorSelectorLayout) {
            colorSelectorLayout.visibility =
                if (colorsList.isEmpty()) GONE else VISIBLE
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
        selectedSku = selectedColor
        showSelectedColor()
        val (_, hasSize) = getColorAndSizeAvailability()
        if (hasSize) updateSizesOnColorSelection()
    }

    private fun getColorAndSizeAvailability(): Pair<Boolean, Boolean> {
        return when (ColourSizeVariants.find(productItem?.colourSizeVariants ?: "")) {
            ColourSizeVariants.DEFAULT, ColourSizeVariants.NO_VARIANT -> {
                Pair(false, false)
            }
            ColourSizeVariants.COLOUR_VARIANT -> {
                Pair(true, false)
            }
            ColourSizeVariants.SIZE_VARIANT, ColourSizeVariants.COLOUR_SIZE_VARIANT -> {
                Pair(true, true)
            }
            ColourSizeVariants.NO_COLOUR_SIZE_VARIANT -> {
                Pair(false, true)
            }
            else -> {
                Pair(false, false)
            }
        }
    }

    private fun updateSizesOnColorSelection() {
        val sizeList = getSizeListByColor(selectedSku?.colour)
        sizeList.let { sizeAdapter?.updatedSizes(ArrayList(it)) }

        //===== positive flow
        // if selected size available for the selected color
        // get the sku for the selected size from the new color group
        // update the selectedSizeSKU

        //===== negative flow
        // if selected size not available on the new color group
        // make selectedSKU to null

        selectedSku?.let { selected ->

            val index: Int = sizeList.indexOfFirst { it ->
                it.size.equals(selected.size, true)
            }

            when (index) {
                -1 -> {
                    val otherSku: OtherSkus? = sizeList.filter { it.quantity > 0 }.getOrNull(0)
                    selectedSku = otherSku
                    sizeAdapter?.apply {
                        selectedSku?.let { setSelection(it) } ?: clearSelection()
                    }
                }
                else -> {
                    selectedSku = (sizeList.getOrNull(index))
                    sizeAdapter?.setSelection(selectedSku)
                }
            }
            showSelectedSize(selectedSku)
        }
    }

    private fun getSizeListByColor(colour: String?): List<OtherSkus> {
        val sizeList: List<OtherSkus> = productItem?.otherSkus?.filter {
            it.colour.equals(colour, ignoreCase = true)
        }?.distinctBy { it.size } ?: emptyList()
        return sizeList
    }

    private fun showSelectedSize(selectedSku: OtherSkus?) {
        selectedSku?.let {
            binding.sizeColorSelectorLayout.selectedSize.text = buildSpannedString {
                append(CONST_HYPHEN_SEPARATOR)
                append(it.size)
            }
            binding.sizeColorSelectorLayout.selectedSizePlaceholder.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.black
                )
            )
        }
    }
}