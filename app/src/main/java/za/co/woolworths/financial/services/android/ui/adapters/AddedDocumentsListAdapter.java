package za.co.woolworths.financial.services.android.ui.adapters;

import android.animation.ObjectAnimator;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.awfs.coordination.R;

import java.util.List;

import za.co.woolworths.financial.services.android.models.dto.Document;
import za.co.woolworths.financial.services.android.ui.views.WTextView;
import za.co.woolworths.financial.services.android.util.Utils;

/**
 * Created by W7099877 on 2017/10/19.
 */

public class AddedDocumentsListAdapter extends RecyclerView.Adapter<AddedDocumentsListAdapter.MyViewHolder> {

	public interface ItemRemoved {
		void onItemRemoved(View view, int position);
	}

	private ItemRemoved itemRemoved;
	private List<Document> documentList;

	public AddedDocumentsListAdapter(ItemRemoved itemRemoved, List<Document> documentList) {
		this.itemRemoved = itemRemoved;
		this.documentList = documentList;
	}

	public class MyViewHolder extends RecyclerView.ViewHolder {
		private ImageView imgRemoveDocument;
		private WTextView tvDocumentName;
		private ProgressBar fileUploadProgressBar;
		private WTextView tvFileSizeError;
		private ImageView imgDocument;

		public MyViewHolder(View view) {
			super(view);
			tvDocumentName = (WTextView) view.findViewById(R.id.tvDocumentName);
			imgRemoveDocument = (ImageView) view.findViewById(R.id.imgRemoveDoc);
			fileUploadProgressBar = (ProgressBar) view.findViewById(R.id.fileUploadProgressBar);
			tvFileSizeError = (WTextView) view.findViewById(R.id.fileSizeError);
			imgDocument = (ImageView) view.findViewById(R.id.imgDocument);
		}

		public void bindUI(int position, final MyViewHolder holder) {
			Document document = documentList.get(position);
			onRemoevButtonClick(holder);
			if (document != null) {
				String name = document.getName();
				if (!TextUtils.isEmpty(name)) {
					setText(holder, name);
				}
			}

			showUploadProgress(document, holder, position);

			if (document.getSize() > Utils.POI_UPLOAD_FILE_SIZE_MAX) {
				tvFileSizeError.setVisibility(View.VISIBLE);
				tvDocumentName.setAlpha(.3f);
				imgRemoveDocument.setEnabled(false);
				imgRemoveDocument.setAlpha(0.3f);
				imgDocument.setAlpha(0.3f);
			} else {
				tvFileSizeError.setVisibility(View.GONE);
				tvDocumentName.setAlpha(1f);
				imgRemoveDocument.setEnabled(true);
				imgRemoveDocument.setAlpha(1f);
				imgDocument.setAlpha(1f);
			}
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

	public void showUploadProgress(Document document, MyViewHolder holder, int position) {
		if (document.progressIsDisplayed()) {
			showView(holder.fileUploadProgressBar);
			int progress = document.getProgress();
			if (progress > 0) {
				setProgressAnimate(holder.fileUploadProgressBar, progress);
				if (progress == holder.fileUploadProgressBar.getMax()) {
					setImageResource(holder.imgRemoveDocument, R.drawable.cli_step_indicator_active);
					disableView(holder.imgRemoveDocument, false);
				}
			}
		} else {
			hideView(holder.fileUploadProgressBar);
		}
	}

	private void setProgressAnimate(ProgressBar pb, int progressTo) {
		ObjectAnimator animation = ObjectAnimator.ofInt(pb, "progress", pb.getProgress(), progressTo * 100);
		//animation.setDuration(1000);
		animation.setInterpolator(new DecelerateInterpolator());
		animation.start();
	}

	private void hideView(View v) {
		v.setVisibility(View.GONE);
	}

	private void showView(View v) {
		v.setVisibility(View.VISIBLE);
	}

	private void setImageResource(ImageView image, int id) {
		image.setImageResource(id);
	}

	private void disableView(View v, boolean enabled) {
		v.setEnabled(enabled);
	}
}
