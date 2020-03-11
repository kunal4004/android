package za.co.woolworths.financial.services.android.ui.fragments.otp

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.retrieve_otp_error_fragment.*
import za.co.woolworths.financial.services.android.util.Utils

class RetrieveOTPErrorFragment : Fragment() {

    var navController: NavController? = null
    var bundle: Bundle? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.retrieve_otp_error_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        retry.setOnClickListener { navController?.navigate(R.id.action_to_retrieveOTPFragment, bundleOf("bundle" to bundle)) }
        needHelp?.apply {
            paintFlags = Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener { activity?.apply { Utils.makeCall("0861 50 20 20") } }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bundle = arguments?.getBundle("bundle")
    }
}