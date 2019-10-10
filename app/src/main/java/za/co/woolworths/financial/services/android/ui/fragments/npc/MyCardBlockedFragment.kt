package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.my_card_blocked_fragment.*
import za.co.woolworths.financial.services.android.ui.extension.replaceFragment
import za.co.woolworths.financial.services.android.util.Utils
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.npc.Card
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import java.util.*

class MyCardBlockedFragment : MyCardExtension() {

    private var mLatestOpenedDateStoreCard: Card? = null
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
            // Extract latest openedDate
            activity?.let {
                mStoreCardDetail?.let { cardValue ->
                    mLatestOpenedDateStoreCard = Gson().fromJson(cardValue, Account::class.java)?.primaryCard?.cards
                            ?.let { cards -> Collections.max(cards) { card, nextCard -> card.openedDate().compareTo(nextCard.openedDate()) } }
                    Utils.updateStatusBarBackground(it, R.color.grey_bg)
                }
            }
        }
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.let { Utils.updateStatusBarBackground(it, R.color.grey_bg) }
        return inflater.inflate(R.layout.my_card_blocked_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnGetReplacementCard?.setOnClickListener { navigateToReplacementCard() }
        btnLinkACard?.setOnClickListener { (activity as? AppCompatActivity)?.apply { navigateToLinkNewCardActivity(this,  mStoreCardDetail) } }
        btnLinkACard?.paintFlags = btnLinkACard.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    private fun navigateToReplacementCard() {
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