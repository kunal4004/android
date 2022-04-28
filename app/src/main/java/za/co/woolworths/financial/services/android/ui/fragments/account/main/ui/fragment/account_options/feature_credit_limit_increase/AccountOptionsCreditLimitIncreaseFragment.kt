package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_credit_limit_increase

import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.databinding.AccountOptionsCreditLimitIncreaseFragmentBinding
import com.google.gson.Gson
import kotlinx.coroutines.flow.collect
import za.co.woolworths.financial.services.android.ui.base.ViewBindingFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderEmpty
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess

class AccountOptionsCreditLimitIncreaseFragment :
    ViewBindingFragment<AccountOptionsCreditLimitIncreaseFragmentBinding>(
        AccountOptionsCreditLimitIncreaseFragmentBinding::inflate
    ) {

    private val viewModel: CreditLimitIncreaseViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.progressCreditLimit.indeterminateDrawable?.setColorFilter(
            Color.BLACK,
            PorterDuff.Mode.MULTIPLY
        )
        subscribeObservers()
    }

    private fun subscribeObservers() {
        lifecycleScope.launchWhenStarted {
            viewModel.queryRemoteServiceCLIOfferActive().collect { result ->
                with(result) {
                    renderSuccess { Log.e("renderLoadingX", "xxx ${Gson().toJson(output)} xx") }
                    renderEmpty { Log.e("renderLoadingX", "xxx empty xx") }
                    renderFailure { Log.e("renderLoadingX", "xxx failure xx") }
                    renderLoading { showProgress(this.isLoading) }
                }
            }
        }
    }

    private fun showProgress(isLoading: Boolean = false) {
        when (isLoading) {
            true -> {
                binding.progressCreditLimit.visibility = VISIBLE
                binding.llCommonLayer.visibility = GONE
                binding.relIncreaseMyLimit.visibility = GONE
            }
            false -> {
                binding.progressCreditLimit.visibility = GONE
                binding.llCommonLayer.visibility = VISIBLE
                binding.relIncreaseMyLimit.visibility = VISIBLE
            }
        }
    }
}