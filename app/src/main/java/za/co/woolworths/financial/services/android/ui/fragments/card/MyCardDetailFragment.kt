package za.co.woolworths.financial.services.android.ui.fragments.card

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
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

class MyCardDetailFragment : MyCardExtension() {

    private var mCardDetail: Card? = null

    companion object {
        const val CARD = "CARD"
        fun newInstance(card: String) = MyCardDetailFragment().withArgs {
            putString(CARD, card)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Extract latest openedDate
            it.getString(CARD)?.let { cardValue ->
                mCardDetail = Gson().fromJson(cardValue, Account::class.java)?.primaryCard?.cards
                        ?.let { cards -> Collections.max(cards) { card, nextCard -> card.openedDate().compareTo(nextCard.openedDate()) } }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity?.let { Utils.updateStatusBarBackground(it, R.color.grey_bg) }
        return inflater.inflate(R.layout.my_card_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        populateView()
        onClick()
    }

    private fun populateView() {
        mCardDetail?.apply {
            maskedCardNumberWithSpaces(cardNumber).also {
                tvCardNumberValue?.text = it
                tvCardNumberHeader?.text = it
            }

            toTitleCase(cardName()).also {
                tvCardHolderValue?.text = it
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
        blockCardView.setOnClickListener { activity?.let { navigateToBlockMyCardActivity(it, (activity as? MyCardDetailActivity)?.getMyStoreCardDetail()) } }
    }
}