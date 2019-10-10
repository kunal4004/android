package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_fragment.*
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.npc.Card
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.ScanBarcodeToPayDialogFragment
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.TemporaryStoreCardExpireInfoDialog
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class MyCardDetailFragment : MyCardExtension() {

    private var mLatestOpenedDateStoreCard: Card? = null
    private var mStoreCardDetail: String? = null

    companion object {
        const val CARD = "CARD"
        fun newInstance(storeCardDetail: String?) = MyCardDetailFragment().withArgs {
            putString(STORE_CARD_DETAIL, storeCardDetail)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mStoreCardDetail = getString(STORE_CARD_DETAIL, "")
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
        return inflater.inflate(R.layout.my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateView()
        onClick()
    }

    private fun populateView() {
        mLatestOpenedDateStoreCard?.apply {
            maskedCardNumberWithSpaces(cardNumber?.toString()).also {
                textViewCardNumber?.text = it
                tvCardNumberHeader?.text = it
            }

            toTitleCase(cardName()).also {
                textViewCardHolderName?.text = it
                tvCardHolderHeader?.text = it
            }
        }
    }

    private fun cardName(): String {
        val jwtDecoded: JWTDecodedModel? = SessionUtilities.getInstance().jwt
        val name = jwtDecoded?.name?.get(0) ?: ""
        val familyName = jwtDecoded?.family_name?.get(0) ?: ""
        return "$familyName $name"
    }

    private fun onClick() {
        blockCard.setOnClickListener { activity?.let { navigateToBlockMyCardActivity(it, mStoreCardDetail, mLatestOpenedDateStoreCard) } }
        payWithCard.setOnClickListener {
            activity?.supportFragmentManager?.apply {
                ScanBarcodeToPayDialogFragment.newInstance().show((this), ScanBarcodeToPayDialogFragment::class.java.simpleName)
            }
        }
        howItWorks.setOnClickListener {
            activity?.apply {
                startActivity(Intent(this, HowToUseTemporaryStoreCardActivity::class.java))
                overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)

            }
        }

        expireInfo.setOnClickListener {
            activity?.supportFragmentManager?.apply {
                TemporaryStoreCardExpireInfoDialog.newInstance().show((this), TemporaryStoreCardExpireInfoDialog::class.java.simpleName)
            }
        }
    }
}