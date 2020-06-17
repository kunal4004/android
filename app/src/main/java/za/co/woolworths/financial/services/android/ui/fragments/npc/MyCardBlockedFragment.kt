package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_blocked_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.Utils
import androidx.appcompat.app.AppCompatActivity
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class MyCardBlockedFragment : MyCardExtension() {

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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.my_card_blocked_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity?.let { Utils.updateStatusBarBackground(it, R.color.grey_background_color) }

        AnimationUtilExtension.animateViewPushDown(btnGetReplacementCard)
        AnimationUtilExtension.animateViewPushDown(btnLinkACard)

        btnGetReplacementCard?.setOnClickListener { navigateToReplacementCard() }
        btnLinkACard?.setOnClickListener { (activity as? AppCompatActivity)?.apply { navigateToLinkNewCardActivity(this, mStoreCardDetail) } }
        btnLinkACard?.paintFlags = btnLinkACard.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        // Hide Replacement card if MC config is true
        if (WoolworthsApplication.getInstantCardReplacement()?.isEnabled == true) {
            tvNoActiveCardDesc?.text = getString(R.string.card_block_desc)
            btnGetReplacementCard?.visibility = VISIBLE
            btnLinkACard?.visibility = VISIBLE
            callUsNowButton?.visibility = GONE
        } else {
            tvNoActiveCardDesc?.text = getString(R.string.card_block_replacement_card_disabled_desc)
            btnGetReplacementCard?.visibility = GONE
            btnLinkACard?.visibility = GONE
            callUsNowButton?.visibility = VISIBLE
        }

        callUsNowButton?.setOnClickListener {  Utils.makeCall( "0861502020") }
        uniqueIdsForBlockCard()
    }

    private fun uniqueIdsForBlockCard() {
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
        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_ICR_GET_CARD)
        replaceFragment(
                fragment = GetReplacementCardFragment.newInstance(),
                tag = GetReplacementCardFragment::class.java.simpleName,
                containerViewId = R.id.flMyCard,
                allowStateLoss = true,
                enterAnimation = R.anim.slide_in_from_right,
                exitAnimation = R.anim.slide_to_left,
                popEnterAnimation = R.anim.slide_from_left,
                popExitAnimation = R.anim.slide_to_right
        )
    }
}