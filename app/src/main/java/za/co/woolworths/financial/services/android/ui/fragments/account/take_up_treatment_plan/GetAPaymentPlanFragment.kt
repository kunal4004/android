package za.co.woolworths.financial.services.android.ui.fragments.account.take_up_treatment_plan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.databinding.GetAPaymentPlanFragmentBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class GetAPaymentPlanFragment : Fragment(){

private val getPaymentViewModel : GetAPaymentViewModel? by activityViewModels()

private lateinit var binding: GetAPaymentPlanFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = GetAPaymentPlanFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

}