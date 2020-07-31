package za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account

import android.os.Bundle
import android.transition.Fade
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.pay_my_account_activity.*
import za.co.woolworths.financial.services.android.contracts.IPaymentOptionContract
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl.Companion.ACCOUNT_INFO
import za.co.woolworths.financial.services.android.util.Utils

class PayMyAccountActivity : AppCompatActivity(), IPaymentOptionContract.PayMyAccountView {

    private var mPayMyAccountPresenterImpl: PayMyAccountPresenterImpl? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Utils.updateStatusBarBackground(this)
        setContentView(R.layout.pay_my_account_activity)

        setSupportActionBar(payMyAccountToolbar)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
            setDisplayUseLogoEnabled(false)
            setHomeAsUpIndicator(R.drawable.back24)
        }

        val fade = Fade()
        fade.excludeTarget(R.id.payMyAccountToolbar, true)
        fade.excludeTarget(android.R.id.statusBarBackground, true)
        fade.excludeTarget(android.R.id.navigationBarBackground, true)

        window?.enterTransition = fade
        window?.exitTransition = fade

        mPayMyAccountPresenterImpl = PayMyAccountPresenterImpl(this, PayMyAccountModelImpl())
        mPayMyAccountPresenterImpl?.retrieveAccountBundle(intent)

        with(findNavController(R.id.payMyAccountNavHostFragmentContainerView)) {
            val bundle = Bundle()
            bundle.putString(ACCOUNT_INFO, intent?.getStringExtra(ACCOUNT_INFO))
            setGraph(graph, bundle)
        }
    }

    override fun getPayMyAccountPresenter(): PayMyAccountPresenterImpl? {
        return mPayMyAccountPresenterImpl
    }

    override fun configureToolbar(title: String?) {
        super.configureToolbar(title)
        payMyAccountDivider?.visibility = if (title?.isEmpty() == true) GONE else VISIBLE
        payMyAccountTitleBar?.text = title
    }

}