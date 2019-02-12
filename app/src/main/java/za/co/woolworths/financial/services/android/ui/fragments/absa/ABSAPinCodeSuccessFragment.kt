package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.absa_pin_code_complete_fragment.*
import za.co.woolworths.financial.services.android.util.SessionUtilities


class ABSAPinCodeSuccessFragment : Fragment() {

    companion object {
        fun newInstance() = ABSAPinCodeSuccessFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_pin_code_complete_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        val jwtDecoded = SessionUtilities.getInstance().jwt
        val name = jwtDecoded?.name?.get(0)
        tvTitle.text = getString(R.string.absa_success_title, name)
    }

    override fun onResume() {
        super.onResume()
        activity?.let {
            (it as? AppCompatActivity)?.supportActionBar?.hide()
        }
    }
}