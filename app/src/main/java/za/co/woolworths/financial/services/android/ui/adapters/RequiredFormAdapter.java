package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.awfs.coordination.R;

import java.util.List;

public class RequiredFormAdapter extends RecyclerView.Adapter<RequiredFormAdapter.MyViewHolder> {

	private List<Integer> item;
	private boolean imageIsVisible;

	class MyViewHolder extends RecyclerView.ViewHolder {
		private TextView tvTitle;
		private ImageView imIcon;

		MyViewHolder(View view) {
			super(view);
			tvTitle = view.findViewById(R.id.tvTitle);
			imIcon = view.findViewById(R.id.imIcon);
		}
	}

	public RequiredFormAdapter(List<Integer> itemList, boolean imageIsVisible) {
		this.item = itemList;
		this.imageIsVisible = imageIsVisible;
	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.required_form_item, parent, false));
	}

	@Override
	public void onBindViewHolder(final MyViewHolder holder, final int position) {
		holder.imIcon.setVisibility(imageIsVisible ? View.VISIBLE : View.GONE);
		holder.tvTitle.setText(item.get(position));
	}

	@Override
	public int getItemCount() {
		return item.size();
	}
}

