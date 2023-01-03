package za.co.woolworths.financial.services.android.ui.fragments.account.chat.ui

import android.os.Bundle
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.fragment.app.activityViewModels
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ChatRetrieveAbsaCardTokenFragmentBinding
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.activities.WChatActivity
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.ChatViewModel
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.helper.LiveChatDBRepository
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_OK
import za.co.woolworths.financial.services.android.util.AppConstant.Companion.HTTP_SESSION_TIMEOUT_440
import za.co.woolworths.financial.services.android.util.ErrorHandlerView
import za.co.woolworths.financial.services.android.util.NetworkManager
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.animation.AnimationUtilExtension
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding
import java.net.ConnectException

class ChatRetrieveABSACardTokenFragment : BaseFragmentBinding<ChatRetrieveAbsaCardTokenFragmentBinding>(ChatRetrieveAbsaCardTokenFragmentBinding::inflate), View.OnClickListener {

    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as? WChatActivity)?.apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
            displayEndSessionButton(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.retryErrorButton?.apply {
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
        binding.chatLoaderProgressBar?.visibility = GONE
        binding.groupRetryErrorUI?.visibility = VISIBLE
    }

    private fun showProgress() {
        binding.chatLoaderProgressBar?.visibility = VISIBLE
        binding.groupRetryErrorUI?.visibility = GONE
    }

    private fun noConnectionToast() {
        activity?.let { ErrorHandlerView(it).showToast() }
    }
}