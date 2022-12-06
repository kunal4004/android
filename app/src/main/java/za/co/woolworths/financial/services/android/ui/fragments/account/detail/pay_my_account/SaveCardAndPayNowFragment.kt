package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.SaveCardAndPayNowFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class SaveCardAndPayNowFragment : BaseFragmentBinding<SaveCardAndPayNowFragmentBinding>(SaveCardAndPayNowFragmentBinding::inflate), View.OnClickListener {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            setToolbarItem()
            setProduct()
            populateField()

            saveCardPayNowButton?.apply {
                AnimationUtilExtension.animateViewPushDown(this)
                setOnClickListener(this@SaveCardAndPayNowFragment)
            }
        }
    }

    private fun SaveCardAndPayNowFragmentBinding.setProduct() {
        saveCardProductValueTextView?.text = bindString(payMyAccountViewModel.getProductLabelId())
        productTotalValueTextView?.text = payMyAccountViewModel.getCardDetail()?.amountEntered
    }

    private fun setToolbarItem() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun SaveCardAndPayNowFragmentBinding.populateField() {
        payMyAccountViewModel.getPayOrSaveNowCardDetails()?.apply {
            nameOnCardValueTextView?.text = first
            expiryDateValueTextView?.text = second
            cardNumberValueTextView?.text = third
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveCardPayNowButton -> {
                payMyAccountViewModel.setSaveAndPayCardNow(binding.pmaSaveCardCheckbox.isChecked)
                val navigateToProcessPayment = SaveCardAndPayNowFragmentDirections.actionSaveCardAndPayNowFragmentToPMAProcessRequestFragment()
                val options = NavOptions.Builder().setPopUpTo(R.id.saveCardAndPayNowFragment, true).build()
                view?.let { view -> Navigation.findNavController(view).navigate(navigateToProcessPayment, options) }
            }
        }
    }
}