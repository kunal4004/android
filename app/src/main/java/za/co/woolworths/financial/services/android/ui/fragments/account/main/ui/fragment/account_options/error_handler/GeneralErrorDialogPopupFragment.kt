package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.error_handler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GeneralErrorDialogPopupFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class GeneralErrorDialogPopupFragment : WBottomSheetDialogFragment() {

    val args: GeneralErrorDialogPopupFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.general_error_dialog_popup_fragment,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = GeneralErrorDialogPopupFragmentBinding.bind(view)
        val response = args.serverErrorResponse
        binding.descriptionTextView.text = response?.desc
        binding.dismissButton.onClick { dismiss() }
    }

}