package za.co.woolworths.financial.services.android.ui.views

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.UnsellableItemsBottomSheetDialogBinding
import za.co.woolworths.financial.services.android.geolocation.model.request.ConfirmLocationParams
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmAddressViewModel
import za.co.woolworths.financial.services.android.geolocation.viewmodel.ConfirmLocationResponseLiveData
import za.co.woolworths.financial.services.android.models.dto.UnSellableCommerceItem
import za.co.woolworths.financial.services.android.ui.adapters.UnsellableItemsListAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.ui.wfs.theme.OpenSansFontFamily
import za.co.woolworths.financial.services.android.util.UnsellableUtils
import za.co.woolworths.financial.services.android.util.analytics.FirebaseAnalyticsEventHelper
import za.co.woolworths.financial.services.android.util.wenum.Delivery

/**
 * Created by Kunal Uttarwar on 11/05/22.
 */
class UnsellableItemsBottomSheetDialog(
    val deliveryType: Delivery,
    val progressBar: ProgressBar,
    val confirmAddressViewModel: ConfirmAddressViewModel,
    val currentFragment: Fragment,
) : WBottomSheetDialogFragment(),
    View.OnClickListener {

    private lateinit var binding: UnsellableItemsBottomSheetDialogBinding
    var bundle: Bundle? = null
    private var commerceItems: ArrayList<UnSellableCommerceItem>? = null
    private var isCheckBoxSelected = true

    companion object {
        const val KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS = "UnSellableCommerceItems"

        fun newInstance(
            unsellableItemsList: ArrayList<UnSellableCommerceItem>,
            deliveryType: Delivery,
            progressBar: ProgressBar,
            viewModel: ConfirmAddressViewModel,
            fragment: Fragment,
        ) =
            UnsellableItemsBottomSheetDialog(deliveryType, progressBar, viewModel, fragment).withArgs {
                putSerializable(KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS, unsellableItemsList)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = UnsellableItemsBottomSheetDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.init()
        initView()
    }

    private fun initView() {
        binding.saveToListTextView.setContent {
            Text(
                text = pluralStringResource(
                    id = R.plurals.save_item_to_list_text,
                    count = commerceItems?.size ?: 0
                ),
                style = TextStyle(
                    fontFamily = OpenSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            )
        }
        val checkedState = mutableStateOf(true)
        binding.saveToListCheckBox.setContent {
            Checkbox(
                colors = CheckboxDefaults.colors(
                    uncheckedColor = Color(
                        android.graphics.Color.parseColor(
                            "#D8D8D8"
                        )
                    ),
                    checkedColor = Color.Black
                ),
                checked = checkedState.value,
                onCheckedChange = {
                    checkedState.value = it
                    onCheckBoxChanged(it)
                }
            )
        }
    }

    private fun onCheckBoxChanged(checked: Boolean) {
        isCheckBoxSelected = checked
        binding.removeItems.text =
            if (checked) getString(R.string.continueLabel) else getString(R.string.remove_and_continue)
    }

    private fun UnsellableItemsBottomSheetDialogBinding.init() {
        incSwipeCloseIndicator?.root?.visibility = View.VISIBLE
        removeItems?.setOnClickListener(this@UnsellableItemsBottomSheetDialog)
        arguments?.apply {
            commerceItems =
                getSerializable(KEY_ARGS_UNSELLABLE_COMMERCE_ITEMS) as? ArrayList<UnSellableCommerceItem>
        }

        val itemCount = commerceItems?.size ?: 0
        unsellableSubTitle?.text = getString(R.string.unsellable_subtitle)
        unsellableTitle?.text = when (deliveryType.name) {
            Delivery.STANDARD.name -> {
                resources.getQuantityText(R.plurals.remove_items_standard_dialog_title, itemCount)
            }

            Delivery.CNC.name -> {
                resources.getQuantityText(R.plurals.remove_items_cnc_dialog_title, itemCount)
            }

            Delivery.DASH.name -> {
                resources.getQuantityText(R.plurals.remove_items_dash_dialog_title, itemCount)
            }

            else -> {
                resources.getQuantityText(R.plurals.remove_items_standard_dialog_title, itemCount)
            }
        }
        cancelBtn?.apply {
            visibility = View.VISIBLE
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener(this@UnsellableItemsBottomSheetDialog)
        }
        loadUnsellableItems()
    }

    private fun UnsellableItemsBottomSheetDialogBinding.loadUnsellableItems() {
        rcvItemsList?.layoutManager = LinearLayoutManager(activity)
        commerceItems?.let { rcvItemsList?.adapter = UnsellableItemsListAdapter(it) }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel_btn -> {
                ConfirmLocationResponseLiveData.value = false
                confirmRemoveItems()
            }

            R.id.removeItems -> {
                commerceItems?.let { unsellableItems ->
                    FirebaseAnalyticsEventHelper.removeFromCartUnsellable(unsellableItems)
                }
                UnsellableUtils.callConfirmPlace(
                    currentFragment,
                    if (isCheckBoxSelected) ConfirmLocationParams(commerceItems, null) else null,
                    progressBar,
                    confirmAddressViewModel,
                    deliveryType
                )
                confirmRemoveItems()
            }
        }
    }

    private fun confirmRemoveItems() {
        dismiss()
    }
}