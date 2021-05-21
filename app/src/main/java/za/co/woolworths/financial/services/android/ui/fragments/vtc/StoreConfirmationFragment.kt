package za.co.woolworths.financial.services.android.ui.fragments.vtc

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.fragment_store_confirmation.*
import kotlinx.android.synthetic.main.layout_confirmation.*
import kotlinx.android.synthetic.main.layout_store_card_confirmed.*
import za.co.woolworths.financial.services.android.contracts.IResponseListener
import za.co.woolworths.financial.services.android.models.network.CompletionHandler
import za.co.woolworths.financial.services.android.models.network.GenericResponse
import za.co.woolworths.financial.services.android.models.network.OneAppService
import za.co.woolworths.financial.services.android.models.network.StoreCardEmailConfirmBody
import za.co.woolworths.financial.services.android.ui.activities.ErrorHandlerActivity
import za.co.woolworths.financial.services.android.util.AppConstant
import za.co.woolworths.financial.services.android.util.ErrorHandlerView

class StoreConfirmationFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.close_menu_item, menu)
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
        val body = getEmailConfirmationBody()
        val emailRequest = OneAppService.confirmStoreCardEmail(body)
        emailRequest.enqueue(CompletionHandler(object : IResponseListener<GenericResponse> {
            override fun onSuccess(response: GenericResponse?) {
                processingViewGroup?.visibility = View.GONE
                when (response?.response?.code) {
                    AppConstant.HTTP_OK.toString() -> {
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
        return StoreCardEmailConfirmBody()
    }

    companion object {
        const val STORE_DETAILS = "STORE_DETAILS"
    }
}