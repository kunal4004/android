package za.co.woolworths.financial.services.android.ui.adapters;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.view.LayoutInflater;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.statement.UserStatement;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class StatementAdapter extends RecyclerView.Adapter<StatementAdapter.StatementViewHolder> {


	public interface StatementListener {
		void onItemClicked(View v, int position);

		void onViewClicked(View v, int position, UserStatement statement);
	}

	private StatementListener statementListener;
	private final static int HEADER_VIEW = 0;
	private final static int CONTENT_VIEW = 1;
	private final ArrayList<UserStatement> mItems;
	private boolean viewWasClicked = true;

	public StatementAdapter(StatementListener statementListener) {
		this.statementListener = statementListener;
		this.mItems = new ArrayList<>();
	}

	@Override
	public StatementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		int layoutRes = 0;
		switch (viewType) {
			case HEADER_VIEW:
				layoutRes = R.layout.account_e_statement_title;
				break;
			case CONTENT_VIEW:
				layoutRes = R.layout.account_e_statement_row;
				break;
		}

		View view = LayoutInflater.from(parent.getContext()).inflate(layoutRes, parent, false);
		return new StatementViewHolder(view);
	}

	@Override
	public int getItemViewType(int position) {
		switch (position) {
			case 0:
				return HEADER_VIEW;
			default:
				return CONTENT_VIEW;
		}
	}

	@Override
	public void onBindViewHolder(StatementViewHolder holder, int position) {
		UserStatement statement = mItems.get(position);
		switch (getItemViewType(position)) {
			case HEADER_VIEW:
				break;
			case CONTENT_VIEW:
				holder.populateMonth(statement, holder.tvStatementName);
				holder.showLoading(statement, holder.imCheckItem, holder.pbLoading, holder.tvViewStatement);
				holder.updateViewStatementUI(statement, holder.tvViewStatement, holder.pbLoading);
				onClickListener(holder);
				break;
			default:
				break;
		}
	}

	private void onClickListener(final StatementViewHolder holder) {
		holder.itemView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!viewClicked()) {  // disable click
					return;
				}
				statementListener.onItemClicked(v, holder.getAdapterPosition());
			}
		});
		holder.tvViewStatement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!viewClicked()) {  // disable click
					return;
				}
				int position = holder.getAdapterPosition();
				statementListener.onViewClicked(v, position, mItems.get(position));
			}
		});
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public class StatementViewHolder extends RecyclerView.ViewHolder {
		private final ProgressBar pbLoading;
		private WTextView tvStatementName, tvViewStatement;
		private ImageView imCheckItem;

		public StatementViewHolder(View itemView) {
			super(itemView);
			tvStatementName = (WTextView) itemView.findViewById(R.id.tvStatementName);
			tvViewStatement = (WTextView) itemView.findViewById(R.id.tvViewStatement);
			imCheckItem = (ImageView) itemView.findViewById(R.id.imCheckItem);
			pbLoading = (ProgressBar) itemView.findViewById(R.id.pbLoading);
		}

		public void populateMonth(UserStatement statement, WTextView view) {
			view.setText(formatDate(statement.docDesc));
		}


		public void updateViewStatementUI(UserStatement statement, WTextView view, ProgressBar pbLoading) {
			if (statement.getStatementView()) {
				hideView(view);
				hideView(pbLoading);
			}
		}

		public void showLoading(UserStatement statement, ImageView im, ProgressBar pb, WTextView tv) {
			if (statement.getStatementView()) {
				hideView(pb);
				hideView(tv);
			} else {
				showView(pb);
				showView(tv);
			}

			if (statement.selectedByUser()) { // show checked item
				im.setImageResource(R.drawable.checked_item);
			} else {
				im.setImageResource(R.drawable.uncheck_item);//show unchecked item
				if (statement.viewIsLoading()) { // check to verify is progressbar loading
					hideView(tv);
					showView(pb);
				} else {
					hideView(pb);
					showView(tv);
				}
			}
		}
	}

	public void add(UserStatement item) {
		mItems.add(item);
		notifyItemInserted(mItems.size() - 1);
	}

	public ArrayList<UserStatement> getStatementList() {
		return mItems;
	}

	public void refreshBlockOverlay(int position) {
		notifyItemChanged(position);
	}

	public void updateStatementViewState(boolean value) {
		int index = 0;
		for (UserStatement statement : mItems) {
			statement.showStatementView(value);
			refreshBlockOverlay(index);
			index++;
		}
	}

	public void onViewClicked(int position, boolean viewIsLoading) {
		if (viewIsLoading) {
			viewWasClicked(false);
		} else {
			viewWasClicked(true);
		}
		mItems.get(position).setViewIsLoading(viewIsLoading);
		notifyItemChanged(position);
	}

	public void showView(View v) {
		v.setVisibility(View.VISIBLE);
	}

	public void hideView(View v) {
		v.setVisibility(View.GONE);
	}

	public void viewWasClicked(boolean viewIsClickable) {
		this.viewWasClicked = viewIsClickable;
	}

	public boolean viewClicked() {
		return viewWasClicked;
	}

	public String formatDate(String _Date) {
		try {
			return new SimpleDateFormat("MMMM yyyy").format(new SimpleDateFormat("dd MMM yyyy").parse(_Date));
		} catch (ParseException pe) {
			return "";
		}
	}
}
