package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.content.DialogInterface
import android.content.res.Resources
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import kotlinx.android.synthetic.main.scan_barcode_to_pay_dialog.*
import kotlinx.android.synthetic.main.scan_barcode_to_pay_dialog.barCodeImage
import za.co.woolworths.financial.services.android.models.dto.temporary_store_card.StoreCardsResponse
import za.co.woolworths.financial.services.android.ui.activities.card.MyCardDetailActivity.Companion.STORE_CARD_DETAIL
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardDetailFragment
import za.co.woolworths.financial.services.android.ui.fragments.npc.MyCardExtension
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment
import za.co.woolworths.financial.services.android.util.Utils


class ScanBarcodeToPayDialogFragment : WBottomSheetDialogFragment() {
    private var mStoreCardDetail: String? = null
    private var mStoreCardsResponse: StoreCardsResponse? = null
    private var listener: IOnTemporaryStoreCardDialogDismiss? = null
    private var isRegenerateBarcode: Boolean = false

    interface IOnTemporaryStoreCardDialogDismiss {
        fun onDialogDismiss()
        fun onRegenerateBarcode()
    }

    companion object {

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
        regenerateBarcode.setOnClickListener { regenerateBarcode() }
        configureUI()
        setUniqueIds()
    }

    private fun configureUI() {
        mStoreCardsResponse?.storeCardsData?.virtualCard?.let {
            barCodeImage.setImageBitmap(Utils.encodeAsBitmap(it.number.plus(it.sequence), BarcodeFormat.CODE_128, Resources.getSystem().displayMetrics.widthPixels, 60))
        }
        cardHolderName.text = MyCardExtension.toTitleCase(MyCardDetailFragment.cardName())
    }

    private fun regenerateBarcode() {
        isRegenerateBarcode = true
        dismissAllowingStateLoss()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        if (isRegenerateBarcode) listener?.onRegenerateBarcode() else listener?.onDialogDismiss()
    }

    private fun setUniqueIds() {
        activity?.resources?.apply {
            title?.contentDescription = getString(R.string.scan_barcode_title)
            description?.contentDescription = getString(R.string.scan_barcode_description)
            barCodeImage?.contentDescription = getString(R.string.barcode_image)
            regenerateBarcode?.contentDescription = getString(R.string.scan_barcode_regenerate)
            cardHolderName?.contentDescription = getString(R.string.text_cardHolderName)
        }
    }
}