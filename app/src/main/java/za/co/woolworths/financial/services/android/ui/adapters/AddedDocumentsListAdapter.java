package za.co.woolworths.financial.services.android.ui.adapters;

import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Document;
import za.co.woolworths.financial.services.android.ui.views.WTextView;

/**
 * Created by W7099877 on 2017/10/19.
 */

public class AddedDocumentsListAdapter extends RecyclerView.Adapter<AddedDocumentsListAdapter.MyViewHolder> {

	public interface ItemRemoved {
		void onItemRemoved(View view, int position);
	}

	private ItemRemoved itemRemoved;
	private List<Document> documentList;

	public AddedDocumentsListAdapter(ItemRemoved itemRemoved,List<Document> documentList) {
		this.itemRemoved = itemRemoved;
		this.documentList=documentList;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private ImageView  imgRemoveDocument;
		private WTextView tvDocumentName;
		private ProgressBar fileUploadProgressBar;

		public MyViewHolder(View view) {
			super(view);
			tvDocumentName = (WTextView) view.findViewById(R.id.tvDocumentName);
			imgRemoveDocument = (ImageView) view.findViewById(R.id.imgRemoveDoc);
			fileUploadProgressBar=(ProgressBar)view.findViewById(R.id.fileUploadProgressBar);
		}

		public void bindUI(int position, final MyViewHolder holder) {
			onRemoevButtonClick(holder);
			setText(holder, documentList.get(position).getName());
			int progress=documentList.get(position).getProgress();
			if(progress>0)
			{
				holder.fileUploadProgressBar.setVisibility(View.VISIBLE);
				holder.fileUploadProgressBar.setProgress(progress);
			}else {
				holder.fileUploadProgressBar.setVisibility(View.GONE);
			}

		}


		private void setText(MyViewHolder holder, String submitType) {
			holder.tvDocumentName.setText(submitType);
		}

		private void onRemoevButtonClick(final MyViewHolder holder) {
			holder.imgRemoveDocument.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int position = holder.getAdapterPosition();
					itemRemoved.onItemRemoved(v, position);
					notifyDataSetChanged();
				}
			});
		}

		private void rowBackground(final MyViewHolder holder, int id) {
			holder.itemView.setBackground(ContextCompat.getDrawable(holder.itemView.getContext(), id));
		}

	}

	@Override
	public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new MyViewHolder(LayoutInflater.from(parent.getContext())
				.inflate(R.layout.added_document_list_item, parent, false));
	}

	@Override
	public void onBindViewHolder(MyViewHolder holder, int position) {
		holder.bindUI(position, holder);
	}


	@Override
	public int getItemCount() {
		return documentList.size();
	}

}
