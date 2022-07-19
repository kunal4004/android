package za.co.woolworths.financial.services.android.getstream.chat

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.R
import com.awfs.coordination.databinding.FragmentOneCartChatBinding
import dagger.hilt.android.AndroidEntryPoint
import io.getstream.chat.android.client.models.Message
import za.co.woolworths.financial.services.android.getstream.OCChatActivity
import za.co.woolworths.financial.services.android.getstream.common.ChatState
import za.co.woolworths.financial.services.android.ui.activities.MultipleImageActivity
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.ui.extension.hideKeyboard
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.VtoErrorBottomSheetDialog
import za.co.woolworths.financial.services.android.ui.vto.ui.bottomsheet.listener.VtoTryAgainListener
import javax.inject.Inject

@AndroidEntryPoint
class ChatFragment : Fragment() , VtoTryAgainListener {

    companion object{
        const val ARG_CHANNEL_ID = "channelId"
        const val AUXILIARY_IMAGE = "auxiliaryImages"

    }

    private val viewModel: ChatViewModel by viewModels()
    private var _binding: FragmentOneCartChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerViewAdapter: ChatRecyclerViewAdapter
    private var isErrorDialogVisible: Boolean = true
    @Inject
    lateinit var errorBottomSheetDialog: VtoErrorBottomSheetDialog

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
        setupInputLayout()
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
                if (s.toString().trim().length == 1){
                    viewModel.emitIsTyping()
                }
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
        if (!isOnline && isErrorDialogVisible)
            showErrorDialog()

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
       isErrorDialogVisible = false
       requireContext().apply {
           errorBottomSheetDialog.showErrorBottomSheetDialog(
               this@ChatFragment,
               this,
               getString(R.string.vto_generic_error),
               getString(R.string.one_cart_chat_error_disc),
               getString(R.string.got_it)
           )
       }
    }


    override fun tryAgain() {
        isErrorDialogVisible = true
        requireActivity().finish()
    }
}