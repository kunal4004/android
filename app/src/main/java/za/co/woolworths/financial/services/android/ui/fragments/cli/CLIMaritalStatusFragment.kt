package za.co.woolworths.financial.services.android.ui.fragments.cli

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_cli_marital_status.*
import za.co.woolworths.financial.services.android.contracts.MaritalStatusListener
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dto.MaritalStatus
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.FragmentUtils
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.picker.WheelView

class CLIMaritalStatusFragment : Fragment(), WheelView.OnItemSelectedListener<Any>, View.OnClickListener {

    private var isChecked: Boolean = false
    private var selectedMaritalStatus: MaritalStatus? = null
    private var maritalStatusListener: MaritalStatusListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is MaritalStatusListener) {
            maritalStatusListener = context
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_cli_marital_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        WoolworthsApplication.getInstance()?.creditLimitIncrease?.init()

        // Default selected position
        setMaritalStatusPicker(0)

        //Set Listeners
        cli_marital_status_next.setOnClickListener(this)
        cli_marital_status_agreement_check.setOnClickListener(this)
        cli_marital_status_selection?.setOnClickListener(this)
        cli_marital_status_picker_done?.setOnClickListener(this)
        cli_marital_status_picker?.onItemSelectedListener = this
    }

    companion object {
        @JvmStatic
        fun newInstance() = CLIMaritalStatusFragment()
    }

    override fun onItemSelected(wheelView: WheelView<Any>?, data: Any?, position: Int) {
        when (wheelView?.id) {
            R.id.cli_marital_status_picker -> {
                val maritalStatusList = WoolworthsApplication.getInstance()?.creditLimitIncrease?.maritalStatusList
                if (maritalStatusList == null || maritalStatusList.isEmpty() || position >= maritalStatusList.size) {
                    return
                }
                selectedMaritalStatus = maritalStatusList[position]
                selectedMaritalStatus?.let { setMaritalStatusPicker(position) }
            }
        }
    }

    private fun setMaritalStatusPicker(position: Int) {
        val maritalStatusList = WoolworthsApplication.getInstance()?.creditLimitIncrease?.maritalStatusList
        if (maritalStatusList == null || maritalStatusList.isEmpty()) {
            return
        }
        val dataList = maritalStatusList.map { it.statusDesc }
        selectedMaritalStatus = maritalStatusList[position]
        cli_marital_status_picker?.apply {
            data = dataList
            selectedItemPosition = position
        }
    }

    override fun onClick(v: View?) {

        when (v?.id) {

            R.id.cli_marital_status_selection -> {
                cli_marital_status_picker_container.visibility = View.VISIBLE
//                AnimationUtilExtension.slideUp(cli_marital_status_picker_container)
            }

            R.id.cli_marital_status_picker_done -> {
                setMaritalStatusPicker(cli_marital_status_picker?.selectedItemPosition ?: 0)

//                AnimationUtilExtension.slideDown(cli_marital_status_picker_container)

                cli_marital_status_picker_container.visibility = View.GONE

                cli_marital_status_agreement_container?.visibility = if (selectedMaritalStatus?.statusId == 6) View.VISIBLE else View.GONE
                cli_marital_status_agreement_container?.visibility = if (selectedMaritalStatus?.statusId == 6) View.VISIBLE else View.GONE
                cli_marital_status_selection?.text = selectedMaritalStatus?.statusDesc

                // Set marital status id for Create and update offer application.
                selectedMaritalStatus?.let { maritalStatusListener?.setMaritalStatus(it) }

                updateNextButtonStatus()
            }

            R.id.cli_marital_status_agreement_check -> {
                isChecked = !isChecked
                context?.let {
                    cli_marital_status_agreement_check?.setImageDrawable(ContextCompat.getDrawable(it,
                            if (isChecked) R.drawable.checked_item else R.drawable.uncheck_item))
                }
                updateNextButtonStatus()
            }

            R.id.cli_marital_status_next -> {
                val fragmentUtils = FragmentUtils()
                fragmentUtils.nextFragment(parentFragmentManager, CLIAllStepsContainerFragment(), R.id.cliMainFrame)
            }
        }
    }

    private fun updateNextButtonStatus() {
        when (selectedMaritalStatus?.statusId) {
            6 -> {
                context?.let {
                    cli_marital_status_next?.setBackgroundColor(ContextCompat.getColor(it,
                            if (isChecked) R.color.black else R.color.button_disable))
                }
                cli_marital_status_next?.isEnabled = isChecked
            }
            0 -> {
                context?.let {
                    cli_marital_status_next?.setBackgroundColor(ContextCompat.getColor(it, R.color.button_disable))
                    cli_marital_status_next?.isEnabled = false
                }
            }
            else -> {
                context?.let { cli_marital_status_next?.setBackgroundColor(ContextCompat.getColor(it, R.color.black)) }
                cli_marital_status_next?.isEnabled = true
            }
        }
    }
}