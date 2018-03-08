package za.co.woolworths.financial.services.android.ui.views;

import android.graphics.Canvas;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import za.co.woolworths.financial.services.android.ui.adapters.CartProductAdapter;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
	private RecyclerItemTouchHelperListener listener;

	public RecyclerItemTouchHelper(int dragDirs, int swipeDirs, RecyclerItemTouchHelperListener listener) {
		super(dragDirs, swipeDirs);
		this.listener = listener;
	}

	@Override
	public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
		return true;
	}

	@Override
	public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
		if (viewHolder != null) {
			final View foregroundView = ((CartProductAdapter.CartItemViewHolder) viewHolder).viewForeground;

			getDefaultUIUtil().onSelected(foregroundView);
		}
	}

	@Override
	public void onChildDrawOver(Canvas c, RecyclerView recyclerView,
								RecyclerView.ViewHolder viewHolder, float dX, float dY,
								int actionState, boolean isCurrentlyActive) {
		final View foregroundView = ((CartProductAdapter.CartItemViewHolder) viewHolder).viewForeground;
		getDefaultUIUtil().onDrawOver(c, recyclerView, foregroundView, dX, dY,
				actionState, isCurrentlyActive);
	}

	@Override
	public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
		final View foregroundView = ((CartProductAdapter.CartItemViewHolder) viewHolder).viewForeground;
		getDefaultUIUtil().clearView(foregroundView);
	}

	@Override
	public void onChildDraw(Canvas c, RecyclerView recyclerView,
							RecyclerView.ViewHolder viewHolder, float dX, float dY,
							int actionState, boolean isCurrentlyActive) {
		final View foregroundView = ((CartProductAdapter.CartItemViewHolder) viewHolder).viewForeground;
		getDefaultUIUtil().onDraw(c, recyclerView, foregroundView, dX / 2, dY,
				actionState, isCurrentlyActive);
	}

	@Override
	public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
		listener.onSwiped(viewHolder, direction, viewHolder.getAdapterPosition());
	}

	@Override
	public int convertToAbsoluteDirection(int flags, int layoutDirection) {
		return super.convertToAbsoluteDirection(flags, layoutDirection);
	}

	public interface RecyclerItemTouchHelperListener {
		void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position);
	}
}
