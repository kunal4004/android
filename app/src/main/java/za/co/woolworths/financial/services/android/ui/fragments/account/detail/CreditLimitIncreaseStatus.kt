package za.co.woolworths.financial.services.android.ui.fragments.account.detail

import android.content.Intent
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.OfferActive
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.util.KotlinUtils.Companion.roundCornerDrawable
import za.co.woolworths.financial.services.android.util.Utils
import java.util.*

enum class CreditLimitIncreaseStates(private val status: String) {
    CONSENTS("Consents"),
    OFFER("Offer"),
    POI_REQUIRED("POI Required"),
    DECLINE("Decline"),
    CONTACT_US("Contact Us"),
    IN_PROGRESS("In Progress"),
    INCOME_AND_EXPENSE("I \u0026 E"),
    UNAVAILABLE("unavailable"),
    CLI_CONCLUDED("cli-concluded"),
    COMPLETE("complete");

    val type: String
        get() = status.toLowerCase(Locale.getDefault())
}

class CreditLimitIncreaseStatus {

    companion object {
        private var activeOffer = false
        private var nextStep = ""
        private var messageSummary = ""
        private var messageDetail = ""
    }

    fun showCLIProgress(logoIncreaseLimit: ImageView?, llCommonLayer: LinearLayout?, tvIncreaseLimit: TextView?) {
        logoIncreaseLimit?.visibility = VISIBLE
        llCommonLayer?.visibility = GONE
        tvIncreaseLimit?.visibility = GONE
    }

    fun cliStatus(llCommonLayer: LinearLayout?, tvIncreaseLimit: TextView?, tvApplyNowIncreaseLimit: TextView?, tvIncreaseLimitDescription: TextView?, logoIncreaseLimit: ImageView?, offerActive: OfferActive?) {
        llCommonLayer?.visibility = GONE
        tvIncreaseLimitDescription?.visibility = GONE
        tvIncreaseLimit?.text = getString(R.string.increase_limit)

        var nextStepColour = ""

        offerActive?.let { offer ->
            nextStep = offer.nextStep.toLowerCase(Locale.getDefault())
            messageSummary = offer.messageSummary
            messageDetail = offer.messageDetail
            activeOffer = offer.offerActive
            nextStepColour = offer.nextStepColour
        }

        when (nextStep) {
            CreditLimitIncreaseStates.CONSENTS.type -> {
                logoIncreaseLimit?.visibility = GONE
                llCommonLayer?.visibility = VISIBLE
                tvIncreaseLimit?.text = getString(R.string.cli_credit_limit_increase)
                showDescription(tvIncreaseLimitDescription, messageDetail)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
            }

            CreditLimitIncreaseStates.OFFER.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                llCommonLayer?.visibility = GONE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                showDescription(tvIncreaseLimitDescription, messageDetail)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
            }

            CreditLimitIncreaseStates.POI_REQUIRED.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                llCommonLayer?.visibility = GONE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                showDescription(tvIncreaseLimitDescription, messageDetail)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
            }

            CreditLimitIncreaseStates.DECLINE.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                llCommonLayer?.visibility = GONE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                showDescription(tvIncreaseLimitDescription, messageDetail)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
            }

            CreditLimitIncreaseStates.CONTACT_US.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                llCommonLayer?.visibility = GONE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
                showDescription(tvIncreaseLimitDescription, messageDetail)
            }

            CreditLimitIncreaseStates.IN_PROGRESS.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                tvIncreaseLimitDescription?.visibility = GONE
                llCommonLayer?.visibility = GONE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
                showDescription(tvIncreaseLimitDescription, messageDetail)
            }

            CreditLimitIncreaseStates.INCOME_AND_EXPENSE.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                cliIcon(logoIncreaseLimit)
                llCommonLayer?.visibility = GONE
                showDescription(tvIncreaseLimitDescription, messageDetail)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
            }

            CreditLimitIncreaseStates.COMPLETE.type -> {
                logoIncreaseLimit?.visibility = VISIBLE
                llCommonLayer?.visibility = GONE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                setTagBackgroundAndTitle(messageSummary, nextStepColour, tvApplyNowIncreaseLimit)
                showDescription(tvIncreaseLimitDescription, messageDetail)
            }

            else -> {
                logoIncreaseLimit?.visibility = VISIBLE
                logoIncreaseLimit?.setImageResource(R.drawable.cli)
                llCommonLayer?.visibility = GONE
                tvIncreaseLimitDescription?.visibility = GONE
                messageSummary = getString(R.string.status_unavailable) ?: ""
                setTagBackgroundAndTitle(messageSummary, nextStepColour,tvApplyNowIncreaseLimit)
            }

        }
    }

    private fun setTagBackgroundAndTitle(messageSummary: String, nextStepColour: String?, tvApplyNowIncreaseLimit: TextView?) {
        tvApplyNowIncreaseLimit?.apply {
            visibility = VISIBLE
            roundCornerDrawable(tvApplyNowIncreaseLimit, nextStepColour ?: "#b2b2b2")
            text = messageSummary
        }
    }

    private fun getString(resourceId: Int) = WoolworthsApplication.getAppContext()?.getString(resourceId)

    private fun showDescription(textView: TextView?, messageDetail: String?) {
        textView?.visibility = VISIBLE
        textView?.text = messageDetail
    }

    private fun cliIcon(logoIncreaseLimit: ImageView?) {
        logoIncreaseLimit?.setImageResource(R.drawable.cli)
    }

    fun showCLIProgress(llCommonLayer: LinearLayout?, tvIncreaseLimitDescription: TextView?) {
        llCommonLayer?.visibility = GONE
        tvIncreaseLimitDescription?.visibility = GONE
    }

    private fun moveToCLIPhase(offerActive: OfferActive, productOfferingId: String) {
        val woolworthApplication = WoolworthsApplication.getInstance()
        woolworthApplication?.currentActivity?.apply {
            woolworthApplication.setProductOfferingId(Integer.valueOf(productOfferingId))
            val openCLIIncrease = Intent(this, CLIPhase2Activity::class.java)
            openCLIIncrease.putExtra("OFFER_ACTIVE_PAYLOAD", Utils.objectToJson(offerActive))
            openCLIIncrease.putExtra("OFFER_IS_ACTIVE", activeOffer)
            startActivityForResult(openCLIIncrease, 0)
            overridePendingTransition(R.anim.slide_up_anim, R.anim.stay)
        }
    }

    fun nextStep(offerActive: OfferActive?, productOfferingId: String?) {
        val cliStatus = offerActive?.cliStatus ?: ""
        if (nextStep.isEmpty() ||
                nextStep == CreditLimitIncreaseStates.IN_PROGRESS.type ||
                nextStep == CreditLimitIncreaseStates.DECLINE.type ||
                nextStep == CreditLimitIncreaseStates.CONTACT_US.type ||
                nextStep == CreditLimitIncreaseStates.UNAVAILABLE.type ||
                (nextStep == CreditLimitIncreaseStates.COMPLETE.type && cliStatus != CreditLimitIncreaseStates.CLI_CONCLUDED.type)) {
            return
        } else {
            productOfferingId?.let { pid -> offerActive?.let { offer -> moveToCLIPhase(offer, pid) } }
        }
    }
}