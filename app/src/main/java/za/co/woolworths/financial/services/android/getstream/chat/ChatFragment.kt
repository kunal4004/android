package za.co.woolworths.financial.services.android.getstream.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentOneCartChatBinding
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Message
import za.co.woolworths.financial.services.android.common.SingleMessageCommonToast
import za.co.woolworths.financial.services.android.getstream.common.ChatState
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() {

    companion object{
        const val ARG_CHANNEL_ID = "channelId"
    }

    private val viewModel: ChatViewModel by viewModels()
    private var _binding: FragmentOneCartChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewAdapter: ChatRecyclerViewAdapter
    @Inject
    lateinit var showEmptyMessageAlert: SingleMessageCommonToast

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            viewModel.channelId = it.getString(ARG_CHANNEL_ID).toString()
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentOneCartChatBinding.inflate(inflater, container, false)

        setupToolbar()
        setupRecyclerView()
        setupInputLayout()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.state.observe(
            viewLifecycleOwner,
            {
                when (it) {
                    is ChatState.ReceivedMessagesData -> updateRecyclerViewDataSet()
                    is ChatState.ReceivedMessageData -> insertIntoRecyclerViewDataSet(it.message)
                    is ChatState.Error -> showErrorMessage(it.errorMessage)
                }
            }
        )
        viewModel.fetchOtherUser()
        viewModel.fetchMessages()
    }

    override fun onResume() {
        super.onResume()
        viewModel.startWatching()
    }

    override fun onPause() {
        super.onPause()
        viewModel.stopWatching()
    }

    private fun setupToolbar(){
        viewModel.isOtherUserOnline.observe(viewLifecycleOwner) {
            updateOtherUserPresenceIndicator(it)
        }
        viewModel.otherUserDisplayName.observe(viewLifecycleOwner) {
            binding.chatToolbarLayout.chatWithPersonName.text = it
        }
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = ChatRecyclerViewAdapter(viewModel.messages.toTypedArray(), viewModel.messageItemDelegate)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.messagesRecyclerView.adapter = recyclerViewAdapter
    }

    private fun setupInputLayout(){
        binding.messageInputLayout.sendMessageImage.setOnClickListener {
            val messageText = binding.messageInputLayout.messageInputEditText.text.toString()
            if (!messageText.isNullOrEmpty()) {
                viewModel.sendMessage(messageText)
                binding.messageInputLayout.messageInputEditText.hideKeyboard(requireActivity() as AppCompatActivity)
                binding.messageInputLayout.messageInputEditText.clearFocus()
                binding.messageInputLayout.messageInputEditText.text?.clear()
            } else {
                showEmptyMessageAlert.showMessage(requireActivity(), getString(R.string.please_enter_a_message), 250)
            }
            binding.messageInputLayout.messageInputEditText.addTextChangedListener(object :
                TextWatcher {
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    // TODO: ("Not yet implemented")
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    viewModel.emitIsTyping()
                }

                override fun afterTextChanged(p0: Editable?) {
                    //TODO: ("Not yet implemented")
                }
            })

        }
    }

    private fun updateOtherUserPresenceIndicator(isOnline: Boolean) {
        binding.chatToolbarLayout.activeStateTextView.text = if (isOnline) getString(
            R.string.currently_active) else getString(
            R.string.currently_in_active)
        binding.chatToolbarLayout.activeInactiveStatus.background =
            if (isOnline) bindDrawable(R.drawable.bg_one_cart_active_status) else
                bindDrawable(R.drawable.bg_one_cart_inactive_status)
        binding.chatToolbarLayout.chatBackImg.setOnClickListener {
            requireActivity().finish()
        }

    }

    private fun showErrorMessage(errorMessage: String?){
        Toast.makeText(activity, errorMessage, Toast.LENGTH_LONG).show()
    }

    private fun updateRecyclerViewDataSet(){
        recyclerViewAdapter.setDataSet(viewModel.messages.toTypedArray())
        binding.messagesRecyclerView.smoothScrollToPosition(recyclerViewAdapter.itemCount -1)
    }

    private fun insertIntoRecyclerViewDataSet(message: Message){
        recyclerViewAdapter.insertDataSetItem(message)
        binding.messagesRecyclerView.smoothScrollToPosition(recyclerViewAdapter.itemCount -1)
    }
}