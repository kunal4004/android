package za.co.woolworths.financial.services.android.ui.wfs.common.biometric

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import com.awfs.coordination.databinding.BiometricActivationConfirmationDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BiometricActivationConfirmationDialog : BottomSheetDialogFragment() {

    private val className = BiometricActivationConfirmationDialog::class.java.simpleName
    companion object {
        const val confirmLabel = "yes"
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val  binding = BiometricActivationConfirmationDialogBinding.inflate(inflater, container, false)
        binding.initView()
        return binding.root
    }

    private fun BiometricActivationConfirmationDialogBinding.initView() {
        noButton.setOnClickListener { dismiss() }
        yesButton.setOnClickListener { setFragmentResult(className, bundleOf( confirmLabel to confirmLabel)) }
    }

}