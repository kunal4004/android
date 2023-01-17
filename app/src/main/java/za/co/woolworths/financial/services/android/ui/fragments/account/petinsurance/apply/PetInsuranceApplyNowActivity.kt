package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance.apply

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.databinding.ActivityPetInsuranceApplyNowBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.fragments.account.available_fund.AvailableFundFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ToastFactory
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.ViewState
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.BindingBaseActivity
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils

@AndroidEntryPoint
class PetInsuranceApplyNowActivity :
    BindingBaseActivity<ActivityPetInsuranceApplyNowBinding>(ActivityPetInsuranceApplyNowBinding::inflate) {
    val viewModel: PetInsuranceApplyNowViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setViews()
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.incPetInsuranceApplyNow.btnPetInsuranceApply -> {
                callAppGUID()
            }
        }
    }

    fun showHideLoading(isLoading: Boolean) {
        binding.incPetInsuranceApplyNow.apply {
            btnPetInsuranceApply.visibility = if (isLoading) GONE else VISIBLE
            pbPetInsuranceApply.visibility = if (isLoading) VISIBLE else GONE
        }
    }

    private fun callAppGUID() {
        showHideLoading(true)
        lifecycleScope.launch {
            viewModel.getAppGUID().collect { response ->
                when (response) {
                    is ViewState.RenderSuccess -> {
                        viewModel.appGUIDResponse.value = response.output
                        showHideLoading(false)
                        binding.apply {
                            response.output.apply {
                                if (!appGuid.isNullOrEmpty()) {
                                    val (_, renderMode, petInsuranceUrl, exitUrl) = AppConfigSingleton.accountOptions!!.insuranceProducts
                                    this@PetInsuranceApplyNowActivity.apply {
                                        KotlinUtils.petInsuranceRedirect(
                                            this, petInsuranceUrl + appGuid,
                                            renderMode == AvailableFundFragment.WEBVIEW, exitUrl
                                        )
                                        finish()
                                    }
                                }
                            }
                        }
                    }
                    is ViewState.RenderFailure,
                    is ViewState.RenderErrorFromResponse -> {
                        errorDialog()
                        showHideLoading(false)
                    }
                    is ViewState.Loading,
                    is ViewState.RenderEmpty -> {
                    }
                    is ViewState.RenderNoConnection -> {
                        ToastFactory.showNoConnectionFound(this@PetInsuranceApplyNowActivity)
                        showHideLoading(false)
                    }
                }
            }
        }
    }


    private fun setViews() {
        Utils.sessionDaoSave(SessionDao.KEY.PET_INSURANCE_INTRODUCTION_SHOWED,"1")
        setClickListeners(binding.incPetInsuranceApplyNow.btnPetInsuranceApply)
        setUpActionBar(binding.toolbarPetInsurance)
    }
}