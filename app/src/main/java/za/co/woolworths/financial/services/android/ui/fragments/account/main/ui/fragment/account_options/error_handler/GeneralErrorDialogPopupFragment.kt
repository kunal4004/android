package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.error_handler

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import com.awfs.coordination.databinding.GeneralErrorDialogPopupFragmentBinding
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.extension.withArgs
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class GeneralErrorDialogPopupFragment : WBottomSheetDialogFragment() {

    val args: GeneralErrorDialogPopupFragmentArgs by navArgs()

    companion object {
        private const val SERVER_ERROR_RESPONSE = "SERVER_ERROR_RESPONSE"
        fun newInstance(response: ServerErrorResponse) = GeneralErrorDialogPopupFragment().withArgs {
            putParcelable(SERVER_ERROR_RESPONSE, response)
        }
    }

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
        val response = arguments?.getParcelable<ServerErrorResponse>(SERVER_ERROR_RESPONSE)
        binding.titleTextView.visibility = GONE
        binding.descriptionTextView.text = response?.desc
        binding.dismissButton.onClick { dismiss() }
    }

}