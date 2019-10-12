package za.co.woolworths.financial.services.android.ui.fragments.npc

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.my_card_fragment.*
import za.co.woolworths.financial.services.android.contracts.RequestListener
import za.co.woolworths.financial.services.android.models.JWTDecodedModel
import za.co.woolworths.financial.services.android.models.dto.npc.LinkNewCardOTP
import za.co.woolworths.financial.services.android.models.dto.npc.OTPMethodType
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCard
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.activities.temporary_store_card.HowToUseTemporaryStoreCardActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card.TemporaryStoreCardExpireInfoDialog
import za.co.woolworths.financial.services.android.util.StoreCardAPIRequest
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.Utils

class MyCardDetailFragment : MyCardExtension() {

    private var mLatestOpenedDateStoreCard: StoreCard? = null
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null

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
                Utils.updateStatusBarBackground(it, R.color.grey_bg)

                mStoreCardDetail?.let { cardValue ->
                    mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
                    mLatestOpenedDateStoreCard = mStoreCardsResponse?.storeCardsData?.primaryCards?.get(0)
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
            maskedCardNumberWithSpaces(number?.toString()).also {
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
        //blockCard.setOnClickListener { activity?.let { navigateToBlockMyCardActivity(it, mStoreCardDetail, mLatestOpenedDateStoreCard) } }

        payWithCard.setOnClickListener {
            activity?.supportFragmentManager?.apply {
                //ScanBarcodeToPayDialogFragment.newInstance().show((this), ScanBarcodeToPayDialogFragment::class.java.simpleName)
                initPayWithCard()
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


    private fun initPayWithCard() {
        when (mStoreCardsResponse?.oneTimePinRequired?.unblockStoreCard) {
            true -> {
                requestGetOTP()
            }
            else -> {
                requestUnblockCard()
            }
        }
    }


    //OTP value is optional
    private fun requestUnblockCard(otp: String = "") {

    }

    private fun requestGetOTP() {
        StoreCardAPIRequest().getOTP(OTPMethodType.SMS, object : RequestListener<LinkNewCardOTP> {
            override fun onSuccess(response: LinkNewCardOTP?) {
                when (response?.httpCode) {
                    200 -> {
                        loadOTPFragment()
                    }
                    440 -> {
                    }
                    else -> {
                    }
                }
            }

            override fun onFailure(error: Throwable?) {

            }
        })
    }

    private fun loadOTPFragment() {
        
    }

    public fun onOTPEntered(otp: String) {
        requestUnblockCard(otp)
    }
}