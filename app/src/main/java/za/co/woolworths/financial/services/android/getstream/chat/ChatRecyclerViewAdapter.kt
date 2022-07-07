package za.co.woolworths.financial.services.android.getstream.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.databinding.OneCartChatMessageRowBinding
import io.getstream.chat.android.client.models.Message

class ChatRecyclerViewAdapter(private var dataSet: Array<Message>): RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(binding: OneCartChatMessageRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val messageTextView: TextView by lazy { binding.messageTextView }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = OneCartChatMessageRowBinding.inflate(inflater, viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.messageTextView.text = dataSet[position].text
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    public fun setDataSet(value: Array<Message>){
        this.dataSet = value
    }
}