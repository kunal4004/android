package za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.loading

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityEmailUsLoadingBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderFailure
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderHttpFailureFromServer
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderLoading
import za.co.woolworths.financial.services.android.ui.fragments.account.main.core.renderSuccess
import za.co.woolworths.financial.services.android.ui.fragments.contact_us.enquiry.list.EnquiriesListViewModel

@AndroidEntryPoint
class EmailUsLoadingActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var binding: ActivityEmailUsLoadingBinding
    private val viewModel: EmailUsLoadingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.emailUsRequest.value = intent.extras?.getParcelable(EnquiriesListViewModel.EMAIL_US_REQUEST)
        binding = ActivityEmailUsLoadingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initView()
    }

    private fun initView() {
        binding.btnEmailUsLoadingGotIt.setOnClickListener(this)
        binding.btnEmailUsLoadingDismiss.setOnClickListener(this)
        lifecycleScope.launch {
            viewModel.userEmailResponse.collect { result ->
                with(result){
                    renderLoading { binding.pbEmailUsLoading.visibility = if (isLoading) VISIBLE else GONE }
                    renderSuccess { handleSuccess() }
                    renderHttpFailureFromServer { handleError(output.response?.message ?: getString(R.string.oops_error_message)) }
                    renderFailure { handleError(throwable.toString()) }
                }
            }
        }
        viewModel.postContactUsEmail()
    }

    private fun handleError(error: String) {
        binding.apply {
            groupImageButton.visibility = VISIBLE
            btnEmailUsLoadingDismiss.visibility = VISIBLE
            btnEmailUsLoadingGotIt.text = bindString(R.string.retry)
            tvEmailUsLoadingTitle.text = bindString(R.string.enquiry_failed)
            tvEmailUsLoadingDesc.text = error
            ivEmailUsLoading.setImageResource(R.drawable.ic_error_icon)
        }
    }

    private fun handleSuccess() {
        binding.apply {
            groupImageButton.visibility = VISIBLE
            btnEmailUsLoadingDismiss.visibility = GONE
            tvEmailUsLoadingTitle.text = bindString(R.string.enquiry_successfully_sent)
            tvEmailUsLoadingDesc.text = bindString(R.string.thanks_for_message)
        }
    }

    override fun onClick(view: View?) {
        when (view) {
            binding.btnEmailUsLoadingGotIt -> {
                if (binding.btnEmailUsLoadingGotIt.text.equals(bindString(R.string.retry))) {
                    retry()
                } else {
                    finish()
                }
            }
            binding.btnEmailUsLoadingDismiss -> {
                finish()
            }
        }
    }

    private fun retry() {
        binding.groupImageButton.visibility = GONE
        binding.pbEmailUsLoading.visibility = VISIBLE
        binding.tvEmailUsLoadingTitle.text = bindString(R.string.sending_enquiry)
        binding.tvEmailUsLoadingDesc.text = bindString(R.string.processing_your_request_desc)
        viewModel.postContactUsEmail()
    }


}
