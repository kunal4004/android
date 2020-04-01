package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.wrewards_voucher_details.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.Voucher
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVouchersAdapter
import za.co.woolworths.financial.services.android.ui.views.card_swipe.CardStackView.CardEventListener
import za.co.woolworths.financial.services.android.ui.views.card_swipe.SwipeDirection
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class WRewardsVoucherDetailsActivity : AppCompatActivity(), View.OnClickListener {
    private lateinit var wRewardsVoucherAdapter: WRewardsVouchersAdapter
    private var voucherCollection: VoucherCollection? = null
    private var position = 0
    private var vouchers: MutableList<Voucher?>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KotlinUtils.setTransparentStatusBar(this)
        setContentView(R.layout.wrewards_voucher_details)
        closeVoucherImageButton.setOnClickListener(this)
        intent?.extras?.apply {
            voucherCollection = Gson().fromJson(getString(VOUCHERS), VoucherCollection::class.java)
            position = getInt(POSITION)
            vouchers = voucherCollection?.vouchers
            Collections.rotate(vouchers, -position)

            setVoucherAdapter()
            swipeStack?.setCardEventListener(object : CardEventListener {
                override fun onCardDragging(percentX: Float, percentY: Float) {
                    if ((percentX > 0 && percentY < 0) || (percentX < 0 && percentY < 0)) { // Detects left and right dragging
                        closeVoucherImageButton?.alpha = if (KotlinUtils.isNumberPositive(percentX)) percentX.plus(1) else 1.minus(percentX)
                    } else if ((percentX > 0 && percentY > 0) || (percentX < 0 && percentY > 0)) { // Detects bottom dragging
                        termsCondition?.alpha = if (percentY > 0.1) 0f else 1f
                    } else {
                        termsCondition?.alpha = 1f
                        closeVoucherImageButton?.alpha = 1f
                    }
                }

                override fun onCardSwiped(direction: SwipeDirection?) {
                    if (direction === SwipeDirection.Bottom) {
                        moveVoucherItemToLastPosition()
                        setVoucherAdapter()
                    }
                }
            })
        }
        tagVoucherDescription(swipeStack?.topIndex ?: 0)
        termsCondition?.setOnClickListener(this)
        closeVoucherImageButton?.setOnClickListener(this)
    }

    private fun moveVoucherItemToLastPosition() {
        val voucher = vouchers?.get(0)
        vouchers?.removeAt(0)
        vouchers?.add(voucher)
        tagVoucherDescription(position+1)
    }

    private fun setVoucherAdapter() {
        wRewardsVoucherAdapter =
                WRewardsVouchersAdapter(this@WRewardsVoucherDetailsActivity, vouchers)
        swipeStack?.setAdapter(wRewardsVoucherAdapter)
    }

    public override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_VOUCHERS_BARCODE)
    }

    private fun tagVoucherDescription(position: Int) {
        val arguments: MutableMap<String, String> = HashMap()
        arguments[FirebaseManagerAnalyticsProperties.PropertyNames.VOUCHERDESCRIPTION] =
                Utils.ellipsizeVoucherDescription(vouchers!![position]!!.description)
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSDESCRIPTION_VOUCHERDESCRIPTION, arguments)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.closeVoucherImageButton -> onBackPressed()
            R.id.termsCondition -> viewTermsAndConditions()
        }
    }

    private fun viewTermsAndConditions() {
        val terms = vouchers?.get(swipeStack.topIndex)?.termsAndConditions
        if (TextUtils.isEmpty(terms)) {
            Utils.openLinkInInternalWebView(WoolworthsApplication.getWrewardsTCLink())
        } else {
            startActivity(Intent(this@WRewardsVoucherDetailsActivity, WRewardsVoucherTermAndConditions::class.java).putExtra(TERMS, terms))
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    companion object {
        const val TAG = "VoucherDetailsActivity"
        const val POSITION = "POSITION"
        const val VOUCHERS = "VOUCHERS"
        const val TERMS = "TERMS"
    }
}