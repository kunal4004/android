package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.scan_barcode_to_pay_dialog.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils


class ScanBarcodeToPayDialogFragment : WBottomSheetDialogFragment() {
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null
    private var listener: IOnTemporaryStoreCardDialogDismiss? = null
    private var isCardDetailsVisible: Boolean = false
    private val timerCardDetailsVisibility = Handler(Looper.getMainLooper())

    interface IOnTemporaryStoreCardDialogDismiss {
        fun onTempStoreCardDialogDismiss()
    }

    companion object {
        private const val DURATION_FADE: Long = 300

        fun newInstance(mStoreCardsResponse: String) = ScanBarcodeToPayDialogFragment().withArgs {
            putString(STORE_CARD_DETAIL, mStoreCardsResponse)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mStoreCardDetail = it.getString(STORE_CARD_DETAIL, "")
            mStoreCardDetail?.let { cardValue ->
                mStoreCardsResponse = Gson().fromJson(cardValue, StoreCardsResponse::class.java)
            }
        }

        try {
            listener = parentFragment as IOnTemporaryStoreCardDialogDismiss?
        } catch (e: ClassCastException) {
            throw ClassCastException("Calling fragment must implement Callback interface")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.scan_barcode_to_pay_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
    }

    private fun configureUI() {
        btnDismissDialog.setOnClickListener { dismiss() }
        btnToggleCardDetails.setOnClickListener { toggleCardDetailsVisibility() }

        mStoreCardsResponse?.storeCardsData?.virtualCard?.let {
            tvCardNumber.text = it.number.chunked(4).joinToString("  ")
            tvCardholderValue.text = it.embossedName
            tvSequenceNumberValue.text = it.sequence
            ivBarcode.post {
                ivBarcode.setImageBitmap(Utils.encodeAsBitmap(it.number.plus(it.sequence), BarcodeFormat.CODE_128, ivBarcode.width, 60))
            }
        }

        rlCardBarcode.alpha = 1f
        rlCardDetails.alpha = 0f
        isCardDetailsVisible = false
    }

    private fun toggleCardDetailsVisibility() {
        isCardDetailsVisible = !isCardDetailsVisible

        if (isCardDetailsVisible) {
            btnToggleCardDetailsLabel.text = getString(R.string.dialog_scan_barcode_to_pay_toggle_details_hidden)
            ivToggleCardDetailsLabelIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_barcode_hide, null))
        } else {
            btnToggleCardDetailsLabel.text = getString(R.string.dialog_scan_barcode_to_pay_toggle_details_visible)
            ivToggleCardDetailsLabelIcon.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_barcode_show, null))
        }

        rlCardBarcode
                .animate()
                .setDuration(DURATION_FADE)
                .alpha(if (isCardDetailsVisible) 0f else 1f)
                .start()
        rlCardDetails
                .animate()
                .setDuration(DURATION_FADE)
                .alpha(if (isCardDetailsVisible) 1f else 0f)
                .start()

        timerCardDetailsVisibility.removeCallbacksAndMessages(null)
        if (isCardDetailsVisible) {
            val cardDisplayTimeoutInSeconds = WoolworthsApplication.getVirtualTempCard().cardDisplayTimeoutInSeconds ?: 10L
            timerCardDetailsVisibility.postDelayed({ toggleCardDetailsVisibility() }, cardDisplayTimeoutInSeconds * 1000)

            Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_VIEWCARDNUMBERS)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener?.onTempStoreCardDialogDismiss()
    }

    override fun onDetach() {
        timerCardDetailsVisibility.removeCallbacksAndMessages(null)
        super.onDetach()
    }
}