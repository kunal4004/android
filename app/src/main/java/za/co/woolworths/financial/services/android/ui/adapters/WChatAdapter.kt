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
import com.awfs.coordination.R
import kotlinx.android.synthetic.main.received_message_item.view.*
import kotlinx.android.synthetic.main.sent_message_item.view.*
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.ChatMessage

import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SendMessageResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.chat.model.SenderMessage
import za.co.woolworths.financial.services.android.util.KotlinUtils


private const val VIEW_TYPE_RECEIVED_MESSAGE = 1
private const val VIEW_TYPE_SENT_MESSAGE = 2

class WChatAdapter : RecyclerView.Adapter<MessageViewHolder>() {
    private val chatMessageList: MutableList<ChatMessage> = mutableListOf()

    fun getMessageList() = chatMessageList

    fun addMessage(message: ChatMessage) {
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
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == VIEW_TYPE_RECEIVED_MESSAGE) {
            AgentMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.received_message_item, parent, false)
            )
        } else {
            SenderMessageViewHolder(
                LayoutInflater.from(parent.context)
                    .inflate(R.layout.sent_message_item, parent, false)
            )
        }
    }

    override fun getItemCount(): Int {
        return chatMessageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(chatMessageList[position])
    }


    override fun getItemViewType(position: Int): Int {
        return when (chatMessageList[position]) {
            is SendMessageResponse -> VIEW_TYPE_RECEIVED_MESSAGE
            is SenderMessage -> VIEW_TYPE_SENT_MESSAGE
        }
    }

    fun clear() {
        chatMessageList?.clear()
        notifyDataSetChanged()
    }

    inner class AgentMessageViewHolder(view: View) : MessageViewHolder(view) {

        override fun bind(chatMessage: ChatMessage) {
            val agentMessage = chatMessage as? SendMessageResponse
            val sendEmail = agentMessage?.sendEmailIntentInfo
            val emailAddress = sendEmail?.emailAddress ?: ""
            when (emailAddress.isNotEmpty()) {
                true -> {
                    val spannableMessage = SpannableString(agentMessage?.content)
                    val clickableSpan: ClickableSpan = object : ClickableSpan() {
                        override fun onClick(textView: View) {
                            (itemView.context as? Activity)?.let { activity ->
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
                    itemView.received_message_text?.apply {
                        text = spannableMessage
                        movementMethod = LinkMovementMethod.getInstance()
                        highlightColor = Color.GRAY
                    }

                }
                false -> {
                    itemView.received_message_text?.apply {
                        text = agentMessage?.content
                        movementMethod = null
                        highlightColor = Color.WHITE
                    }
                }
            }

            itemView.image_message_profile?.visibility =
                if (agentMessage?.isWoolworthIconVisible == true) VISIBLE else INVISIBLE
        }
    }

    inner class SenderMessageViewHolder(view: View) : MessageViewHolder(view) {

        override fun bind(chatMessage: ChatMessage) {
            val userMessage = chatMessage as? SenderMessage
            itemView.sent_message_text?.text = userMessage?.message
        }
    }
}

open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: ChatMessage) {}
}