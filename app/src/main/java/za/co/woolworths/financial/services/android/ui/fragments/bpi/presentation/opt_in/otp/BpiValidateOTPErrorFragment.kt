package za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.opt_in.otp

import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.bpi_validate_otp_error_fragment.*
import kotlinx.android.synthetic.main.circle_progress_layout.*
import za.co.woolworths.financial.services.android.ui.extension.onClick
import za.co.woolworths.financial.services.android.ui.fragments.bpi.presentation.BalanceProtectionInsuranceActivity

class BpiValidateOTPErrorFragment : Fragment() {

    private var mCircularProgressIndicator: ProgressIndicator? = null
    var bundle: Bundle? = null
    var navController: NavController? = null

    val args: BpiValidateOTPErrorFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.bpi_validate_otp_error_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as? BalanceProtectionInsuranceActivity)?.hideDisplayHomeAsUpEnabled()
        navController = Navigation.findNavController(view)

        failureProgressIndicator()

        retry.onClick {
           val navigateTo =  when (bundle?.getString("screenType", "")) {
                BpiValidateOTPFragment::class.java.simpleName -> R.id.action_bpiValidateOTPErrorFragment_to_bpiValidateOTPFragment
                BPIProcessingRequestFragment::class.java.simpleName ->  R.id.action_bpiValidateOTPErrorFragment_to_BPIProcessingRequestFragment
               else -> null
            }

            navigateTo?.let { destination ->
                navController?.navigate(destination, bundleOf("bundle" to bundle))
            }
        }
    }

    private fun failureProgressIndicator() {
        mCircularProgressIndicator = ProgressIndicator(circularProgressIndicator, success_frame, imFailureIcon, success_tick)
        mCircularProgressIndicator?.apply {
            progressIndicatorListener {  }
            animationStatus = ProgressIndicator.AnimationStatus.Failure
            spin()
            stopSpinning()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        bundle = arguments?.getBundle("bundle")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.closeIcon -> {
                activity?.apply {
                    finish()
                    overridePendingTransition(0,0)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}