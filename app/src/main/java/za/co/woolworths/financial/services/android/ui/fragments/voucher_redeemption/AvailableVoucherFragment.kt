package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.available_vouchers_fragment.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails
import za.co.woolworths.financial.services.android.ui.adapters.AvailableVouchersToRedeemListAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils

class AvailableVoucherFragment : Fragment(), View.OnClickListener, VoucherAndPromoCodeContract.AvailableVoucherView {

    private var voucherDetails: VoucherDetails? = null
    var presenter: VoucherAndPromoCodeContract.AvailableVoucherPresenter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.available_vouchers_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.findViewById<TextView>(R.id.toolbarText)?.text = bindString(R.string.available_vouchers)
        redeemVoucher?.setOnClickListener(this)
        showAvailableVouchers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = AvailableVoucherPresenterImpl(this, AvailableVoucherInteractorImpl())
        voucherDetails = Utils.strToJson(activity?.intent?.getStringExtra("VoucherDetails"), VoucherDetails::class.java) as VoucherDetails?
        voucherDetails?.vouchers?.let { presenter?.setVouchers(it) }
    }


    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.redeemVoucher -> {
                redeemVouchers()
            }
        }
    }

    override fun showAvailableVouchers() {
        activity?.let {
            rcvVoucherList?.apply {
                layoutManager = LinearLayoutManager(it)
                adapter = presenter?.getVouchers()?.let { it1 -> AvailableVouchersToRedeemListAdapter(it1, this@AvailableVoucherFragment) }
            }
        }
    }

    override fun onVoucherRedeemSuccess(shoppingCartResponse: ShoppingCartResponse) {
        activity?.apply {
            setResult(Activity.RESULT_OK, Intent().putExtra("ShoppingCartResponse", Utils.toJson(shoppingCartResponse)))
            finish()
        }
    }

    override fun onVoucherRedeemFailure() {
        activity?.apply {
            hideRedeemVoucherProgress()
            rcvVoucherList?.adapter?.notifyDataSetChanged()
        }
    }

    override fun redeemVouchers() {
        voucherDetails?.vouchers?.let {
            presenter?.getSelectedVouchersToApply()?.let { selectedVouchers ->
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_ovr_voucher_redeem)
                showRedeemVoucherProgress()
                presenter?.initRedeemVouchers(selectedVouchers)
            }
        }
    }

    override fun showRedeemVoucherProgress() {
        activity?.findViewById<AppBarLayout>(R.id.appbar)?.visibility = View.GONE
        dataLayout?.visibility = View.GONE
        progressLayout?.visibility = View.VISIBLE
    }

    override fun hideRedeemVoucherProgress() {
        progressLayout?.visibility = View.GONE
        activity?.findViewById<AppBarLayout>(R.id.appbar)?.visibility = View.VISIBLE
        dataLayout?.visibility = View.VISIBLE
    }

    override fun onVoucherSelected() {
        voucherDetails?.vouchers?.let {
            redeemVoucher?.isEnabled = presenter?.isVouchersSelectedToRedeem() ?: true
        }
    }

    override fun onVoucherRedeemGeneralFailure(message: String) {
        activity?.apply {
            hideRedeemVoucherProgress()
            errorMessage?.let {
                it.text = message
                it.visibility = View.VISIBLE
                Handler().postDelayed({
                    it.visibility = View.GONE
                }, 3000)
            }
        }
    }

}