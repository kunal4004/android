package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ErrorDialogWithOkButtonFragmentBinding
import za.co.woolworths.financial.services.android.ui.activities.StatementActivity
import za.co.woolworths.financial.services.android.ui.activities.WTransactionsActivity
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class AccountsErrorHandlerFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: ErrorDialogWithOkButtonFragmentBinding
    private var mDescription: String? = null

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        fun newInstance(description: String) = AccountsErrorHandlerFragment().withArgs {
            putString(DESCRIPTION, description)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            mDescription = getString(DESCRIPTION)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = ErrorDialogWithOkButtonFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvDescription?.text = mDescription
            okButton?.setOnClickListener { dismiss() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (activity is WTransactionsActivity|| activity is StatementActivity) {
            activity?.apply {
                finish()
                overridePendingTransition(R.anim.stay, R.anim.slide_down_anim)
            }
        }
    }
}