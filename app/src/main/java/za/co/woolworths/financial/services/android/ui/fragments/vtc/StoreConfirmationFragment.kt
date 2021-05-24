package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.app.Activity
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
import kotlinx.android.synthetic.main.layout_store_card_confirmed.*
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            val storeDetails = getString(STORE_DETAILS, null)
            body = Gson().fromJson(storeDetails, StoreCardEmailConfirmBody::class.java)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_store_confirmation, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setHasOptionsMenu(true)
        setActionBar()

        if (!TextUtils.isEmpty(body?.storeAddress)) {
            subTitleTextView?.text = body?.storeAddress
        } else {
            body?.let {
//               address = street, complexName,businessName, city,postalCode,province
                var address = it.street + ", " + it.complexName + ", "
                address += if (TextUtils.isEmpty(it.businessName)) {
                    it.city + ", " + it.postalCode + ", " + it.province
                } else {
                    it.businessName + ", " + it.city + ", " + it.postalCode + ", " + it.province
                }
                subTitleTextView?.text = address
            }
        }

        nextActionTextView?.setOnClickListener {
            callConfirmStoreAPI()
        }

        cancelActionTextView?.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }

        emailGotItBtn?.setOnClickListener {
            activity?.apply {
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
                processingViewGroup?.visibility = View.GONE
                when (response?.response?.code) {
                    AppConstant.HTTP_OK_201.toString(), AppConstant.HTTP_OK.toString() -> {
                        menuBar?.getItem(0)?.isVisible = menuBar?.getItem(0)?.itemId == R.id.closeIcon
                        (activity as? SelectStoreActivity)?.apply {
                            actionBar?.show()
                        }
                        storeConfirmedLayout?.visibility = View.VISIBLE
                    }
                    else -> {
                        showErrorScreen(ErrorHandlerActivity.ERROR_STORE_CARD_EMAIL_CONFIRMATION)
                    }
                }
            }

            override fun onFailure(error: Throwable?) {
                super.onFailure(error)
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
            processingViewGroup?.visibility = View.GONE
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