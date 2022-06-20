package za.co.woolworths.financial.services.android.ui.fragments.shop.helpandsupport

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.layout_help_and_support_fragement.*
import androidx.recyclerview.widget.LinearLayoutManager
import za.co.woolworths.financial.services.android.util.Utils

class HelpAndSupportFragment: Fragment(R.layout.layout_help_and_support_fragement) ,
    HelpAndSupportAdapter.HelpAndSupportClickListener {

    companion object {
        fun newInstance() = HelpAndSupportFragment()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpHelpAndSupportUi()
    }

    private fun setUpHelpAndSupportUi() {
        val dataList = prepareHelpAndSupportList()
        val adapter = HelpAndSupportAdapter(context, dataList, this)
        val llm = LinearLayoutManager(context)
        llm.orientation = LinearLayoutManager.VERTICAL
        rvHelpAndSupport.setLayoutManager(llm)
        rvHelpAndSupport.setAdapter(adapter)
        imgDelBack?.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    fun prepareHelpAndSupportList(): ArrayList<HelpAndSupport> {
        /* prepare data list as per delivery type , currently done for standard and CNC only*/
        val dataList = arrayListOf<HelpAndSupport>()
        dataList.add(HelpAndSupport(getString(R.string.dash_call_customer_care),
            getString(R.string.dash_customer_care_no), R.drawable.help_phone))
        dataList.add(HelpAndSupport(getString(R.string.cancel_order),
            "", R.drawable.ic_dash_cancel_order))
        return dataList
    }

    override fun openCallSupport(contactNumber: String) {
        Utils.makeCall(contactNumber)
    }
}