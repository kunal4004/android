package za.co.woolworths.financial.services.android.ui.adapters

import android.app.Activity
import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.ReceivedMessageItemBinding
import com.awfs.coordination.databinding.SentMessageItemBinding
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SenderMessage
import za.co.woolworths.financial.services.android.util.KotlinUtils

private const val VIEW_TYPE_RECEIVED_MESSAGE = 1
private const val VIEW_TYPE_SENT_MESSAGE = 2

class WChatAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val chatMessageList: MutableList<ChatMessage> = mutableListOf()

    fun getMessageList() = chatMessageList

    fun addMessage(activity: Activity?,message: ChatMessage) {
        chatMessageList.add(message)
        //Display 1 woolies icon per message received
        var messagesSize = chatMessageList.size
        if (messagesSize > 1) {
            messagesSize -= 1
            for (i in 1..messagesSize) {
                val chatMessage: ChatMessage = chatMessageList[i]
                if (chatMessage is SendMessageResponse) {
                    if (chatMessage.javaClass == chatMessageList[i - 1].javaClass) {
                        chatMessage.isWoolworthIconVisible = false
                    }
                }

            }
        }
        activity?.runOnUiThread { notifyDataSetChanged()  }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_RECEIVED_MESSAGE) {
            AgentMessageViewHolder(
                ReceivedMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        } else {
            SenderMessageViewHolder(
                SentMessageItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessageList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is AgentMessageViewHolder -> {
                holder.bind(chatMessageList[position])
            }
            is SenderMessageViewHolder -> {
                holder.bind(chatMessageList[position])
            }
        }
    }


    override fun getItemViewType(position: Int): Int {
        return when (chatMessageList[position]) {
            is SendMessageResponse -> VIEW_TYPE_RECEIVED_MESSAGE
            is SenderMessage -> VIEW_TYPE_SENT_MESSAGE
        }
    }

    fun clear() {
        chatMessageList.clear()
    }

    inner class AgentMessageViewHolder(val itemBinding: ReceivedMessageItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(chatMessage: ChatMessage) {
            val agentMessage = chatMessage as? SendMessageResponse
            val sendEmail = agentMessage?.sendEmailIntentInfo
            val emailAddress = sendEmail?.emailAddress ?: ""
            when (emailAddress.isNotEmpty()) {
                true -> {
                    val spannableMessage = SpannableString(agentMessage?.content)
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(textView: View) {
                            (itemBinding.root.context as? Activity)?.let { activity ->
                                KotlinUtils.sendEmail(
                                    activity,
                                    emailAddress,
                                    sendEmail?.subjectLine,
                                    ""
                                )
                            }
                        }

                        override fun updateDrawState(ds: TextPaint) {
                            super.updateDrawState(ds)
                            ds.color = Color.WHITE
                            ds.isUnderlineText = true
                        }
                    }
                    spannableMessage.setSpan(
                        clickableSpan,
                        spannableMessage.indexOf(emailAddress),
                        spannableMessage.indexOf(emailAddress) + emailAddress.length,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                    itemBinding.receivedMessageText?.apply {
                        text = spannableMessage
                        movementMethod = LinkMovementMethod.getInstance()
                        highlightColor = Color.GRAY
                    }

                }
                false -> {
                    itemBinding.receivedMessageText?.apply {
                        text = agentMessage?.content
                        movementMethod = null
                        highlightColor = Color.WHITE
                    }
                }
            }

            itemBinding.imageMessageProfile?.visibility =
                if (agentMessage?.isWoolworthIconVisible == true) VISIBLE else INVISIBLE
        }
    }

    inner class SenderMessageViewHolder(val itemBinding: SentMessageItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {

        fun bind(chatMessage: ChatMessage) {
            val userMessage = chatMessage as? SenderMessage
            itemBinding.sentMessageText?.text = userMessage?.message
        }
    }
}