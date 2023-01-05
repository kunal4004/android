package za.co.woolworths.financial.services.android.onecartgetstream.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentOneCartChatBinding
import io.getstream.chat.android.client.models.Message
import za.co.woolworths.financial.services.android.onecartgetstream.OCChatActivity
import za.co.woolworths.financial.services.android.onecartgetstream.common.ChatState
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard


class ChatFragment : Fragment() {

    companion object{
        const val ARG_CHANNEL_ID = "channelId"
        const val AUXILIARY_IMAGE = "auxiliaryImages"
    }

    private var isPaused: Boolean = false
    private val viewModel: ChatViewModel by activityViewModels()
    private var _binding: FragmentOneCartChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewAdapter: ChatRecyclerViewAdapter


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

        setupRecyclerView()
        setupToolbar()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val orderId = (activity as? OCChatActivity)?.getOrderId()
        setupInputLayout()
        if (isPaused) {
            isPaused = false
            startActivity(OCChatActivity.newIntent(requireActivity(),  orderId ?: ""))
        }
    }

    override fun onPause() {
        super.onPause()
        isPaused = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.disconnect()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.state.observe(
            viewLifecycleOwner
        ) {
            when (it) {
                is ChatState.ReceivedMessagesData -> updateRecyclerViewDataSet()
                is ChatState.ReceivedMessageData -> insertIntoRecyclerViewDataSet(it.message)
                is ChatState.Error -> showErrorDialog()
            }
        }
        viewModel.fetchOtherUser()
        viewModel.fetchMessages()
    }

    private fun setupToolbar(){
        viewModel.isOtherUserOnline.observe(viewLifecycleOwner) {
            updateOtherUserPresenceIndicator(it)

        }
        viewModel.otherUserDisplayName.observe(viewLifecycleOwner) {
            binding.chatToolbarLayout.chatWithPersonName.text = it
        }
        viewModel.otherUserTyping.observe(viewLifecycleOwner) {
            binding.chatToolbarLayout.typingIndicator.text = it
        }

        val orderId = (activity as? OCChatActivity)?.getOrderId()
        binding.chatToolbarLayout.oneCartChatOrderID.text = getString(R.string.one_cart_chat_order_id,orderId)
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = ChatRecyclerViewAdapter(ChatRecyclerViewAdapter.OnClickListener{
            openAttachmentSelectedImage(it)
        },viewModel.messages.toTypedArray(), viewModel.messageItemDelegate)
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
            }
        }

        binding.messageInputLayout.messageInputEditText.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //Do Nothing
            }

            override fun onTextChanged(cs: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (cs.toString().trim().isEmpty()) {
                    viewModel.stopTyping()
                    binding.messageInputLayout.sendMessageImage.alpha = 0.3F
                } else {
                    binding.messageInputLayout.sendMessageImage.alpha = 0.9F
                }
            }
            override fun afterTextChanged(s: Editable?) {
              // do Nothing
            }
        })

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

    private fun updateRecyclerViewDataSet(){
        recyclerViewAdapter.setDataSet(viewModel.messages.toTypedArray())
        binding.messagesRecyclerView.smoothScrollToPosition(recyclerViewAdapter.itemCount -1)
    }

    private fun insertIntoRecyclerViewDataSet(message: Message){
        recyclerViewAdapter.insertDataSetItem(message)
        binding.messagesRecyclerView.smoothScrollToPosition(recyclerViewAdapter.itemCount -1)
    }


    private fun openAttachmentSelectedImage(image: String?) {
        activity?.apply {
            val intent = Intent(requireActivity(), MultipleImageActivity::class.java)
            intent.putExtra(AUXILIARY_IMAGE, image)
            startActivity(intent)
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }

    private fun showErrorDialog(){
        (activity as? OCChatActivity)?.showErrorDialog()
    }

}