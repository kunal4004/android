package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.MyCardBlockedFragmentBinding
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class MyCardBlockedFragment : MyCardExtension(R.layout.my_card_blocked_fragment) {

    private lateinit var binding: MyCardBlockedFragmentBinding
    private var mStoreCardDetail: String? = null

    companion object {
        fun newInstance(storeCardDetail: String?) = MyCardBlockedFragment().withArgs {
            putString(MyCardDetailActivity.STORE_CARD_DETAIL, storeCardDetail)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mStoreCardDetail = getString(MyCardDetailActivity.STORE_CARD_DETAIL, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = MyCardBlockedFragmentBinding.bind(view)

        activity?.let { Utils.updateStatusBarBackground(it, R.color.grey_background_color) }

        binding.apply {
            AnimationUtilExtension.animateViewPushDown(btnGetReplacementCard)
            AnimationUtilExtension.animateViewPushDown(btnLinkACard)

            btnGetReplacementCard?.setOnClickListener { navigateToReplacementCard() }
            btnLinkACard?.setOnClickListener {
                (activity as? AppCompatActivity)?.apply {
                    navigateToLinkNewCardActivity(
                        this,
                        mStoreCardDetail
                    )
                }
            }
            btnLinkACard?.paintFlags = btnLinkACard.paintFlags or Paint.UNDERLINE_TEXT_FLAG

            // Hide Replacement card if MC config is true
            when (AppConfigSingleton.instantCardReplacement?.isEnabled == true) {
                true -> {
                    tvNoActiveCardDesc?.text = bindString(R.string.card_block_desc)
                    btnGetReplacementCard?.visibility = VISIBLE
                    btnLinkACard?.visibility = VISIBLE
                    callUsNowButton?.visibility = GONE
                }
                else -> {
                    tvNoActiveCardDesc?.text =
                        bindString(R.string.card_block_replacement_card_disabled_desc)
                    btnGetReplacementCard?.visibility = GONE
                    btnLinkACard?.visibility = GONE
                    callUsNowButton?.visibility = VISIBLE
                    cardStatusTagTextView?.text = bindString(R.string.active)
                    imStoreCard?.setImageResource(R.drawable.w_store_card)
                }
            }

            callUsNowButton?.setOnClickListener { Utils.makeCall("0861502020") }
            uniqueIdsForBlockCard()
        }
    }

    private fun MyCardBlockedFragmentBinding.uniqueIdsForBlockCard() {
        activity?.resources?.apply {
            imStoreCard?.contentDescription = getString(R.string.image_card)
            tvNoActiveCard?.contentDescription = getString(R.string.label_noActiveCard)
            tvNoActiveCardDesc?.contentDescription = getString(R.string.label_noActiveCardDescription)
            btnGetReplacementCard?.contentDescription = getString(R.string.button_getReplacementCard)
            callUsNowButton?.contentDescription = getString(R.string.call_us)
            btnLinkACard?.contentDescription = getString(R.string.link_alreadyHaveCard)
            blockCardConstraintLayout?.contentDescription = getString(R.string.block_card_layout)
        }
    }

    private fun navigateToReplacementCard() {
        activity?.apply {
            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD, this)
            Intent(this, SelectStoreActivity::class.java).apply {
                startActivity(this)
            }
        }
    }
}