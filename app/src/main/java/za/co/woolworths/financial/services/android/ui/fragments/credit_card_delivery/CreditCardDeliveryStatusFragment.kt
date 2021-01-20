package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.credit_card_delivery_status_layout.*
import kotlinx.android.synthetic.main.credit_card_delivery_status_layout.imgCreditCard
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.account.CreditCardDeliveryStatus
import za.co.woolworths.financial.services.android.ui.activities.credit_card_delivery.CreditCardDeliveryActivity
import za.co.woolworths.financial.services.android.ui.extension.asEnumOrDefault
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.Utils
import za.co.woolworths.financial.services.android.util.WFormatter

class CreditCardDeliveryStatusFragment : CreditCardDeliveryBaseFragment(), View.OnClickListener {

    enum class DateType(val value: Int) { TODAY(0), TOMORROW(1) }

    var navController: NavController? = null
    var accountBinNumber: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.credit_card_delivery_status_layout, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
        bundle?.apply {
            accountBinNumber = getString("accountBinNumber")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Utils.updateStatusBarBackground(activity, R.color.grey_bg)
        navController = Navigation.findNavController(view)
        callTheCallCenter?.setOnClickListener { Utils.makeCall(WoolworthsApplication.getCreditCardDelivery().callCenterNumber) }
        if (activity is CreditCardDeliveryActivity) {
            (activity as? CreditCardDeliveryActivity)?.apply {
                changeToolbarBackground(R.color.grey_bg)
                setToolbarTitle(bindString(R.string.my_card_title))
            }
        }
        manageDeliveryLayout.setOnClickListener(this)
        trackDeliveryLayout.setOnClickListener(this)
        init()
        configureUI()
    }

    private fun init() {
        if (accountBinNumber.equals(Utils.GOLD_CARD, true)) {
            imgCreditCard.setImageDrawable(bindDrawable(R.drawable.w_gold_credit_card))
        } else if (accountBinNumber.equals(Utils.SILVER_CARD, true)) {
            imgCreditCard.setImageDrawable(bindDrawable(R.drawable.w_silver_credit_card))
        } else if (accountBinNumber.equals(Utils.BLACK_CARD, true)) {
            imgCreditCard.setImageDrawable(bindDrawable(R.drawable.w_black_credit_card))
        }
    }

    fun configureUI() {
        when (statusResponse?.deliveryStatus?.statusDescription?.asEnumOrDefault(CreditCardDeliveryStatus.DEFAULT)) {
            CreditCardDeliveryStatus.CARD_RECEIVED -> {
                cardReceivedOrAppointmentScheduled()
            }
            CreditCardDeliveryStatus.CARD_DELIVERED -> {
                progressIcon.setBackgroundResource(R.drawable.ic_delivered)
                deliveryDate.text = bindString(R.string.card_delivery_delivered)
                deliveryStatusTitle.text = bindString(R.string.delivered_cc_delivery_desc)
                val deliveryDayTimeDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_time24)
                deliveryDayAndTime.setCompoundDrawablesWithIntrinsicBounds(deliveryDayTimeDrawable, null, null, null)
                statusResponse?.slotDetails?.appointmentDate?.let {
                    if (it == "" || it == null) {
                        deliveryDayAndTime.text = ""
                    } else {
                        val deliveryDayTimeDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_time24)
                        deliveryDayAndTime.setCompoundDrawablesWithIntrinsicBounds(deliveryDayTimeDrawable, null, null, null)
                        deliveryDayAndTime.text = WFormatter.convertDayShortToLong(it).plus(", ").plus(statusResponse?.slotDetails?.slot)
                    }
                }
            }
            CreditCardDeliveryStatus.CANCELLED -> {
                progressIcon.setBackgroundResource(R.drawable.icon_credit_card_delivery_failed)
                deliveryDate.text = bindString(R.string.card_delivery_cancelled)
                deliveryStatusTitle.text = bindString(R.string.cancelled_cc_delivery_desc)
                callTheCallCenter.visibility = View.VISIBLE

                statusResponse?.slotDetails?.appointmentDate?.let {
                    if (it == "" || it == null) {
                        deliveryDayAndTime.text = ""
                    } else {
                        val deliveryDayTimeDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_time24)
                        deliveryDayAndTime.setCompoundDrawablesWithIntrinsicBounds(deliveryDayTimeDrawable, null, null, null)
                        deliveryDayAndTime.text = WFormatter.convertDayShortToLong(it).plus(", ").plus(statusResponse?.slotDetails?.slot)
                    }
                }
            }
            CreditCardDeliveryStatus.CARD_SHREDDED -> {
                progressIcon.setBackgroundResource(R.drawable.icon_credit_card_delivery_failed)
                deliveryDate.text = bindString(R.string.card_delivery_failed)
                deliveryStatusTitle.text = bindString(R.string.failed_cc_delivery_desc)
                callTheCallCenter.visibility = View.VISIBLE
                deliveryDayAndTime.text = ""
            }
            CreditCardDeliveryStatus.APPOINTMENT_SCHEDULED -> {
                cardReceivedOrAppointmentScheduled()
            }
        }
        deliveryStatusDescription.text = statusResponse?.deliveryStatus?.displayCopy
    }

    private fun cardReceivedOrAppointmentScheduled() {
        manageDeliveryLayout.visibility = View.VISIBLE
        trackDeliveryLayout.visibility = View.VISIBLE
        splitAndApplyFormatedDate(statusResponse?.slotDetails?.appointmentDate)
        val manageDeliveryDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_delivery_truck)
        manageDeliveryDrawable?.alpha = 77
        manageDeliveryText.setCompoundDrawablesWithIntrinsicBounds(manageDeliveryDrawable, null, ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_caret_black), null)
        val trackDeliveryDrawable = ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_directions)
        trackDeliveryText.setCompoundDrawablesWithIntrinsicBounds(trackDeliveryDrawable, null, ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_caret_black), null)
    }

    private fun splitAndApplyFormatedDate(appointmentDate: String?) {
        val parts: List<String>? = appointmentDate?.split("-")
        deliveryDayAndTime.text = WFormatter.convertDayShortToLong(appointmentDate).plus(", ").plus(statusResponse?.slotDetails?.slot)
        deliveryStatusTitle.text = bindString(R.string.delivery_confirmation)
        val currentDate: Int = WFormatter.checkIfDateisTomorrow(appointmentDate).toInt()
        if (currentDate == DateType.TOMORROW.value) {
            progressIcon.setBackgroundResource(R.drawable.ic_delivery_tomorrow)
            deliveryDate.text = bindString(R.string.tomorrow)
        } else if (currentDate == DateType.TODAY.value) {
            progressIcon.setBackgroundResource(R.drawable.ic_delivery_tomorrow)
            deliveryDate.text = bindString(R.string.bottom_title_today)
            deliveryStatusTitle.text = bindString(R.string.arriving)
        } else {
            progressIcon.setBackgroundResource(R.drawable.ic_delivery_later)
            deliveryDate.text = parts?.get(2)?.plus(" ").plus(WFormatter.convertMonthShortToLong(appointmentDate))
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.manageDeliveryLayout -> {
                Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_BLK_CC_MANAGE_DELIVERY)
                navController?.navigate(R.id.action_to_creditCardDeliveryManageDeliveryFragment, bundleOf("bundle" to bundle))
            }
            R.id.trackDeliveryLayout -> {
                activity?.apply {
                    supportFragmentManager?.apply {
                        val creditCardTrackMyDelivery = CreditCardTrackMyDelivery.newInstance(bundleOf("bundle" to bundle), envelopeNumber)
                        creditCardTrackMyDelivery.show(this, CreditCardTrackMyDelivery::class.java.simpleName)
                    }
                }
            }
        }
    }
}