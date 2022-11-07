package za.co.woolworths.financial.services.android.ui.fragments.account.applynow.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.databinding.FragmentApplyNowBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.activities.ApplyNowViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.applynow.adapters.ApplyNowMainAdapter

const val ARG_POSITION = "position"

@AndroidEntryPoint
class ApplyNowFragment : Fragment() {
    val viewModel: ApplyNowViewModel by activityViewModels()
    private var _binding: FragmentApplyNowBinding? = null
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApplyNowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.takeIf { it.containsKey(ARG_POSITION) }?.apply {
                binding.rcvApplyNow.adapter = ApplyNowMainAdapter(viewModel.applyNowResponse.value?.content!![this.getInt("position")].children)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}