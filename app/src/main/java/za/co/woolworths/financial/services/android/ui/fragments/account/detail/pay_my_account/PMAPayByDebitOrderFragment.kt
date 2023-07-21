package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account // ktlint-disable package-name

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.PmaPayByDebitOrderFragmentBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntity
import za.co.woolworths.financial.services.android.util.mappers.pma.EntityMapper.toDomain

class PMAPayByDebitOrderFragment : Fragment() {
    private lateinit var binding: PmaPayByDebitOrderFragmentBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.pma_pay_by_debit_order_fragment,
            container,
            false,
        )
        binding.payByDebitOrderModel =
            AppConfigSingleton.mPayMyAccount?.debitOrder?.let { PMAPayByDebitOrderEntity(it) }
                ?.toDomain()
        binding.executePendingBindings()
        return binding.root
    }
}
