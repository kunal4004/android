package za.co.woolworths.financial.services.android.ui.views.actionsheet.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ListItemPopupDialogBinding
import za.co.woolworths.financial.services.android.ui.adapters.CliSupplyDetailListAdapter
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class SupplyInfoDetailFragment : WBottomSheetDialogFragment() {

    enum class SupplyDetailViewType { INCOME, EXPENSE }

    private val incomeDetailsInfoList = mutableListOf(
        R.string.income_gross_monthly_title to R.string.income_gross_monthly_desc,
        R.string.income_net_monthly_title to R.string.income_net_monthly_desc,
        R.string.income_additional_monthly_title to R.string.income_additional_monthly_desc
    ) to mutableListOf(
        R.string.incomeGrossMonthlyInfoTitle to R.string.incomeGrossMonthlyInfoDesc,
        R.string.netMonthlyIncomeInfoTitle to R.string.netMonthlyIncomeInfoDesc,
        R.string.additionalMonthlyIncomeInfoTitle to R.string.additionalMonthlyIncomeInfoDesc)

    private val expenseDetailsInfoList = mutableListOf(
        R.string.expenses_mortgage_payment to R.string.expenses_mortgage_payment_desc,
        R.string.expenses_rental_payment to R.string.expenses_rental_payment_desc,
        R.string.expenses_maintenance_payment to R.string.expenses_maintenance_payment_desc,
        R.string.expense_monthly_credit_payment to R.string.expense_monthly_credit_payment_desc,
        R.string.cli_other_expenses to R.string.cli_overlay_all_expenses

    ) to mutableListOf(
        R.string.expensesMortgagePaymentInfoTitle to R.string.expensesMortgagePaymentInfoDesc,
        R.string.rentalPaymentsInfoTitle to R.string.rentalPaymentsInfoDesc,
        R.string.maintenanceExpensesInfoTitle to R.string.maintenanceExpensesInfoDesc,
        R.string.monthlyCreditPaymentsInfoTitle to R.string.monthlyCreditPaymentsInfoDesc,
        R.string.otherExpensesInfoTitle to R.string.otherExpensesInfoDesc
    )

    companion object {
        private const val SUPPLY_DETAIL_VIEW_TYPE = "SupplyDetailViewType"
        fun newInstance(popupType: SupplyDetailViewType) =
            SupplyInfoDetailFragment().withArgs {
                putSerializable(SUPPLY_DETAIL_VIEW_TYPE, popupType)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.list_item_popup_dialog,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args = arguments?.getSerializable(SUPPLY_DETAIL_VIEW_TYPE) as SupplyDetailViewType
        val binding = ListItemPopupDialogBinding.bind(view)
        val supplyDetailItems = if (args == SupplyDetailViewType.INCOME) incomeDetailsInfoList else expenseDetailsInfoList
        val supplyDetailListAdapter = CliSupplyDetailListAdapter(supplyDetailItems)
        binding.supplyDetailRecyclerview.apply {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = supplyDetailListAdapter
        }
        binding.supplyDetailGotItButton.setOnClickListener { dismiss() }

    }
}