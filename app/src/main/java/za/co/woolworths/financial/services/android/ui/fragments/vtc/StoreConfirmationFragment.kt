package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.awfs.coordination.R
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_store_confirmation.*
import kotlinx.android.synthetic.main.layout_confirmation.*
import kotlinx.android.synthetic.main.layout_confirmation.titleTextView
import kotlinx.android.synthetic.main.layout_store_card_confirmed.*
import za.co.woolworths.financial.services.android.contracts.FirebaseManagerAnalyticsProperties
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.StoreCardEmailConfirmBody
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.ui.activities.card.SelectStoreActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.Utils

class StoreConfirmationFragment : Fragment() {

    private var body: StoreCardEmailConfirmBody? = null
    private var menuBar: Menu? = null
    private var isConfirmStore: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            val storeDetails = getString(STORE_DETAILS, null)
            body = Gson().fromJson(storeDetails, StoreCardEmailConfirmBody::class.java)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setActionBar()

        if (!TextUtils.isEmpty(body?.storeAddress)) {
            isConfirmStore = true
            subTitleTextView?.text = body?.storeAddress
        } else {
            isConfirmStore = false
            body?.let {
                // Two types of address we receive in bundle
                // 1. Already present storeAddress when select store (Can be empty or null for some stores)
                // 2. Manually entered field from store address fragment = street, complexName, businessName, city, postalCode, province
                var address = it.complexName  + ", " + it.street + ", "
                address += if (TextUtils.isEmpty(it.businessName)) {
                    it.city + ", " + it.province + ", " + it.postalCode
                } else {
                    it.businessName + ", " + it.city + ", " + it.province  + ", " + it.postalCode
                }
                subTitleTextView?.text = address
            }
        }


        context?.let {
            nextActionTextView.text =
                    if (isConfirmStore) it.getString(R.string.confirm_store) else it.getString(R.string.confirm_address)
            titleTextView.text =
                    if (isConfirmStore) it.getString(R.string.please_confirm_your_nselected_store) else it.getString(R.string.please_confirm_your_address)
            cancelActionTextView.text =
                    if (isConfirmStore) it.getString(R.string.edit_store) else it.getString(R.string.edit_address)
        }

        nextActionTextView?.setOnClickListener {
            callConfirmStoreAPI()
        }

        cancelActionTextView?.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }

        emailGotItBtn?.setOnClickListener {
            activity?.apply {
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun setActionBar() {
        (activity as? SelectStoreActivity)?.apply {
            val mActionBar = supportActionBar
            actionBar?.hide()
            mActionBar?.setDisplayHomeAsUpEnabled(false)
            mActionBar?.setDisplayUseLogoEnabled(false)
            mActionBar?.setHomeAsUpIndicator(null)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
        menuBar = menu
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.closeIcon -> {
                activity?.apply {
                    finish()
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun callConfirmStoreAPI() {
        processingViewGroup?.visibility = View.VISIBLE
        confirmStoreLayout?.visibility = View.GONE
        val body = getEmailConfirmationBody()
        if (TextUtils.isEmpty(body?.visionAccountNumber)) {
            showErrorScreen(ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION)
            return
        }

        val emailRequest = OneAppService.confirmStoreCardEmail(body)
        emailRequest.enqueue(CompletionHandler(object : IResponseListener<GenericResponse> {
            override fun onSuccess(response: GenericResponse?) {
                activity?.apply {
                    if (isConfirmStore) {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_REPLACE_CARD_STORE_DELIVERY, this)
                    } else {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_REPLACE_CARD_F2F, this)
                    }
                }

                processingViewGroup?.visibility = View.GONE
                when (response?.httpCode?.toString() ?: response?.response?.code?.toString()
                ?: "0") {
                    AppConstant.HTTP_OK_201.toString(), AppConstant.HTTP_OK.toString() -> {
                        menuBar?.getItem(0)?.isVisible =
                            menuBar?.getItem(0)?.itemId == R.id.closeIcon
                        (activity as? SelectStoreActivity)?.apply {
                            actionBar?.show()
                        }
                        storeConfirmedLayout?.visibility = View.VISIBLE
                    }
                    AppConstant.HTTP_SESSION_TIMEOUT_400.toString() -> {
                        showErrorScreen(
                            ErrorHandlerActivity.ERROR_STORE_CARD_DUPLICATE_CARD_REPLACEMENT,
                            response?.response?.desc?.toString() ?: "")
                    }
                    else -> {
                        showErrorScreen(ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                activity?.apply {
                    if (isConfirmStore) {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_REPLACE_CARD_STORE_DELIVERY, this)
                    } else {
                        Utils.triggerFireBaseEvents(FirebaseManagerAnalyticsProperties.MYACCOUNTS_REPLACE_CARD_F2F, this)
                    }
                }
                processingViewGroup?.visibility = View.GONE
                showErrorScreen(ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION)
            }
        }, GenericResponse::class.java))
    }

    private fun showErrorScreen(errorType: Int, errorMessage: String = "") {
        activity?.let {
            val intent = Intent(it, ErrorHandlerActivity::class.java)
            intent.putExtra("errorType", errorType)
            intent.putExtra("errorMessage", errorMessage)
            it.startActivityForResult(intent, ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE)
        }
    }

    private fun getEmailConfirmationBody(): StoreCardEmailConfirmBody {
        return body ?: StoreCardEmailConfirmBody()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_CANCELED) {
            activity?.finish()
        }

        when (requestCode) {
            ErrorHandlerActivity.ERROR_PAGE_REQUEST_CODE -> {
                when (resultCode) {
                    ErrorHandlerActivity.RESULT_RETRY -> {
                        callConfirmStoreAPI()
                    }
                    ErrorHandlerActivity.RESULT_CALL_CENTER -> {
                        Utils.makeCall(AppConstant.WOOLWOORTH_CALL_CENTER_NUMBER)
                    }
                }
            }
        }
    }


    companion object {
        const val STORE_DETAILS = "STORE_DETAILS"
    }
}