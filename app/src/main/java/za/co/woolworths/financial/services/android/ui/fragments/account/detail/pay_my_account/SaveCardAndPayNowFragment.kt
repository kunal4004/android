package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.save_card_and_pay_now_fragment.*
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class SaveCardAndPayNowFragment : Fragment(), View.OnClickListener {

    val payMyAccountViewModel: PayMyAccountViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.save_card_and_pay_now_fragment, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarItem()
        setProduct()
        populateField()

        saveCardPayNowButton?.apply {
            AnimationUtilExtension.animateViewPushDown(this)
            setOnClickListener(this@SaveCardAndPayNowFragment)
        }
    }

    private fun setProduct() {
        saveCardProductValueTextView?.text = bindString(payMyAccountViewModel.getProductLabelId())
        productTotalValueTextView?.text = payMyAccountViewModel.getAmountEntered()
    }

    private fun setToolbarItem() {
        (activity as? PayMyAccountActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(false)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun populateField() {
        payMyAccountViewModel.getPayOrSaveNowCardDetails()?.apply {
            nameOnCardValueTextView?.text = first
            expiryDateValueTextView?.text = second
            cardNumberValueTextView?.text = third
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.saveCardPayNowButton -> {
                payMyAccountViewModel.setSaveAndPayCardNow(pmaSaveCardCheckbox.isChecked)
                val navigateToProcessPayment = SaveCardAndPayNowFragmentDirections.actionSaveCardAndPayNowFragmentToPMAProcessRequestFragment()
                val options = NavOptions.Builder().setPopUpTo(R.id.saveCardAndPayNowFragment, true).build()
                view?.let { view -> Navigation.findNavController(view).navigate(navigateToProcessPayment, options) }
            }
        }
    }
}