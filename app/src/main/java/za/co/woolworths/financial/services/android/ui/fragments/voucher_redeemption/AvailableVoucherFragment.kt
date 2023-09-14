package za.co.woolworths.financial.services.android.ui.fragments.voucher_redeemption

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.AvailableVouchersFragmentBinding
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.cart.view.CartFragment
import za.co.woolworths.financial.services.android.cart.view.CartFragment.Companion.BLACK_CARD_HOLDER
import za.co.woolworths.financial.services.android.cart.view.CartFragment.Companion.CASH_BACK_VOUCHERS
import za.co.woolworths.financial.services.android.cart.view.CartFragment.Companion.VOUCHER_DETAILS
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.dto.ShoppingCartResponse
import za.co.woolworths.financial.services.android.models.dto.account.ApplyNowState
import za.co.woolworths.financial.services.android.models.dto.voucher_and_promo_code.VoucherDetails
import za.co.woolworths.financial.services.android.ui.adapters.AvailableVouchersToRedeemListAdapter
import za.co.woolworths.financial.services.android.ui.adapters.CashBackVouchersAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities.ApplyNowActivity
import za.co.woolworths.financial.services.android.ui.views.actionsheet.vouchersBottomDialog.VouchersBottomDialog
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.DELAY_3000_MS
import za.co.woolworths.financial.services.android.util.Utils
import javax.inject.Inject

@AndroidEntryPoint
class AvailableVoucherFragment : Fragment(R.layout.available_vouchers_fragment), View.OnClickListener, VoucherAndPromoCodeContract.AvailableVoucherView {

    private lateinit var binding: AvailableVouchersFragmentBinding
    private var voucherDetails: VoucherDetails? = null
    private var presenter: VoucherAndPromoCodeContract.AvailableVoucherPresenter? = null
    private var vouchersListAdapter: AvailableVouchersToRedeemListAdapter? = null
    var shoppingCartResponse: ShoppingCartResponse? = null
    private lateinit var cashBackVouchersAdapter: CashBackVouchersAdapter
    private var isFromCashBackVoucher: Boolean = false
    private var wrewardsListVisiblePosition = 0
    private var cashBackListVisiblePosition = 0
    private var isBlackCardHolder : Boolean = false

    @Inject
    lateinit var vouchersBottomDialog: VouchersBottomDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = AvailableVouchersFragmentBinding.bind(view)

        activity?.findViewById<TextView>(R.id.toolbarText)?.text = bindString(R.string.available_vouchers)
        binding.redeemVoucher?.setOnClickListener(this)
        binding.cashBackVouchersInfo.setOnClickListener(this)
        binding.noVoucherLayout.applyForCreditCard?.setOnClickListener(this)
        cashBackVouchersAdapter = CashBackVouchersAdapter(ArrayList())
        voucherTabSelection()
        showWrewardsVouchers()
        if (isFromCashBackVoucher) {
            val tab: TabLayout.Tab? = binding.voucherTabLayout.getTabAt(1)
            tab?.select()
            showCashBackVouchers()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        presenter = AvailableVoucherPresenterImpl(this, AvailableVoucherInteractorImpl())
        voucherDetails = Utils.strToJson(activity?.intent?.getStringExtra(VOUCHER_DETAILS), VoucherDetails::class.java) as VoucherDetails?
        isFromCashBackVoucher = checkNotNull(activity?.intent?.getBooleanExtra(CASH_BACK_VOUCHERS,false))
        isBlackCardHolder = checkNotNull(activity?.intent?.getBooleanExtra(BLACK_CARD_HOLDER,false))
        voucherDetails?.vouchers?.let { presenter?.setVouchers(it) }
        voucherDetails?.cashBack?.let { presenter?.setCashBackVouchers(it) }
    }

    private fun voucherTabSelection() {
        binding.voucherTabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        showWrewardsVouchers()
                        presenter?.getCashBackVouchers()?.let {
                            if (it.size > 0) {
                                cashBackListVisiblePosition =
                                    (binding.rcCashBackVoucherList.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()!!
                            }
                        }
                    }
                    1 -> {
                        showCashBackVouchers()
                        presenter?.getVouchers()?.let {
                            if (it.size > 0) {
                                wrewardsListVisiblePosition =
                                    (binding.rcvVoucherList.layoutManager as? LinearLayoutManager)?.findFirstCompletelyVisibleItemPosition()!!
                            }
                        }

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                //Do Nothing
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                //Do Nothing
            }
        })
    }

    private fun showCashBackVouchers() {
        presenter?.getCashBackVouchers()?.let {
            binding.apply {
                if (it.size > 0) {
                    rcvVoucherList.visibility = View.GONE
                    dataLayout.visibility = View.VISIBLE
                    noVoucherLayout.noVouchersAvailable.visibility = View.GONE
                    rcCashBackVoucherList.apply {
                        visibility = View.VISIBLE
                        layoutManager = LinearLayoutManager(requireActivity())
                        adapter = cashBackVouchersAdapter
                    }
                    cashBackVouchersAdapter.onItemClick = {
                        vouchersBottomDialog.showCashBackVouchersInfo(
                            requireActivity(),
                            getString(R.string.can_not_redeem_cash_back_vouchers),
                            getString(R.string.cash_back_cancellation),
                            false
                        )
                    }
                    redeemVoucher.visibility = View.GONE
                    cashBackVouchersInfo.visibility = View.VISIBLE
                    vouchersTitle.text = bindString(R.string.your_cash_back_vouchers)
                    vouchersSubTitle.text = bindString(R.string.cash_back_vouchers_desc)
                    cashBackVouchersAdapter.renderCashBackVouchers(it)
                    cashBackVouchersAdapter.notifyDataSetChanged()

                    (rcCashBackVoucherList.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                        cashBackListVisiblePosition)
                    cashBackListVisiblePosition = 0

                } else {
                    dataLayout.visibility = View.GONE
                    redeemVoucher.visibility = View.GONE
                    noVoucherLayout.noVouchersAvailable.visibility = View.VISIBLE
                    if (!isBlackCardHolder) {
                        noVoucherLayout.subTitle.visibility = View.VISIBLE
                        noVoucherLayout.applyForCreditCard.visibility = View.VISIBLE
                        noVoucherLayout.title.text =
                            getString(R.string.you_don_t_qualify_for_cash_back)
                    } else {
                        noVoucherLayout.title.text = getString(R.string.no_cash_back_right_now)
                    }

                }
            }
        }
    }

    private fun showWrewardsVouchers() {
        presenter?.getVouchers()?.let {
            binding.apply {
                if (it.size > 0) {
                    rcCashBackVoucherList.visibility = View.GONE
                    rcvVoucherList.visibility = View.VISIBLE
                    dataLayout.visibility = View.VISIBLE
                    noVoucherLayout.noVouchersAvailable.visibility = View.GONE
                    redeemVoucher.visibility = View.VISIBLE
                    cashBackVouchersInfo.visibility = View.GONE
                    vouchersTitle.text = bindString(R.string.select_wvouchers_redeem)
                    vouchersSubTitle.text = bindString(R.string.select_wvouchers_desc)
                    showAvailableVouchers()
                    (rcvVoucherList.layoutManager as? LinearLayoutManager)?.scrollToPosition(
                        wrewardsListVisiblePosition)
                    wrewardsListVisiblePosition = 0

                } else {
                    noVoucherLayout.subTitle.visibility = View.GONE
                    noVoucherLayout.applyForCreditCard.visibility = View.GONE
                    dataLayout.visibility = View.GONE
                    noVoucherLayout.noVouchersAvailable.visibility = View.VISIBLE
                    noVoucherLayout.title.text = getString(R.string.no_wrewords_right_now)
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.redeemVoucher -> {
                redeemVouchers()
            }
            R.id.applyForCreditCard -> {
                redirectToMyAccountsCardsActivity(ApplyNowState.BLACK_CREDIT_CARD)
            }
            R.id.cashBackVouchersInfo -> {
                showCashBackInfo()
            }

        }
    }

    private fun showCashBackInfo() {
        vouchersBottomDialog.showCashBackVouchersInfo(
            requireActivity(),
            getString(R.string.using_cash_back_vouchers),
            getString(R.string.cash_back_cancellation),
            true
        )
    }

    override fun showAvailableVouchers() {
        activity?.let {
            vouchersListAdapter = presenter?.getVouchers()?.let { it1 -> AvailableVouchersToRedeemListAdapter(it1, this@AvailableVoucherFragment) }
            binding.rcvVoucherList?.apply {
                layoutManager = LinearLayoutManager(it)
                adapter = vouchersListAdapter
            }
        }
    }

    override fun onVoucherRedeemSuccess(shoppingCartResponse: ShoppingCartResponse, isPartialSuccess: Boolean) {
        this.shoppingCartResponse = shoppingCartResponse
        activity?.apply {
            when (isPartialSuccess) {
                true -> {
                    updateVouchersList()
                    showGenericErrorMessage()
                }
                false -> {
                    setResult(intent.getIntExtra(
                        CartFragment.INTENT_REQUEST_CODE,
                        AppCompatActivity.RESULT_OK
                    ), Intent().putExtra(CartFragment.SHOPPING_CART_RESPONSE, Utils.toJson(shoppingCartResponse)))
                    finish()
                }
            }
        }

    }

    override fun onVoucherRedeemFailure() {
        activity?.apply {
            updateVouchersList()
        }
    }

    override fun redeemVouchers() {
        voucherDetails?.vouchers?.let {
            presenter?.getSelectedVouchersToApply()?.let { selectedVouchers ->
                activity?.apply { Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.Cart_ovr_voucher_redeem, this) }
                showRedeemVoucherProgress()
                presenter?.initRedeemVouchers(selectedVouchers)
            }
        }
    }

    override fun showRedeemVoucherProgress() {
        activity?.findViewById<AppBarLayout>(R.id.appbar)?.visibility = View.GONE
        binding.apply {
            dataLayout.visibility = View.GONE
            redeemVoucher.visibility = View.GONE
            voucherTabLayout.visibility = View.GONE
            progressLayout.visibility = View.VISIBLE
        }

    }

    override fun hideRedeemVoucherProgress() {
        activity?.findViewById<AppBarLayout>(R.id.appbar)?.visibility = View.VISIBLE
        binding.apply {
            progressLayout.visibility = View.GONE
            dataLayout.visibility = View.VISIBLE
            voucherTabLayout.visibility = View.VISIBLE
            redeemVoucher.visibility = View.VISIBLE
        }
    }

    override fun enableRedeemButton() {
        voucherDetails?.vouchers?.let {
            binding.redeemVoucher?.isEnabled = presenter?.isVouchersSelectedToRedeem() ?: true
        }
    }

    override fun onVoucherRedeemGeneralFailure(message: String) {
        activity?.apply {
            hideRedeemVoucherProgress()
            binding.errorMessage?.let {
                it.text = message
                it.visibility = View.VISIBLE
                Handler().postDelayed({
                    it.visibility = View.GONE
                }, DELAY_3000_MS)
            }
        }
    }

    override fun updateVouchersList() {
        hideRedeemVoucherProgress()
        enableRedeemButton()
        presenter?.getVouchers()?.let { vouchersListAdapter?.updateVouchersList(it) }
    }

    private fun showGenericErrorMessage() {
        binding.errorMessage?.let {
            it.text = bindString(R.string.generic_error_message_for_redeem_voucher)
            it.visibility = View.VISIBLE
            Handler().postDelayed({
                it.visibility = View.GONE
            }, DELAY_3000_MS)
        }
    }

    private fun redirectToMyAccountsCardsActivity(applyNowState: ApplyNowState) {
        Intent(requireActivity(), ApplyNowActivity::class.java).apply {
            val bundle = Bundle()
            bundle.putSerializable("APPLY_NOW_STATE", applyNowState)
            putExtras(bundle)
            startActivity(this)
            requireActivity().overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

}