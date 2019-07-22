package za.co.woolworths.financial.services.android.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.ChatMessage
import kotlinx.android.synthetic.main.received_message_item.view.*
import kotlinx.android.synthetic.main.sent_message_item.view.*

private const val VIEW_TYPE_RECEIVED_MESSAGE = 1
private const val VIEW_TYPE_SENT_MESSAGE = 2

class WChatAdapter : RecyclerView.Adapter<MessageViewHolder>() {
    private val messages: ArrayList<ChatMessage> = arrayListOf()

    fun addMessage(message: ChatMessage) {
        messages.add(message)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return if (viewType == VIEW_TYPE_RECEIVED_MESSAGE) {
            ReceivedMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.received_message_item, parent, false))
        } else {
            SentMessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.sent_message_item, parent, false))
        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemViewType(position: Int): Int {
        return when (messages[position].type) {
            ChatMessage.Type.RECEIVED -> VIEW_TYPE_RECEIVED_MESSAGE
            ChatMessage.Type.SENT -> VIEW_TYPE_SENT_MESSAGE
        }
    }

    inner class ReceivedMessageViewHolder(view: View) : MessageViewHolder(view) {

        override fun bind(message: ChatMessage) {
            itemView.received_message_text.text = message.message
        }
    }

    inner class SentMessageViewHolder(view: View) : MessageViewHolder(view) {

        override fun bind(message: ChatMessage) {
            itemView.sent_message_text.text = message.message
        }
    }
}

open class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    open fun bind(message: ChatMessage) {}
}