package za.co.woolworths.financial.services.android.ui.fragments.temporary_store_card

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.temporary_store_card_expire_info_dialog.*
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class TemporaryStoreCardExpireInfoDialog : WBottomSheetDialogFragment() {
    companion object {
        fun newInstance() = TemporaryStoreCardExpireInfoDialog().withArgs {
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.temporary_store_card_expire_info_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        done.setOnClickListener { dismiss() }
    }
}