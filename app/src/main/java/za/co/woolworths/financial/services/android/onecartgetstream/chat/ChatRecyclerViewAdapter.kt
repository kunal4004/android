package za.co.woolworths.financial.services.android.onecartgetstream.chat

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import com.awfs.coordination.R
import com.awfs.coordination.databinding.OneCartChatMessageRowItemBinding
import io.getstream.chat.android.client.models.Message
import za.co.woolworths.financial.services.android.ui.extension.bindDrawable
import za.co.woolworths.financial.services.android.util.ImageManager

class ChatRecyclerViewAdapter(private val onClickListener: OnClickListener,initialDataSet: Array<Message>,
                              private val messageItemDelegate: IMessageItemDelegate): RecyclerView.Adapter<ChatRecyclerViewAdapter.ViewHolder>() {

    private var dataSet: MutableList<Message> = initialDataSet.toMutableList()

    class ViewHolder(binding: OneCartChatMessageRowItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val messageBubble: LinearLayoutCompat by lazy { binding.messageBubble }
        val messageTextView: AppCompatTextView by lazy { binding.messageTextView }
        val senderTextView: AppCompatTextView by lazy { binding.senderTextView }
        val oneCartChatAttachment: AppCompatImageView by lazy { binding.oneCartChatAttachment }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(viewGroup.context)
        val binding = OneCartChatMessageRowItemBinding.inflate(inflater, viewGroup, false)

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = dataSet[position]
        holder.senderTextView.text = message.user.name
        if (!message.text.isNullOrEmpty()){
            holder.messageTextView.text = message.text
            holder.messageTextView.visibility = View.VISIBLE
            holder.oneCartChatAttachment.visibility = View.GONE
        } else {
            holder.messageTextView.visibility = View.GONE
            holder.oneCartChatAttachment.visibility = View.VISIBLE
            ImageManager.setPictureWithoutPlaceHolder(holder.oneCartChatAttachment,
                message.attachments.getOrNull(0)?.imageUrl.toString())
            holder.oneCartChatAttachment.setOnClickListener {
                onClickListener.onClick(message.attachments.getOrNull(0)?.imageUrl.toString())
            }
        }


        val isMessageOwnedByMe = messageItemDelegate.isMessageOwnedByMe(message)

        holder.messageBubble.updateLayoutParams<ConstraintLayout.LayoutParams> {

            if(isMessageOwnedByMe){

                this.leftToLeft = ConstraintLayout.LayoutParams.UNSET
                this.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID

                //1. bubble background is white
                holder.messageBubble.background = bindDrawable(R.drawable.bg_one_cart_chat_mine_message_bubble)

                //2. sender name should be white
                holder.senderTextView.setTextColor(Color.parseColor("#FFFFFF"))

                //3. message color is off white
                holder.messageTextView.setTextColor(Color.parseColor("#EFEFEF"))

            } else{
                this.rightToRight = ConstraintLayout.LayoutParams.UNSET
                this.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID

                //1. bubble background is black
                holder.messageBubble.background = bindDrawable(R.drawable.bg_one_cart_chat_theirs_message_bubble)

                //2. sender name should be black
                holder.senderTextView.setTextColor(Color.parseColor("#000000"))

                //3. message color is grey
                holder.messageTextView.setTextColor(Color.parseColor("#666666"))

            }
        }
    }

    override fun getItemCount() = dataSet.size

    internal fun setDataSet(value: Array<Message>){
        this.dataSet.clear()
        this.dataSet.addAll(value)

        this.notifyDataSetChanged()
    }

    internal fun insertDataSetItem(value: Message){
        this.dataSet.add(value)

        this.notifyDataSetChanged()
    }

    class OnClickListener(val clickListener: (selectedImage: String) -> Unit) {
        fun onClick(selectedImage: String) = clickListener(selectedImage)
    }
}


