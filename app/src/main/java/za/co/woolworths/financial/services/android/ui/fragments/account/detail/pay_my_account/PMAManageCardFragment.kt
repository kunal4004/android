package za.co.woolworths.financial.services.android.ui.fragments.account.detail.pay_my_account

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.pma_manage_card_fragment.*
import za.co.woolworths.financial.services.android.models.dto.Account
import za.co.woolworths.financial.services.android.models.dto.GetPaymentMethod
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountActivity
import za.co.woolworths.financial.services.android.ui.activities.account.sign_in.pay_my_account.PayMyAccountPresenterImpl
import za.co.woolworths.financial.services.android.ui.adapters.PMACardsAdapter
import za.co.woolworths.financial.services.android.ui.extension.bindString
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension

class PMAManageCardFragment : Fragment(), View.OnClickListener {

    private var accountInfo: String? = null
    private var paymentMethod: String? = null
    private var mAccounts: Account? = null
    private var manageCardAdapter: PMACardsAdapter? = null
    private var mPaymentMethod: MutableList<GetPaymentMethod>? = null
    private var navController: NavController? = null

    val args: PMAProcessRequestFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            accountInfo = getString(PayMyAccountPresenterImpl.ACCOUNT_INFO, "")
            paymentMethod = getString(PayMyAccountPresenterImpl.PAYMENT_METHOD, "")
            mAccounts = Gson().fromJson(accountInfo, Account::class.java)
            mPaymentMethod = Gson().fromJson<MutableList<GetPaymentMethod>>(paymentMethod, object : TypeToken<MutableList<GetPaymentMethod>>() {}.type)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pma_manage_card_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        configureToolbar()
        configureRecyclerview()
        useThisCardButton?.apply {
            setOnClickListener(this@PMAManageCardFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }

        addCardTextView?.apply {
            setOnClickListener(this@PMAManageCardFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    private fun configureRecyclerview() {

//        val array = mutableListOf<GetPaymentMethod>()
//        val payM = mPaymentMethod?.get(0)
//        payM?.let { array.add(it) }
//        payM?.let { array.add(it) }
//        payM?.let { array.add(it) }
//        payM?.let { array.add(it) }
//        payM?.let { array.add(it) }
//        payM?.let { array.add(it) }
//
//        for (list in array) {
//            mPaymentMethod?.add(list)
//        }

        pmaManageCardRecyclerView?.apply {
            layoutManager = activity?.let { LinearLayoutManager(it, LinearLayoutManager.VERTICAL, false) }

            manageCardAdapter = PMACardsAdapter(mPaymentMethod) { paymentMethod ->
                Log.e("xxPaymentMethod",Gson().toJson(paymentMethod))
            }
            adapter = manageCardAdapter
        }
    }

    private fun configureToolbar() {
        (activity as? PayMyAccountActivity)?.apply {
            configureToolbar(bindString(R.string.credit_debit_cards_label))
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            displayToolbarDivider(true)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.useThisCardButton -> {
                val vendorCardDetail = PMAManageCardFragmentDirections.actionManageCardFragmentToDisplayVendorCardDetailFragment(paymentMethod, mAccounts)
                navController?.navigate(vendorCardDetail)
            }

            R.id.addCardTextView -> {
                val manageCard = PMAManageCardFragmentDirections.actionManageCardFragmentToAddNewPayUCardFragment(mAccounts)
                navController?.navigate(manageCard)
            }
        }
    }
}