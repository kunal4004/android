package za.co.woolworths.financial.services.android.ui.views.actionsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.RootDeviceInfoFragmentBinding
import za.co.woolworths.financial.services.android.ui.extension.withArgs

class RootedDeviceInfoFragment : WBottomSheetDialogFragment() {

    private lateinit var binding: RootDeviceInfoFragmentBinding
    private var mDescription: String? = null

    companion object {
        private const val DESCRIPTION = "DESCRIPTION"
        fun newInstance(description: String) = RootedDeviceInfoFragment().withArgs {
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
        binding = RootDeviceInfoFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            tvDescription?.text = mDescription
            gotItButton?.setOnClickListener { finishActivity() }
        }
    }

    override fun onDetach() {
        super.onDetach()
        finishActivity()
    }

    private fun finishActivity() {
        activity?.apply {
            finish()
            overridePendingTransition(0, 0)
        }
    }
}