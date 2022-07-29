package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.main.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AccountLandingDialogFragmentBinding
import za.co.woolworths.financial.services.android.ui.base.ViewBindingDialogFragment
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.DialogData

class AccountLandingDialogFragment : ViewBindingDialogFragment<AccountLandingDialogFragmentBinding>(), View.OnClickListener {

    val args: AccountLandingDialogFragmentArgs by navArgs()
    val viewModel by viewModels<AccountLandingDialogViewModel>()

    override fun inflateViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): AccountLandingDialogFragmentBinding {
        return AccountLandingDialogFragmentBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()

    }

    private fun setupViewModel() {
        viewModel.setup(args)
        viewModel.dialogData.observe(viewLifecycleOwner) {
            setDialogViews(it)
        }
        viewModel.command.observe(viewLifecycleOwner) {

        }
    }

    fun setDialogViews(dialogData: DialogData?) {
        dialogData?.let {
            with(binding) {
                accountInArrearsTitleTextView.text = getString(it.title)
                accountInArrearsDescriptionTextView.text = getString(it.desc, it.formattedValue)

                payNowButton.apply {
                    text = bindString(it.firstButtonTitle)
                    setOnClickListener(this@AccountLandingDialogFragment)
                }
                chatToUsButton.apply {
                    text = bindString(it.secondButtonTitle)
                    visibility = it.secondButtonVisibility
                    setOnClickListener(this@AccountLandingDialogFragment)
                }
                closeIconImageButton.setOnClickListener(this@AccountLandingDialogFragment)
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.payNowButton -> {
                viewModel.handlePayNowClick()
            }
            R.id.chatToUsButton -> {
                viewModel.handleCallUsClick()
            }
            R.id.closeIconImageButton -> {
                dismiss()
            }
        }

    }


//    var resultLauncher =
//        activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == Activity.RESULT_OK) {
//                // There are no request codes
//                val data: Intent? = result.data
////            doSomeOperations()
//            }
//        }
//
//    fun navigateToGetAPayment() {
//        val intent = Intent(activity, GetAPaymentPlanActivity::class.java)
//        intent.putExtra(
//            ViewTreatmentPlanDialogFragment.ELIGIBILITY_PLAN,
//            viewModel.eligibilityPlan.value
//        )
//        resultLauncher?.launch(intent)
//    }
}

