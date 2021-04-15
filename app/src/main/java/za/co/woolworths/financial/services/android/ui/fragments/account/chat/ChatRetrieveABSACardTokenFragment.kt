package za.co.woolworths.financial.services.android.ui.fragments.account.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.chat_retrieve_absa_card_token_fragment.*
import kotlinx.android.synthetic.main.chat_retrieve_absa_card_token_fragment.chatLoaderProgressBar
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import java.net.ConnectException

class ChatRetrieveABSACardTokenFragment : Fragment(), View.OnClickListener {

    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? WChatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            setChatState(true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.chat_retrieve_absa_card_token_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        retryErrorButton?.apply {
            setOnClickListener(this@ChatRetrieveABSACardTokenFragment)
            AnimationUtilExtension.animateViewPushDown(this)
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.retryErrorButton -> if (NetworkManager.getInstance().isConnectedToNetwork(activity)) getCardToken() else noConnectionToast()
        }
    }

    private fun getCardToken() {
        with(chatViewModel) {
            showProgress()
            getCreditCardToken({ result ->
                result?.cards = null
                when (result?.httpCode) {
                    HTTP_OK -> {
                        val cards = result.cards
                        if (cards.isNullOrEmpty()) {
                            (activity as? WChatActivity)?.setStartDestination(R.id.chatToCollectionAgentOfflineFragment)
                        } else {
                            LiveChatDBRepository().saveABSACardsList(cards)
                            (activity as? WChatActivity)?.setStartDestination(R.id.chatFragment)
                        }
                    }
                    HTTP_SESSION_TIMEOUT_440 -> activity?.let { SessionUtilities.getInstance().setSessionState(SessionDao.SESSION_STATE.INACTIVE, result.response.stsParams, it) }

                    else -> (activity as? WChatActivity)?.setStartDestination(R.id.chatToCollectionAgentOfflineFragment)

                }
                stopProgress()
            }, { error ->
                activity?.runOnUiThread {
                    stopProgress()
                    when (error) {
                        is ConnectException -> noConnectionToast()
                    }
                }
            })
        }
    }

    private fun stopProgress() {
        chatLoaderProgressBar?.visibility = GONE
        groupRetryErrorUI?.visibility = VISIBLE
    }

    private fun showProgress() {
        chatLoaderProgressBar?.visibility = VISIBLE
        groupRetryErrorUI?.visibility = GONE
    }

    private fun noConnectionToast() {
        activity?.let { ErrorHandlerView(it).showToast() }
    }
}