package za.co.woolworths.financial.services.android.ui.adapters;


import android.content.Context;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.awfs.coordination.R;
import com.daimajia.swipe.SwipeLayout;
import com.daimajia.swipe.adapters.RecyclerSwipeAdapter;

import java.text.ParseException;
import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.MessageDetails;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.WFormatter;

public class MesssagesListAdapter extends RecyclerSwipeAdapter<MesssagesListAdapter.SimpleViewHolder> {

	public interface MessageClickListener {
		void onDeleteItemClicked(String id);

		void messageInboxIsEmpty(int sizeOfList);
	}

	public List<MessageDetails> messageDetailsList;
	private MessageClickListener messageClickListener;

	public MesssagesListAdapter(MessageClickListener messageClickListener, List<MessageDetails> messageDetailsList) {
		this.messageDetailsList = messageDetailsList;
		this.messageClickListener = messageClickListener;
	}

	public static class SimpleViewHolder extends RecyclerView.ViewHolder {
		private final RelativeLayout relContainer;
		SwipeLayout swipeLayout;
		WTextView txtTitle;
		TextView txtDate;
		 TextView txtBody;
		ImageView imgdelete;
		LinearLayout cardlayout;
		View tranparentView;

		public SimpleViewHolder(View itemView) {
			super(itemView);
			swipeLayout = (SwipeLayout) itemView.findViewById(R.id.swipe);
			txtTitle = (WTextView) itemView.findViewById(R.id.msgTitle);
			txtDate = (TextView) itemView.findViewById(R.id.date);
			txtBody = (TextView) itemView.findViewById(R.id.bodyMessage);
			imgdelete = (ImageView) itemView.findViewById(R.id.msgDelete);
			cardlayout = (LinearLayout) itemView.findViewById(R.id.cardLayout);
			tranparentView = (View) itemView.findViewById(R.id.transparentview);
			relContainer = (RelativeLayout) itemView.findViewById(R.id.relContainer);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder viewHolder, final int position) {

		viewHolder.txtTitle.setText(messageDetailsList.get(position).title);
		viewHolder.txtBody.setText(messageDetailsList.get(position).content);
		Context context = viewHolder.txtTitle.getContext();
		if (context == null) return;

		if (messageDetailsList.get(position).isRead) {
			viewHolder.cardlayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_shadow));
			viewHolder.cardlayout.setAlpha(0.5f);
		} else {
			viewHolder.cardlayout.setBackground(ContextCompat.getDrawable(context, R.drawable.card_shadow));
			viewHolder.cardlayout.setAlpha(1f);
		}
		try {
			viewHolder.txtDate.setText(WFormatter.formatMessagingDate(messageDetailsList.get(position).createDate));
		} catch (ParseException e) {
		}

		mItemManger.bindView(viewHolder.itemView, position);
		viewHolder.imgdelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				MessageDetails message = messageDetailsList.get(position);
				if (position < getItemCount()) {
					messageDetailsList.remove(position);
					notifyItemRemoved(position);
					notifyItemRangeChanged(position, getItemCount());
					mItemManger.closeAllItems();
				}
				messageClickListener.messageInboxIsEmpty(getItemCount());
				messageClickListener.onDeleteItemClicked(message.id);
			}
		});
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_list_item, parent, false);
		return new SimpleViewHolder(view);
	}

	@Override
	public int getItemCount() {
		return (messageDetailsList == null) ? 0 : messageDetailsList.size();
	}

	@Override
	public int getSwipeLayoutResourceId(int position) {
		return R.id.swipe;
	}
}