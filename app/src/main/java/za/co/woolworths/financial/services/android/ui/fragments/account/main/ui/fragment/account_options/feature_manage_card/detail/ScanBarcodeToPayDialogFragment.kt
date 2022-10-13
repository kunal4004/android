package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.detail

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResult
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.google.zxing.BarcodeFormat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.scan_barcode_to_pay_dialog.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_account_options_list.card_freeze.TemporaryFreezeCardViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.PayWithCardListFragment
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.feature_manage_card.card.PayWithCardListFragment.Companion.PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils

@AndroidEntryPoint
class ScanBarcodeToPayDialogFragment : WBottomSheetDialogFragment() {
    private var isCardDetailsVisible: Boolean = false
    private val timerCardDetailsVisibility = Handler(Looper.getMainLooper())

    val args: ScanBarcodeToPayDialogFragmentArgs by navArgs()

    val viewModel: TemporaryFreezeCardViewModel by activityViewModels()

    companion object {
        private const val DURATION_FADE: Long = 300
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.scan_barcode_to_pay_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        configureUI()
    }

    private fun configureUI() {
        btnDismissDialog.setOnClickListener { dismiss() }
        btnToggleCardDetails.setOnClickListener { toggleCardDetailsVisibility() }

        tvCardDetailsTitle.text = AppConfigSingleton.virtualTempCard?.cardDisplayTitle ?: getString(
            R.string.dialog_scan_barcode_to_pay_card_details_title
        )
        tvBarcodeTitle.text = AppConfigSingleton.virtualTempCard?.barcodeDisplayTitle
            ?: getString(R.string.dialog_scan_barcode_to_pay_barcode_title)
        tvBarcodeDescription.text = AppConfigSingleton.virtualTempCard?.barcodeDisplaySubtitle
            ?: getString(R.string.dialog_scan_barcode_to_pay_barcode_desc)

        args.storeCardResponse.storeCardsData?.virtualCard?.let {
            tvCardNumber.text = it.number.chunked(4).joinToString("  ")
            tvCardholderValue.text = it.embossedName
            tvSequenceNumberValue.text = it.sequence.toString()
            ivBarcode.post {
                ivBarcode.setImageBitmap(
                    Utils.encodeAsBitmap(
                        it.number.plus(it.sequence),
                        BarcodeFormat.CODE_128,
                        ivBarcode.width,
                        60
                    )
                )
            }
        }

        rlCardBarcode.alpha = 1f
        rlCardDetails.alpha = 0f
        isCardDetailsVisible = false
    }

    private fun toggleCardDetailsVisibility() {
        isCardDetailsVisible = !isCardDetailsVisible

        toggleShowCardDetailsButtonState()

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
            val cardDisplayTimeoutInSeconds =
                AppConfigSingleton.virtualTempCard?.cardDisplayTimeoutInSeconds ?: 10L
            timerCardDetailsVisibility.postDelayed(
                { toggleCardDetailsVisibility() },
                cardDisplayTimeoutInSeconds * 1000
            )

            activity?.apply {
                Utils.triggerFireBaseEvents(
                    FirebaseManagerAnalyticsProperties.MY_ACCOUNTS_VTC_VIEWCARDNUMBERS,
                    this
                )
            }
        }
    }

    private fun toggleShowCardDetailsButtonState() {
        ivToggleCardDetailsLabelIcon
            .setImageDrawable(
                ResourcesCompat.getDrawable(
                    resources,
                    if (isCardDetailsVisible) R.drawable.ic_barcode_hide else R.drawable.ic_barcode_show,
                    null
                )
            )
        tvTapToViewLabel
            .animate()
            .setDuration(DURATION_FADE)
            .alpha(if (isCardDetailsVisible) 0f else 1f)
            .start()
        tvTapToHideLabel
            .animate()
            .setDuration(DURATION_FADE)
            .alpha(if (isCardDetailsVisible) 1f else 0f)
            .start()
    }

    override fun onDismiss(dialog: DialogInterface) {
        setFragmentResult(
            PayWithCardListFragment.PAY_WITH_CARD_REQUEST_LISTENER, bundleOf(
                PayWithCardListFragment.PAY_WITH_CARD_REQUEST_LISTENER to PAY_WITH_CARD_ON_DISMISS_RESULT_LISTENER))
        super.onDismiss(dialog)
    }

    override fun onDetach() {
        timerCardDetailsVisibility.removeCallbacksAndMessages(null)
        super.onDetach()
    }
}