package za.co.woolworths.financial.services.android.ui.activities

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.WrewardsVoucherDetailsBinding
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.Voucher
import za.co.woolworths.financial.services.android.models.dto.VoucherCollection
import za.co.woolworths.financial.services.android.ui.adapters.WRewardsVouchersAdapter
import za.co.woolworths.financial.services.android.ui.views.card_swipe.CardStackView.CardEventListener
import za.co.woolworths.financial.services.android.ui.views.card_swipe.SwipeDirection
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class WRewardsVoucherDetailsActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: WrewardsVoucherDetailsBinding
    private lateinit var wRewardsVoucherAdapter: WRewardsVouchersAdapter
    private var voucherCollection: VoucherCollection? = null
    private var position = 0
    private var vouchers: MutableList<Voucher?>? = null

    companion object {
        const val TAG = "VoucherDetailsActivity"
        const val POSITION = "POSITION"
        const val VOUCHERS = "VOUCHERS"
        const val TERMS = "TERMS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KotlinUtils.setTransparentStatusBar(this)
        binding = WrewardsVoucherDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            closeVoucherImageButton.setOnClickListener(this@WRewardsVoucherDetailsActivity)
            intent?.extras?.apply {
                voucherCollection = Gson().fromJson(getString(VOUCHERS), VoucherCollection::class.java)
                position = getInt(POSITION)
                vouchers = voucherCollection?.vouchers
                Collections.rotate(vouchers, -position)
            }
            termsCondition?.setOnClickListener(this@WRewardsVoucherDetailsActivity)
            setVoucherAdapter()
            cardSwipeStackView?.setCardEventListener(object : CardEventListener {
                override fun onCardDragging(percentX: Float, percentY: Float) {
                    if ((percentX > 0 && percentY < 0) || (percentX < 0 && percentY < 0)) { // Detects left and right dragging
                        closeVoucherImageButton?.alpha =
                                if (KotlinUtils.isNumberPositive(percentX)) percentX.plus(1) else 1.minus(percentX)
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

            tagVoucherDescription()
        }
    }

    private fun WrewardsVoucherDetailsBinding.moveVoucherItemToLastPosition() {
        tagVoucherDescription()
        vouchers?.apply {
            val item = vouchers?.get(0)
            removeAt(0) // remove visible card
            add(item) // add visible card to last position in array
        }
    }

    private fun WrewardsVoucherDetailsBinding.setVoucherAdapter() {
        wRewardsVoucherAdapter =
                WRewardsVouchersAdapter(this@WRewardsVoucherDetailsActivity, vouchers)
        cardSwipeStackView?.setAdapter(wRewardsVoucherAdapter)
    }


    public override fun onResume() {
        super.onResume()
        Utils.setScreenName(this, FirebaseManagerAnalyticsProperties.ScreenNames.WREWARDS_VOUCHERS_BARCODE)
    }

    private fun WrewardsVoucherDetailsBinding.tagVoucherDescription() {
        val position = if (vouchers?.size == 1) 0 else cardSwipeStackView?.topIndex ?: 0
        vouchers?.get(position)?.description?.apply {
            val arguments: MutableMap<String, String> = HashMap()
            arguments[FirebaseManagerAnalyticsProperties.PropertyNames.VOUCHERDESCRIPTION] = Utils.ellipsizeVoucherDescription(this)
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.WREWARDSDESCRIPTION_VOUCHERDESCRIPTION, arguments, this@WRewardsVoucherDetailsActivity)
        }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.closeVoucherImageButton -> onBackPressed()
            R.id.termsCondition -> binding.viewTermsAndConditions()
        }
    }

    private fun WrewardsVoucherDetailsBinding.viewTermsAndConditions() {
        val terms = vouchers?.get(cardSwipeStackView?.topIndex ?: 0)?.termsAndConditions
        if (TextUtils.isEmpty(terms)) {
            Utils.openLinkInInternalWebView(AppConfigSingleton.wrewardsTCLink, true)
        } else {
            startActivity(Intent(this@WRewardsVoucherDetailsActivity, WRewardsVoucherTermAndConditions::class.java).putExtra(TERMS, terms))
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }
}