package za.co.woolworths.financial.services.android.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import android.view.LayoutInflater;

import com.awfs.coordination.R;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.models.dto.statement.Statement;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

public class StatementAdapter extends RecyclerView.Adapter<StatementAdapter.StatementViewHolder> {


	public interface StatementListener {
		void onItemClicked(View v, int position);

		void onViewClicked(View v, int position);
	}

	private StatementListener statementListener;
	private final static int HEADER_VIEW = 0;
	private final static int CONTENT_VIEW = 1;
	private final ArrayList<Statement> mItems;

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
		Statement statement = mItems.get(position);
		switch (getItemViewType(position)) {
			case HEADER_VIEW:
				break;
			case CONTENT_VIEW:
				holder.populateMonth(statement, holder.tvStatementName);
				holder.updateCheckStatementUI(statement, holder.imCheckItem);
				holder.updateViewStatementUI(statement, holder.tvViewStatement);
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
				statementListener.onItemClicked(v, holder.getAdapterPosition());
			}
		});
		holder.tvViewStatement.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				statementListener.onViewClicked(v, holder.getAdapterPosition());
			}
		});
	}

	@Override
	public int getItemCount() {
		return mItems.size();
	}

	public class StatementViewHolder extends RecyclerView.ViewHolder {
		private WTextView tvStatementName, tvViewStatement;
		private ImageView imCheckItem;

		public StatementViewHolder(View itemView) {
			super(itemView);
			tvStatementName = (WTextView) itemView.findViewById(R.id.tvStatementName);
			tvViewStatement = (WTextView) itemView.findViewById(R.id.tvViewStatement);
			imCheckItem = (ImageView) itemView.findViewById(R.id.imCheckItem);
		}

		public void populateMonth(Statement statement, WTextView view) {
			view.setText(statement.docDesc);
		}

		public void updateCheckStatementUI(Statement statement, final ImageView view) {
			if (statement.selectedByUser()) {
				view.setImageResource(R.drawable.checked_item);
			} else {
				view.setImageResource(R.drawable.uncheck_item);
			}
		}

		public void updateViewStatementUI(Statement statement, WTextView view) {
			if (statement.getStatementView()) {
				view.setVisibility(View.GONE);
			} else {
				view.setVisibility(View.VISIBLE);
			}
		}
	}

	public void add(Statement item) {
		mItems.add(item);
		notifyItemInserted(mItems.size() - 1);
	}

	public ArrayList<Statement> getStatementList() {
		return mItems;
	}

	public void refreshBlockOverlay(int position) {
		notifyItemChanged(position);
	}

	public void updateStatementViewState(boolean value) {
		int index = 0;
		for (Statement statement : mItems) {
			statement.showStatementView(value);
			refreshBlockOverlay(index);
			index++;
		}
	}
}
