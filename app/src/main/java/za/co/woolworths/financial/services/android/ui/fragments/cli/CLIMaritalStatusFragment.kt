package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentCliMaritalStatusBinding
import za.co.woolworths.financial.services.android.contracts.MaritalStatusListener
import za.co.woolworths.financial.services.android.models.AppConfigSingleton
import za.co.woolworths.financial.services.android.models.dto.app_config.credit_limit_increase.ConfigMaritalStatus
import za.co.woolworths.financial.services.android.ui.activities.cli.CLIPhase2Activity
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.picker.WheelView

class CLIMaritalStatusFragment : Fragment(R.layout.fragment_cli_marital_status), WheelView.OnItemSelectedListener<Any>, View.OnClickListener {

    private lateinit var binding: FragmentCliMaritalStatusBinding
    private var isChecked: Boolean = false
    private var selectedMaritalStatus: ConfigMaritalStatus? = null
    private var maritalStatusListener: MaritalStatusListener? = null
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MaritalStatusListener) {
            maritalStatusListener = context
        }
        mContext = context
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentCliMaritalStatusBinding.bind(view)

        AppConfigSingleton.creditLimitIncrease?.maritalStatus?.apply {
            if (!contains(ConfigMaritalStatus(0, mContext.getString(R.string.please_select))))
                add(0, ConfigMaritalStatus(0, mContext.getString(R.string.please_select)))
        }

        binding.apply {
            //set default text for picker selection.
            cliMaritalStatusSelection.text = mContext.getString(R.string.select)

            (activity as? CLIPhase2Activity)?.selectedMaritalStatusPosition?.let {
                setMaritalStatusPicker(it)
                cliMaritalStatusPickerDone?.performClick()
            } ?: run {
                // Default selected position
                setMaritalStatusPicker(0)
            }

            //Set Listeners
            cliMaritalStatusNext?.setOnClickListener(this@CLIMaritalStatusFragment)
            cliMaritalStatusAgreementCheck?.setOnClickListener(this@CLIMaritalStatusFragment)
            cliMaritalStatusSelection?.setOnClickListener(this@CLIMaritalStatusFragment)
            cliMaritalStatusPickerDone?.setOnClickListener(this@CLIMaritalStatusFragment)
            cliMaritalStatusPicker?.onItemSelectedListener = this@CLIMaritalStatusFragment
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = CLIMaritalStatusFragment()
    }

    override fun onItemSelected(wheelView: WheelView<Any>?, data: Any?, position: Int) {
        when (wheelView?.id) {
            R.id.cli_marital_status_picker -> {
                val maritalStatusList = AppConfigSingleton.creditLimitIncrease?.maritalStatus
                if (maritalStatusList == null || maritalStatusList.isEmpty() || position >= maritalStatusList.size) {
                    return
                }
                selectedMaritalStatus = maritalStatusList[position]
                (activity as? CLIPhase2Activity)?.selectedMaritalStatusPosition = position
                selectedMaritalStatus?.let { setMaritalStatusPicker(position) }
            }
        }
    }

    private fun setMaritalStatusPicker(position: Int) {
        val maritalStatusList =
            AppConfigSingleton.creditLimitIncrease?.maritalStatus
        if (maritalStatusList == null || maritalStatusList.isEmpty()) {
            return
        }
        val dataList = maritalStatusList.map { it.statusDesc }
        selectedMaritalStatus = maritalStatusList[position]
            binding.cliMaritalStatusPicker?.apply {
                data = dataList
                selectedItemPosition = position
        }
    }

    override fun onClick(v: View?) {

        binding.apply {
            when (v?.id) {

                R.id.cli_marital_status_selection -> {
                    cliMaritalStatusPickerContainer.visibility = View.VISIBLE
                    val slideUpAnimation: Animation = AnimationUtils.loadAnimation(
                        context,
                        R.anim.slide_in_up
                    )
                    cliMaritalStatusPickerContainer.startAnimation(slideUpAnimation)
                }

                R.id.cli_marital_status_picker_done -> {
                    setMaritalStatusPicker(cliMaritalStatusPicker?.selectedItemPosition ?: 0)

                    val slideDownAnimation: Animation = AnimationUtils.loadAnimation(
                        context,
                        R.anim.slide_down_anim
                    )
                    cliMaritalStatusPickerContainer?.startAnimation(slideDownAnimation)

                    cliMaritalStatusPickerContainer?.visibility = View.GONE

                    cliMaritalStatusAgreementContainer?.visibility =
                        if (selectedMaritalStatus?.statusId == 6) View.VISIBLE else View.GONE
                    cliMaritalStatusAgreementContainer?.visibility =
                        if (selectedMaritalStatus?.statusId == 6) View.VISIBLE else View.GONE

                    cliMaritalStatusSelection?.text =
                        if (mContext.getString(R.string.please_select)
                                .equals(selectedMaritalStatus?.statusDesc, ignoreCase = true)
                        ) context?.getString(R.string.select) else selectedMaritalStatus?.statusDesc

                    // Set marital status id for Create and update offer application.
                    selectedMaritalStatus?.let { maritalStatusListener?.setMaritalStatus(it) }

                    updateNextButtonStatus()
                }

                R.id.cli_marital_status_agreement_check -> {
                    isChecked = !isChecked
                    context?.let {
                        cliMaritalStatusAgreementCheck?.setImageDrawable(
                            ContextCompat.getDrawable(
                                it,
                                if (isChecked) R.drawable.checked_item else R.drawable.uncheck_item
                            )
                        )
                    }
                    updateNextButtonStatus()
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

    private fun updateNextButtonStatus() {
        binding.apply {
            when (selectedMaritalStatus?.statusId) {
                6 -> {
                    context?.let {
                        cliMaritalStatusNext?.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                if (isChecked) R.color.black else R.color.button_disable
                            )
                        )
                    }
                    cliMaritalStatusNext?.isEnabled = isChecked
                }
                0 -> {
                    context?.let {
                        cliMaritalStatusNext?.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                R.color.button_disable
                            )
                        )
                        cliMaritalStatusNext?.isEnabled = false
                    }
                }
                else -> {
                    context?.let {
                        cliMaritalStatusNext?.setBackgroundColor(
                            ContextCompat.getColor(
                                it,
                                R.color.black
                            )
                        )
                    }
                    cliMaritalStatusNext?.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        (binding.root?.parent as? ViewGroup)?.removeView(binding.root)
        super.onDestroy()
    }
}