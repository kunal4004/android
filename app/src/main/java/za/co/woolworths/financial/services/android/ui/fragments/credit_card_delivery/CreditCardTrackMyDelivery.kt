package za.co.woolworths.financial.services.android.ui.fragments.credit_card_delivery

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.CreditCardTrackMyDeliveryBinding
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.BundleKeysConstants
import za.co.woolworths.financial.services.android.util.KotlinUtils
import za.co.woolworths.financial.services.android.util.ToastUtils
import za.co.woolworths.financial.services.android.util.ToastUtils.ToastInterface
import kotlin.math.roundToInt

class CreditCardTrackMyDelivery : WBottomSheetDialogFragment(), View.OnClickListener, ToastInterface {

    private lateinit var binding: CreditCardTrackMyDeliveryBinding
    var bundle: Bundle? = null
    private var envelopeNumber: String? = null
    private var mToastUtils: ToastUtils? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = CreditCardTrackMyDeliveryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.init()
    }

    companion object {
        fun newInstance(bundle: Bundle, envelopeNumber: String) = CreditCardTrackMyDelivery().withArgs {
            putBundle(BundleKeysConstants.BUNDLE, bundle)
            putString(BundleKeysConstants.ENVELOPE_NUMBER, envelopeNumber)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mToastUtils = ToastUtils(this)
        bundle = arguments?.getBundle(BundleKeysConstants.BUNDLE)
        envelopeNumber = arguments?.getString(BundleKeysConstants.ENVELOPE_NUMBER, "")
    }

    private fun CreditCardTrackMyDeliveryBinding.init() {
        referenceNumber.text = envelopeNumber
        referenceNumberText.setOnClickListener(this@CreditCardTrackMyDelivery)
        trackMyDelivery.setOnClickListener(this@CreditCardTrackMyDelivery)
        cancelText.setOnClickListener(this@CreditCardTrackMyDelivery)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.trackMyDelivery -> {
                KotlinUtils.openUrlInPhoneBrowser(AppConfigSingleton.creditCardDelivery?.deliveryTrackingUrl, activity)
            }
            R.id.referenceNumberText -> {
                val clipboard = activity?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(binding.referenceNumber.text, binding.referenceNumber.text)
                clipboard.setPrimaryClip(clip)

                val currentActivity: Activity? = activity
                mToastUtils?.apply {
                    activity = currentActivity
                    pixel = ((binding.trackMyDelivery.height * 2.5).roundToInt())
                    view = binding.trackMyDelivery
                    setMessage(bindString(R.string.copied_to_clipboard))
                    viewState = false
                    buildCustomTrackMyDeliveryToast(context)
                }
            }
            R.id.cancelText -> {
                dismiss()
            }
        }
    }

    override fun onToastButtonClicked(currentState: String?) {
        TODO("Not yet implemented")
    }
}