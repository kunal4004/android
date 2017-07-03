package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class UserDetailAdapter extends RecyclerView.Adapter<UserDetailAdapter.SimpleViewHolder> {
	public interface UserDetailInterface {
		void onRowSelected(View v, int position);
	}

	private UserDetailInterface mUserDetailInterface;
	private int selectedIndex = -1;
	private List<String> mUserDetail;

	public UserDetailAdapter(List<String> userDetail,
	                         UserDetailInterface userDetailInterface) {
		this.mUserDetail = userDetail;
		this.mUserDetailInterface = userDetailInterface;
	}

	class SimpleViewHolder extends RecyclerView.ViewHolder {
		WTextView mTvMyDetail;
		RelativeLayout mRlContainer;

		SimpleViewHolder(View view) {
			super(view);
			mTvMyDetail = (WTextView) view.findViewById(R.id.tvMyDetail);
			mRlContainer = (RelativeLayout) view.findViewById(R.id.rlMyDetails);
		}
	}

	@Override
	public void onBindViewHolder(final SimpleViewHolder holder, final int position) {
		String userDetail = mUserDetail.get(position);
		holder.mTvMyDetail.setText(userDetail);
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				selectedIndex = position;
				mUserDetailInterface.onRowSelected(v, holder.getAdapterPosition());
				notifyDataSetChanged();
			}
		});

		if (selectedIndex == position) {
			backgroundDrawable(holder, R.drawable.pressed_bg_light);
		} else {
			backgroundDrawable(holder, R.drawable.stores_details_background);
		}
	}

	@Override
	public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_detail_row,
				parent, false);
		return new SimpleViewHolder(view);
	}

	@Override
	public int getItemCount() {
		return mUserDetail.size();
	}

	public void resetIndex() {
		selectedIndex = -1;
		notifyDataSetChanged();
	}

	private void backgroundDrawable(SimpleViewHolder holder, int id) {
		holder.mRlContainer.setBackground(ContextCompat.getDrawable(holder.mRlContainer.getContext(), id));
	}
}