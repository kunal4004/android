package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ValidateOtpErrorFragmentBinding
import za.co.woolworths.financial.services.android.util.Utils

class ValidateOTPErrorFragment : Fragment(R.layout.validate_otp_error_fragment) {

    private lateinit var binding: ValidateOtpErrorFragmentBinding
    var bundle: Bundle? = null
    var navController: NavController? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = ValidateOtpErrorFragmentBinding.bind(view)
        navController = Navigation.findNavController(view)
        binding.retry.setOnClickListener { navController?.navigate(R.id.action_to_validateOTPFragment, bundleOf("bundle" to bundle)) }
        binding.needHelp?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { activity?.apply { Utils.makeCall("0861 50 20 20") } }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
    }

}