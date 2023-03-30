package za.co.woolworths.financial.services.android.ui.fragments.account.petinsurance

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.databinding.FragmentPetInsurancePendingBinding
import za.co.woolworths.financial.services.android.ui.views.actionsheet.WBottomSheetDialogFragment

class PetInsurancePendingFragment : WBottomSheetDialogFragment() {
    private var _binding: FragmentPetInsurancePendingBinding? = null
    private val binding get() = _binding!!
    companion object {
        fun newInstance() = PetInsurancePendingFragment()
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetInsurancePendingBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnPetInsurancePending.setOnClickListener {
            dismiss()
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}