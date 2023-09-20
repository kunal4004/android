package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ApplyPromoCodeFragmentBinding
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.ui.extension.afterTextChanged
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard
import za.co.woolworths.financial.services.android.ui.extension.showKeyboard
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class ApplyPromoCodeFragment : Fragment(R.layout.apply_promo_code_fragment), VoucherAndPromoCodeContract.ApplyPromoCodeView, View.OnClickListener {

    private lateinit var binding: ApplyPromoCodeFragmentBinding
    private var presenter: ApplyPromoCodePresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = ApplyPromoCodePresenterImpl(this, ApplyPromoCodeInteractorImpl())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ApplyPromoCodeFragmentBinding.bind(view)

        binding.apply {
            activity?.apply {
                etPromoCode?.let { editText ->
                    editText.showKeyboard(this as AppCompatActivity)
                    editText.afterTextChanged { onPromoCodeTextChanged(it) }
                    editText.filters = editText.filters + InputFilter.AllCaps()
                }
            }
            cancel?.setOnClickListener(this@ApplyPromoCodeFragment)
            clear?.setOnClickListener(this@ApplyPromoCodeFragment)
            applyPromoCode?.setOnClickListener(this@ApplyPromoCodeFragment)
        }
    }

    override fun onApplyPromoCodeSuccess(shoppingCartResponse: ShoppingCartResponse) {
        activity?.apply {
            setResult(intent.getIntExtra(
                CartFragment.INTENT_REQUEST_CODE,
                AppCompatActivity.RESULT_OK
            ), Intent().putExtra(CartFragment.SHOPPING_CART_RESPONSE, Utils.toJson(shoppingCartResponse)))
            finish()
        }
    }

    override fun applyPromoCode() {
        activity?.apply {
            binding.etPromoCode?.text.toString().trim().let {
                if (it.isNotEmpty()) {
                    Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_apply, this)
                    binding.etPromoCode.hideKeyboard(activity as AppCompatActivity)
                    Handler().postDelayed({
                        showApplyPromoCodeProgress()
                        presenter?.initApplyPromoCode(it)
                    }, AppConstant.DELAY_300_MS)

                }
            }
        }
    }

    override fun showApplyPromoCodeProgress() {
        binding.apply {
            parentLayout?.visibility = View.GONE
            progressLayout?.visibility = View.VISIBLE
        }
    }

    override fun hideApplyPromoCodeProgress() {
        binding.apply {
            parentLayout?.visibility = View.VISIBLE
            progressLayout?.visibility = View.GONE
        }
    }

    override fun onApplyPromoCodeFailure(message: String) {
        binding.apply {
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
    }

    override fun onPromoCodeTextChanged(promoCode: String) {
        binding.apply {
            if (errorMessage?.visibility == View.VISIBLE)
                errorMessage?.visibility = View.GONE
            applyPromoCode?.visibility = if (promoCode.isNotEmpty()) View.VISIBLE else View.GONE
            cancelLayout?.visibility = if (promoCode.isNotEmpty()) View.GONE else View.VISIBLE
            clear?.visibility = if (promoCode.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cancel -> activity?.finish()
            R.id.applyPromoCode -> applyPromoCode()
            R.id.clear -> {
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_promo_clear, this) }
                binding.etPromoCode?.text?.clear()
            }
        }
    }

}