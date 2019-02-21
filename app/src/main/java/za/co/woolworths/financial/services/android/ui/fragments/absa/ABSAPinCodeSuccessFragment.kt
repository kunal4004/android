package za.co.woolworths.financial.services.android.ui.fragments.absa

import android.app.Activity.RESULT_OK
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.absa_pin_code_complete_fragment.*
import za.co.woolworths.financial.services.android.util.SessionUtilities

class ABSAPinCodeSuccessFragment : Fragment() {

    companion object {
        const val DELAY_CLOSING_ACTIVITY = 2000
        fun newInstance() = ABSAPinCodeSuccessFragment()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.absa_pin_code_complete_fragment, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideActionBar()
        initView()
        closeActivity()
    }

    private fun hideActionBar() {
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }

    private fun closeActivity() {
        val handler: Handler? = Handler()
        handler?.postDelayed({
            activity?.apply {
                setResult(RESULT_OK)
                finish()
                overridePendingTransition(R.anim.stay, android.R.anim.fade_out)
            }
        }, DELAY_CLOSING_ACTIVITY.toLong())
    }

    private fun initView() {
        val jwtDecoded = SessionUtilities.getInstance().jwt
        val name = jwtDecoded?.name?.get(0)
        tvTitle.text = getString(R.string.absa_success_title, name)
    }
}