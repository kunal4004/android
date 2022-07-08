package za.co.woolworths.financial.services.android.getstream.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OneCartChatMessageRowBinding
import io.getstream.chat.android.client.models.Message
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable

class ChatRecyclerViewAdapter(private var initialDataSet: Array<Message>, private val messageItemDelegate: IMessageItemDelegate): RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    private var dataSet: MutableList<Message> = initialDataSet.toMutableList()

    class ViewHolder(binding: OneCartChatMessageRowBinding) : RecyclerView.ViewHolder(binding.root) {
        val messageBubble: LinearLayoutCompat by lazy { binding.messageBubble }
        val messageTextView: AppCompatTextView by lazy { binding.messageTextView }
        val senderTextView: AppCompatTextView by lazy { binding.senderTextView }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = OneCartChatMessageRowBinding.inflate(inflater, viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataSet[position]
        holder.senderTextView.text = message.user.name
        holder.messageTextView.text = message.text

        val isMessageOwnedByMe = messageItemDelegate.isMessageOwnedByMe(message)

        holder.messageBubble.updateLayoutParams<ConstraintLayout.LayoutParams> {

            if(isMessageOwnedByMe){

                this.leftToLeft = ConstraintLayout.LayoutParams.UNSET
                this.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID

                //1. bubble background is white
                holder.messageBubble.background = bindDrawable(R.drawable.bg_one_cart_chat_mine)

                //2. sender name should be white
                holder.senderTextView.setTextColor(Color.parseColor("#FFFFFF"))

                //3. message color is off white
                holder.messageTextView.setTextColor(Color.parseColor("#EFEFEF"))

            } else{
                this.rightToRight = ConstraintLayout.LayoutParams.UNSET
                this.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID

                //1. bubble background is black
                holder.messageBubble.background = bindDrawable(R.drawable.bg_one_cart_chat_there)

                //2. sender name should be black
                holder.senderTextView.setTextColor(Color.parseColor("#000000"))

                //3. message color is grey
                holder.messageTextView.setTextColor(Color.parseColor("#666666"))

            }
        }
    }

    override fun getItemCount() = dataSet.size

    public fun setDataSet(value: Array<Message>){
        this.dataSet.clear()
        this.dataSet.addAll(value)

        this.notifyDataSetChanged()
    }

    public fun insertDataSetItem(value: Message){
        this.dataSet.add(value)

        this.notifyDataSetChanged()
    }
}