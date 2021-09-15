package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.apply_promo_code_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard
import za.co.woolworths.financial.services.android.ui.extension.showKeyboard
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class ApplyPromoCodeFragment : Fragment(), VoucherAndPromoCodeContract.ApplyPromoCodeView, View.OnClickListener {

    var presenter: ApplyPromoCodePresenterImpl? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.apply_promo_code_fragment, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ApplyPromoCodePresenterImpl(this, ApplyPromoCodeInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.apply {
            etPromoCode?.let {
                it.showKeyboard(this as AppCompatActivity)
                it.afterTextChanged { onPromoCodeTextChanged(it) }
                it.filters = it.filters + InputFilter.AllCaps()
            }
        }
        cancel?.setOnClickListener(this)
        clear?.setOnClickListener(this)
        applyPromoCode?.setOnClickListener(this)
    }

    override fun onApplyPromoCodeSuccess(shoppingCartResponse: ShoppingCartResponse) {
        activity?.apply {
            setResult(Activity.RESULT_OK, Intent().putExtra("ShoppingCartResponse", Utils.toJson(shoppingCartResponse)))
            finish()
        }
    }

    override fun applyPromoCode() {
        activity?.apply {
            etPromoCode?.text.toString().trim().let {
                if (it.isNotEmpty()) {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_apply, this)
                    etPromoCode.hideKeyboard(activity as AppCompatActivity)
                    Handler().postDelayed({
                        showApplyPromoCodeProgress()
                        presenter?.initApplyPromoCode(it)
                    }, AppConstant.DELAY_300_MS)

                }
            }
        }
    }

    override fun showApplyPromoCodeProgress() {
        parentLayout?.visibility = View.GONE
        progressLayout?.visibility = View.VISIBLE
    }

    override fun hideApplyPromoCodeProgress() {
        parentLayout?.visibility = View.VISIBLE
        progressLayout?.visibility = View.GONE
    }

    override fun onApplyPromoCodeFailure(message: String) {
        hideApplyPromoCodeProgress()
        errorMessage?.apply {
            text = message
            visibility = View.VISIBLE
        }
        activity?.apply {
            etPromoCode?.let {
                it.requestFocus()
                it.showKeyboard(this as AppCompatActivity)
            }
        }
    }

    override fun onPromoCodeTextChanged(promoCode: String) {
        if (errorMessage?.visibility == View.VISIBLE)
            errorMessage?.visibility = View.GONE
        applyPromoCode?.visibility = if (promoCode.isNotEmpty()) View.VISIBLE else View.GONE
        cancelLayout?.visibility = if (promoCode.isNotEmpty()) View.GONE else View.VISIBLE
        clear?.visibility = if (promoCode.isNotEmpty()) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> activity?.finish()
            R.id.applyPromoCode -> applyPromoCode()
            R.id.clear -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_clear, this) }
                etPromoCode?.text?.clear()
            }
        }
    }

}