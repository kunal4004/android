package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCliMaritalStatusBinding
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.ui.fragments.cli.MaritalStatusSelectionFragment.Companion.MaritalStatusResultCode
import za.co.woolworths.financial.services.android.util.FragmentUtils

class CLIMaritalStatusFragment : Fragment(R.layout.fragment_cli_marital_status), View.OnClickListener {

    private lateinit var binding: FragmentCliMaritalStatusBinding
    private var isChecked: Boolean = false

    val maritalStatusViewModel : MaritalStatusViewModel by activityViewModels()
    var statusId: Int? = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCliMaritalStatusBinding.bind(view)
        binding.apply {
            //set default text for picker selection.
            cliMaritalStatusSelection.text = requireActivity().getString(R.string.select)
            //Set Listeners
            cliMaritalStatusNext.setOnClickListener(this@CLIMaritalStatusFragment)
            cliMaritalStatusAgreementCheck.setOnClickListener(this@CLIMaritalStatusFragment)
            cliMaritalStatusSelection.setOnClickListener(this@CLIMaritalStatusFragment)
            confirmAgreementTextview.setOnClickListener(this@CLIMaritalStatusFragment)
        }

        setFragmentResultListener(MaritalStatusResultCode){ _, bundle ->
            when (val result  = bundle.get(MaritalStatusResultCode) as? MaritalStatusSelection) {
                is MaritalStatusSelection.OnSelected -> {
                     statusId = result.configMaritalStatus.statusId
                    (activity as? CLIPhase2Activity)?.selectedMaritalStatusPosition = statusId
                    binding.cliMaritalStatusSelection.text = result.configMaritalStatus.statusDesc
                    (activity as? CLIPhase2Activity)?.setMaritalStatus(result.configMaritalStatus)
                    binding.cliMaritalStatusAgreementContainer.visibility = if (maritalStatusViewModel.isCliStatusId6(statusId)) VISIBLE else GONE
                    updateNextButtonStatus(statusId = statusId)
                }
                else -> Unit
            }
            binding.cliMaritalStatusSelection.isEnabled = true
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = CLIMaritalStatusFragment()
    }

    override fun onClick(v: View?) {

        binding.apply {
            when (v?.id) {

                R.id.confirmAgreementTextview -> cliMaritalStatusAgreementCheck.performClick()

                R.id.cli_marital_status_selection -> {
                    cliMaritalStatusSelection.isEnabled = false
                    val selectMaritalStatusFragment=
                        MaritalStatusSelectionFragment()
                    selectMaritalStatusFragment.show(
                        requireActivity().supportFragmentManager,
                        MaritalStatusSelectionFragment::class.java.simpleName
                    )
                }


                R.id.cli_marital_status_agreement_check -> {
                    isChecked = !isChecked
                    context?.let {
                        cliMaritalStatusAgreementCheck.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                if (isChecked) R.drawable.checked_item else R.drawable.uncheck_item
                            )
                        )
                    }
                    updateNextButtonStatus(statusId = statusId)
                }

                R.id.cli_marital_status_next -> {
                    (activity as? CLIPhase2Activity)?.getFirebaseEvent()?.forMaritialStatus()
                    val fragmentUtils = FragmentUtils()
                    fragmentUtils.nextFragment(
                        parentFragmentManager,
                        CLIAllStepsContainerFragment(),
                        R.id.cliMainFrame
                    )
                }
            }
        }
    }

    private fun updateNextButtonStatus(statusId : Int?) {
        binding.apply {
            when (statusId) {
                6 -> {
                    context?.let {
                        cliMaritalStatusNext.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                if (isChecked) R.color.black else R.color.button_disable
                            )
                        )
                    }
                    cliMaritalStatusNext.isEnabled = isChecked
                }
                0 -> {
                    context?.let {
                        cliMaritalStatusNext.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                R.color.button_disable
                            )
                        )
                        cliMaritalStatusNext.isEnabled = false
                    }
                }
                else -> {
                    context?.let {
                        cliMaritalStatusNext.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                R.color.black
                            )
                        )
                    }
                    cliMaritalStatusNext.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        (binding.root.parent as? ViewGroup)?.removeView(binding.root)
        super.onDestroy()
    }
}