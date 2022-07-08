package za.co.woolworths.financial.services.android.getstream.chat

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.awfs.coordination.databinding.FragmentOneCartChatBinding
import io.getstream.chat.android.client.models.Message
import io.getstream.chat.android.livedata.ChatDomain
import za.co.woolworths.financial.services.android.getstream.common.ChatState



class ChatFragment : Fragment() {

    companion object{
        const val ARG_CHANNEL_ID = "channelId"
    }

    private val viewModel: ChatViewModel by viewModels()
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
        setupInputLayout()
        setupPresenceIndicator()
        setupPresenceIndicator()

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.state.observe(
                viewLifecycleOwner,
                Observer {
                    when (it) {
                        is ChatState.ReceivedMessagesData -> updateRecyclerViewDataSet()
                        is ChatState.ReceivedMessageData -> insertIntoRecyclerViewDataSet(it.message)
                        is ChatState.Error -> showErrorMessage(it.errorMessage)
                    }
                }
        )

        ChatDomain.instance().typingUpdates.observe(viewLifecycleOwner, Observer {
            Log.d("Someone", "is typing")
        })

        viewModel.fetchMessages()
    }

    private fun setupRecyclerView(){
        recyclerViewAdapter = ChatRecyclerViewAdapter(viewModel.messages.toTypedArray(), viewModel.messageItemDelegate)

        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.messagesRecyclerView.adapter = recyclerViewAdapter
    }

    private fun setupInputLayout(){
        binding.messageInputLayout.sendMessageImage.setOnClickListener{
            val messageText = binding.messageInputLayout.messageInputEditText.text.toString()
            viewModel.sendMessage(messageText)

            binding.messageInputLayout.messageInputEditText.clearFocus()
            binding.messageInputLayout.messageInputEditText.text?.clear()

            binding.messageInputLayout.messageInputEditText.addTextChangedListener(object : TextWatcher {
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
            //TODO dismiss the keyboard
        }
    }

    private fun setupPresenceIndicator(){
        viewModel.observeOtherUserPresence()
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