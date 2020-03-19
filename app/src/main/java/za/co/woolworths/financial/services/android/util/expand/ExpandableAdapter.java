package za.co.woolworths.financial.services.android.util.expand;

import android.os.Bundle;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ExpandableAdapter<PVH extends ParentViewHolder, CVH extends ChildViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ParentViewHolder.ParentListItemExpandCollapseListener {

    private static final String EXPANDED_STATE_MAP = "ExpandableRecyclerAdapter.ExpandedStateMap";
    private static final int TYPE_PARENT = 0;
    private static final int TYPE_CHILD = 1;

    protected List<Object> mItemList;

    private List<? extends ParentListItem> mParentItemList;
    private ExpandCollapseListener mExpandCollapseListener;
    private List<RecyclerView> mAttachedRecyclerViewPool;

    public interface ExpandCollapseListener {

        void onListItemExpanded(int position);

        void onListItemCollapsed(int position);
    }

    public ExpandableAdapter(List<? extends ParentListItem> parentItemList) {
        super();
        mParentItemList = parentItemList;
        mItemList = ExpandableRecyclerAdapterHelper.generateParentChildItemList(parentItemList);
        mAttachedRecyclerViewPool = new ArrayList<>();
    }

    public void setParentItemList(List<? extends ParentListItem> mParentItemList) {
        this.mParentItemList = mParentItemList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == TYPE_PARENT) {
            PVH pvh = onCreateParentViewHolder(viewGroup, viewType);
            pvh.setParentListItemExpandCollapseListener(this);
            return pvh;
        } else if (viewType == TYPE_CHILD) {
            return onCreateChildViewHolder(viewGroup, viewType);
        } else {
            throw new IllegalStateException("Incorrect ViewType found");
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            PVH parentViewHolder = (PVH) holder;

            if (parentViewHolder.shouldItemViewClickToggleExpansion()) {
                parentViewHolder.setMainItemClickToExpand();
            }

            ParentWrapper parentWrapper = (ParentWrapper) listItem;
            parentViewHolder.setExpanded(parentWrapper.isExpanded());
            onBindParentViewHolder(parentViewHolder, position, parentWrapper.getParentListItem());
        } else if (listItem == null) {
            throw new IllegalStateException("Incorrect ViewHolder found");
        } else {
            onBindChildViewHolder((CVH) holder, position, listItem);
        }
    }

    public abstract PVH onCreateParentViewHolder(ViewGroup parentViewGroup, int viewType);

    public abstract CVH onCreateChildViewHolder(ViewGroup childViewGroup, int viewType);

    public abstract void onBindParentViewHolder(PVH parentViewHolder, int position, ParentListItem parentListItem);

    public abstract void onBindChildViewHolder(CVH childViewHolder, int position, Object childListItem);

    @Override
    public int getItemCount() {
        return mItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object listItem = getListItem(position);
       if (listItem instanceof ParentWrapper) {
            return TYPE_PARENT;
        } else if (listItem == null) {
            throw new IllegalStateException("Null object added");
        } else {
            return TYPE_CHILD;
        }
    }

    public List<? extends ParentListItem> getParentItemList() {
        return mParentItemList;
    }

    @Override
    public void onParentListItemExpanded(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            expandParentListItem((ParentWrapper) listItem, position, true);
        }
    }

    @Override
    public void onParentListItemExpandedChanged(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            expandParentListItem((ParentWrapper) listItem, position, true, true);
        }
    }

    @Override
    public void onParentListItemCollapsed(int position) {
        Object listItem = getListItem(position);
        if (listItem instanceof ParentWrapper) {
            collapseParentListItem((ParentWrapper) listItem, position, true);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.add(recyclerView);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mAttachedRecyclerViewPool.remove(recyclerView);
    }

    public void setExpandCollapseListener(ExpandCollapseListener expandCollapseListener) {
        mExpandCollapseListener = expandCollapseListener;
    }

    public void expandParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object listItem = getListItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (listItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) listItem;
        } else {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    public void expandParent(ParentListItem parentListItem) {
        ParentWrapper parentWrapper = getParentWrapper(parentListItem);
        int parentWrapperIndex = mItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        expandViews(parentWrapper, parentWrapperIndex);
    }

    public void expandParentRange(int startParentIndex, int parentCount) {
        int endParentIndex = startParentIndex + parentCount;
        for (int i = startParentIndex; i < endParentIndex; i++) {
            expandParent(i);
        }
    }

    public void expandAllParents() {
        for (ParentListItem parentListItem : mParentItemList) {
            expandParent(parentListItem);
        }
    }

    public void collapseParent(int parentIndex) {
        int parentWrapperIndex = getParentWrapperIndex(parentIndex);

        Object listItem = getListItem(parentWrapperIndex);
        ParentWrapper parentWrapper;
        if (listItem instanceof ParentWrapper) {
            parentWrapper = (ParentWrapper) listItem;
        } else {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    public void collapseParent(ParentListItem parentListItem) {
        ParentWrapper parentWrapper = getParentWrapper(parentListItem);
        int parentWrapperIndex = mItemList.indexOf(parentWrapper);
        if (parentWrapperIndex == -1) {
            return;
        }

        collapseViews(parentWrapper, parentWrapperIndex);
    }

    public void collapseParentRange(int startParentIndex, int parentCount) {
        int endParentIndex = startParentIndex + parentCount;
        for (int i = startParentIndex; i < endParentIndex; i++) {
            collapseParent(i);
        }
    }

    public void collapseAllParents() {
        for (ParentListItem parentListItem : mParentItemList) {
            collapseParent(parentListItem);
        }
    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putSerializable(EXPANDED_STATE_MAP, generateExpandedStateMap());
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null
                || !savedInstanceState.containsKey(EXPANDED_STATE_MAP)) {
            return;
        }

        HashMap<Integer, Boolean> expandedStateMap = (HashMap<Integer, Boolean>) savedInstanceState.getSerializable(EXPANDED_STATE_MAP);
        if (expandedStateMap == null) {
            return;
        }

        List<Object> parentWrapperList = new ArrayList<>();
        ParentListItem parentListItem;
        ParentWrapper parentWrapper;

        int parentListItemCount = mParentItemList.size();
        for (int i = 0; i < parentListItemCount; i++) {
            parentListItem = mParentItemList.get(i);
            parentWrapper = new ParentWrapper(parentListItem);
            parentWrapperList.add(parentWrapper);

            if (expandedStateMap.containsKey(i)) {
                boolean expanded = expandedStateMap.get(i);
                if (expanded) {
                    parentWrapper.setExpanded(true);

                    int childListItemCount = parentWrapper.getChildItemList().size();
                    for (int j = 0; j < childListItemCount; j++) {
                        parentWrapperList.add(parentWrapper.getChildItemList().get(j));
                    }
                }
            }
        }

        mItemList = parentWrapperList;

        notifyDataSetChanged();
    }

    protected Object getListItem(int position) {
        boolean indexInRange = position >= 0 && position < mItemList.size();
        if (indexInRange) {
            return mItemList.get(position);
        } else {
            return null;
        }
    }

    private void expandViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && !viewHolder.isExpanded()) {
                viewHolder.setExpanded(true);
                viewHolder.onExpansionToggled(false);
            }

            expandParentListItem(parentWrapper, parentIndex, false);
        }
    }

    private void collapseViews(ParentWrapper parentWrapper, int parentIndex) {
        PVH viewHolder;
        for (RecyclerView recyclerView : mAttachedRecyclerViewPool) {
            viewHolder = (PVH) recyclerView.findViewHolderForAdapterPosition(parentIndex);
            if (viewHolder != null && viewHolder.isExpanded()) {
                viewHolder.setExpanded(false);
                viewHolder.onExpansionToggled(true);
            }

            collapseParentListItem(parentWrapper, parentIndex, false);
        }
    }

    private void expandParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean expansionTriggeredByListItemClick) {
        if (!parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(true);

            List<?> childItemList = parentWrapper.getChildItemList();
            if (childItemList != null) {
                int childListItemCount = childItemList.size();
                for (int i = 0; i < childListItemCount; i++) {
                    mItemList.add(parentIndex + i + 1, childItemList.get(i));
                }
                notifyItemRangeInserted(parentIndex + 1, childListItemCount);
            }

            if (expansionTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemExpanded(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    private void expandParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean expansionTriggeredByListItemClick, boolean notifyItemChanged) {
        if (!parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(true);
            notifyItemChanged(parentIndex);
            if (expansionTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemExpanded(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    private void collapseParentListItem(ParentWrapper parentWrapper, int parentIndex, boolean collapseTriggeredByListItemClick) {
        if (parentWrapper.isExpanded()) {
            parentWrapper.setExpanded(false);

            List<?> childItemList = parentWrapper.getChildItemList();
            if (childItemList != null) {
                int childListItemCount = childItemList.size();
                for (int i = childListItemCount - 1; i >= 0; i--) {
                    mItemList.remove(parentIndex + i + 1);
                }
                notifyItemRangeRemoved(parentIndex + 1, childListItemCount);
            }

            if (collapseTriggeredByListItemClick && mExpandCollapseListener != null) {
                int expandedCountBeforePosition = getExpandedItemCount(parentIndex);
                mExpandCollapseListener.onListItemCollapsed(parentIndex - expandedCountBeforePosition);
            }
        }
    }

    private int getExpandedItemCount(int position) {
        if (position == 0) {
            return 0;
        }

        int expandedCount = 0;
        for (int i = 0; i < position; i++) {
            Object listItem = getListItem(i);
            if (!(listItem instanceof ParentWrapper)) {
                expandedCount++;
            }
        }
        return expandedCount;
    }

    public void notifyParentItemInserted(int parentPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);

        int wrapperIndex;
        if (parentPosition < mParentItemList.size() - 1) {
            wrapperIndex = getParentWrapperIndex(parentPosition);
        } else {
            wrapperIndex = mItemList.size();
        }

        int sizeChanged = addParentWrapper(wrapperIndex, parentListItem);
        notifyItemRangeInserted(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the currently reflected {@code itemCount}
     * ParentListItems starting at {@code parentPositionStart} have been newly inserted.
     * The ParentListItems previously located at {@code parentPositionStart} and beyond
     * can now be found starting at position {@code parentPositionStart + itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart Position of the first ParentListItem that was inserted, relative
     *                            to list of ParentListItems only.
     * @param itemCount           Number of items inserted
     * @see #notifyParentItemInserted(int)
     */
    public void notifyParentItemRangeInserted(int parentPositionStart, int itemCount) {
        int initialWrapperIndex;
        if (parentPositionStart < mParentItemList.size() - itemCount) {
            initialWrapperIndex = getParentWrapperIndex(parentPositionStart);
        } else {
            initialWrapperIndex = mItemList.size();
        }

        int sizeChanged = 0;
        int wrapperIndex = initialWrapperIndex;
        int changed;
        int parentPositionEnd = parentPositionStart + itemCount;
        for (int i = parentPositionStart; i < parentPositionEnd; i++) {
            ParentListItem parentListItem = mParentItemList.get(i);
            changed = addParentWrapper(wrapperIndex, parentListItem);
            wrapperIndex += changed;
            sizeChanged += changed;
        }

        notifyItemRangeInserted(initialWrapperIndex, sizeChanged);
    }

    private int addParentWrapper(int wrapperIndex, ParentListItem parentListItem) {
        int sizeChanged = 1;
        ParentWrapper parentWrapper = new ParentWrapper(parentListItem);
        mItemList.add(wrapperIndex, parentWrapper);
        if (parentWrapper.isInitiallyExpanded()) {
            parentWrapper.setExpanded(true);
            List<?> childItemList = parentWrapper.getChildItemList();
            mItemList.addAll(wrapperIndex + sizeChanged, childItemList);
            sizeChanged += childItemList.size();
        }
        return sizeChanged;
    }

    /**
     * Notify any registered observers that the ParentListItem previously located at {@code parentPosition}
     * has been removed from the data set. The ParentListItems previously located at and after
     * {@code parentPosition} may now be found at {@code oldPosition - 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem that has now been removed, relative
     *                       to list of ParentListItems only.
     */
    public void notifyParentItemRemoved(int parentPosition) {
        int wrapperIndex = getParentWrapperIndex(parentPosition);
        int sizeChanged = removeParentWrapper(wrapperIndex);

        notifyItemRangeRemoved(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the {@code itemCount} ParentListItems previously located
     * at {@code parentPositionStart} have been removed from the data set. The ParentListItems
     * previously located at and after {@code parentPositionStart + itemCount} may now be found at
     * {@code oldPosition - itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPositionStart The previous position of the first ParentListItem that was
     *                            removed, relative to list of ParentListItems only.
     * @param itemCount           Number of ParentListItems removed from the data set
     */
    public void notifyParentItemRangeRemoved(int parentPositionStart, int itemCount) {
        int sizeChanged = 0;
        int wrapperIndex = getParentWrapperIndex(parentPositionStart);
        for (int i = 0; i < itemCount; i++) {
            sizeChanged += removeParentWrapper(wrapperIndex);
        }

        notifyItemRangeRemoved(wrapperIndex, sizeChanged);
    }

    private int removeParentWrapper(int parentWrapperIndex) {
        int sizeChanged = 1;
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.remove(parentWrapperIndex);
        if (parentWrapper.isExpanded()) {
            int childListSize = parentWrapper.getChildItemList().size();
            for (int i = 0; i < childListSize; i++) {
                mItemList.remove(parentWrapperIndex);
                sizeChanged++;
            }
        }
        return sizeChanged;
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has changed.
     * This will also trigger an item changed for children of the ParentList specified.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at {@code parentPosition} is out of date and should be updated.
     * The ParentListItem at {@code parentPosition} retains the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPosition Position of the item that has changed
     */
    public void notifyParentItemChanged(int parentPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int wrapperIndex = getParentWrapperIndex(parentPosition);
        int sizeChanged = changeParentWrapper(wrapperIndex, parentListItem);

        notifyItemRangeChanged(wrapperIndex, sizeChanged);
    }

    /**
     * Notify any registered observers that the {@code itemCount} ParentListItems starting
     * at {@code parentPositionStart} have changed. This will also trigger an item changed
     * for children of the ParentList specified.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data in the given position range is out of date and should be updated.
     * The ParentListItems in the given range retain the same identity. This means
     * the number of children must stay the same.
     *
     * @param parentPositionStart Position of the item that has changed
     * @param itemCount           Number of ParentListItems changed in the dataset
     */
    public void notifyParentItemRangeChanged(int parentPositionStart, int itemCount) {
        int initialWrapperIndex = getParentWrapperIndex(parentPositionStart);

        int wrapperIndex = initialWrapperIndex;
        int sizeChanged = 0;
        int changed;
        ParentListItem parentListItem;
        for (int j = 0; j < itemCount; j++) {
            parentListItem = mParentItemList.get(parentPositionStart);
            changed = changeParentWrapper(wrapperIndex, parentListItem);
            sizeChanged += changed;
            wrapperIndex += changed;
            parentPositionStart++;
        }
        notifyItemRangeChanged(initialWrapperIndex, sizeChanged);
    }

    private int changeParentWrapper(int wrapperIndex, ParentListItem parentListItem) {
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(wrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        int sizeChanged = 1;
        if (parentWrapper.isExpanded()) {
            List<?> childItems = parentWrapper.getChildItemList();
            int childListSize = childItems.size();
            Object child;
            for (int i = 0; i < childListSize; i++) {
                child = childItems.get(i);
                mItemList.set(wrapperIndex + i + 1, child);
                sizeChanged++;
            }
        }

        return sizeChanged;

    }

    /**
     * Notify any registered observers that the ParentListItem and it's child list items reflected at
     * {@code fromParentPosition} has been moved to {@code toParentPosition}.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     *
     * @param fromParentPosition Previous position of the ParentListItem, relative to list of
     *                           ParentListItems only.
     * @param toParentPosition   New position of the ParentListItem, relative to list of
     *                           ParentListItems only.
     */
    public void notifyParentItemMoved(int fromParentPosition, int toParentPosition) {

        int fromWrapperIndex = getParentWrapperIndex(fromParentPosition);
        ParentWrapper fromParentWrapper = (ParentWrapper) mItemList.get(fromWrapperIndex);

        // If the parent is collapsed we can take advantage of notifyItemMoved otherwise
        // we are forced to do a "manual" move by removing and then adding the parent + children
        // (no notifyItemRangeMovedAvailable)
        boolean isCollapsed = !fromParentWrapper.isExpanded();
        boolean isExpandedNoChildren = !isCollapsed && (fromParentWrapper.getChildItemList().size() == 0);
        if (isCollapsed || isExpandedNoChildren) {
            int toWrapperIndex = getParentWrapperIndex(toParentPosition);
            ParentWrapper toParentWrapper = (ParentWrapper) mItemList.get(toWrapperIndex);
            mItemList.remove(fromWrapperIndex);
            int childOffset = 0;
            if (toParentWrapper.isExpanded()) {
                childOffset = toParentWrapper.getChildItemList().size();
            }
            mItemList.add(toWrapperIndex + childOffset, fromParentWrapper);

            notifyItemMoved(fromWrapperIndex, toWrapperIndex + childOffset);
        } else {
            // Remove the parent and children
            int sizeChanged = 0;
            int childListSize = fromParentWrapper.getChildItemList().size();
            for (int i = 0; i < childListSize + 1; i++) {
                mItemList.remove(fromWrapperIndex);
                sizeChanged++;
            }
            notifyItemRangeRemoved(fromWrapperIndex, sizeChanged);


            // Add the parent and children at new position
            int toWrapperIndex = getParentWrapperIndex(toParentPosition);
            int childOffset = 0;
            if (toWrapperIndex != -1) {
                ParentWrapper toParentWrapper = (ParentWrapper) mItemList.get(toWrapperIndex);
                if (toParentWrapper.isExpanded()) {
                    childOffset = toParentWrapper.getChildItemList().size();
                }
            } else {
                toWrapperIndex = mItemList.size();
            }
            mItemList.add(toWrapperIndex + childOffset, fromParentWrapper);
            List<?> childItemList = fromParentWrapper.getChildItemList();
            sizeChanged = childItemList.size() + 1;
            mItemList.addAll(toWrapperIndex + childOffset + 1, childItemList);
            notifyItemRangeInserted(toWrapperIndex + childOffset, sizeChanged);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has a child list item that has been newly inserted at {@code childPosition}.
     * The child list item previously at {@code childPosition} is now at
     * position {@code childPosition + 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has been added a child, relative
     *                       to list of ParentListItems only.
     * @param childPosition  Position of the child object that has been inserted, relative to children
     *                       of the ParentListItem specified by {@code parentPosition} only.
     */
    public void notifyChildItemInserted(int parentPosition, int childPosition) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            ParentListItem parentListItem = mParentItemList.get(parentPosition);
            Object child = parentListItem.getChildItemList().get(childPosition);
            mItemList.add(parentWrapperIndex + childPosition + 1, child);
            notifyItemInserted(parentWrapperIndex + childPosition + 1);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem reflected at {@code parentPosition}
     * has {@code itemCount} child list items that have been newly inserted at {@code childPositionStart}.
     * The child list item previously at {@code childPositionStart} and beyond are now at
     * position {@code childPositionStart + itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.
     *
     * @param parentPosition     Position of the ParentListItem which has been added a child, relative
     *                           to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been inserted,
     *                           relative to children of the ParentListItem specified by
     *                           {@code parentPosition} only.
     * @param itemCount          number of children inserted
     */
    public void notifyChildItemRangeInserted(int parentPosition, int childPositionStart, int itemCount) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            ParentListItem parentListItem = mParentItemList.get(parentPosition);
            List<?> childList = parentListItem.getChildItemList();
            Object child;
            for (int i = 0; i < itemCount; i++) {
                child = childList.get(childPositionStart + i);
                mItemList.add(parentWrapperIndex + childPositionStart + i + 1, child);
            }
            notifyItemRangeInserted(parentWrapperIndex + childPositionStart + 1, itemCount);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at {@code parentPosition}
     * has a child list item that has been removed from the data set, previously located at {@code childPosition}.
     * The child list item previously located at and after {@code childPosition} may
     * now be found at {@code childPosition - 1}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition Position of the ParentListItem which has a child removed from, relative
     *                       to list of ParentListItems only.
     * @param childPosition  Position of the child object that has been removed, relative to children
     *                       of the ParentListItem specified by {@code parentPosition} only.
     */
    public void notifyChildItemRemoved(int parentPosition, int childPosition) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            mItemList.remove(parentWrapperIndex + childPosition + 1);
            notifyItemRemoved(parentWrapperIndex + childPosition + 1);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem located at {@code parentPosition}
     * has {@code itemCount} child list items that have been removed from the data set, previously
     * located at {@code childPositionStart} onwards. The child list item previously located at and
     * after {@code childPositionStart} may now be found at {@code childPositionStart - itemCount}.
     * <p>
     * This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their positions
     * may be altered.
     *
     * @param parentPosition     Position of the ParentListItem which has a child removed from, relative
     *                           to list of ParentListItems only.
     * @param childPositionStart Position of the first child object that has been removed, relative
     *                           to children of the ParentListItem specified by
     *                           {@code parentPosition} only.
     * @param itemCount          number of children removed
     */
    public void notifyChildItemRangeRemoved(int parentPosition, int childPositionStart, int itemCount) {
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);

        if (parentWrapper.isExpanded()) {
            for (int i = 0; i < itemCount; i++) {
                mItemList.remove(parentWrapperIndex + childPositionStart + 1);
            }
            notifyItemRangeRemoved(parentWrapperIndex + childPositionStart + 1, itemCount);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * a child located at {@code childPosition} that has changed.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * reflection of the data at {@code childPosition} is out of date and should be updated.
     * The ParentListItem at {@code childPosition} retains the same identity.
     *
     * @param parentPosition Position of the ParentListItem who has a child that has changed
     * @param childPosition  Position of the child that has changed
     */
    public void notifyChildItemChanged(int parentPosition, int childPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            int listChildPosition = parentWrapperIndex + childPosition + 1;
            Object child = parentWrapper.getChildItemList().get(childPosition);
            mItemList.set(listChildPosition, child);
            notifyItemChanged(listChildPosition);
        }
    }

    /**
     * Notify any registered observers that the ParentListItem at {@code parentPosition} has
     * {@code itemCount} child Objects starting at {@code childPositionStart} that have changed.
     * <p>
     * This is an item change event, not a structural change event. It indicates that any
     * The ParentListItem at {@code childPositionStart} retains the same identity.
     * reflection of the set of {@code itemCount} child objects starting at {@code childPositionStart}
     * are out of date and should be updated.
     *
     * @param parentPosition     Position of the ParentListItem who has a child that has changed
     * @param childPositionStart Position of the first child object that has changed
     * @param itemCount          number of child objects changed
     */
    public void notifyChildItemRangeChanged(int parentPosition, int childPositionStart, int itemCount) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            int listChildPosition = parentWrapperIndex + childPositionStart + 1;
            for (int i = 0; i < itemCount; i++) {
                Object child = parentWrapper.getChildItemList().get(childPositionStart + i);
                mItemList.set(listChildPosition + i, child);

            }
            notifyItemRangeChanged(listChildPosition, itemCount);
        }
    }

    /**
     * Notify any registered observers that the child list item contained within the ParentListItem
     * at {@code parentPosition} has moved from {@code fromChildPosition} to {@code toChildPosition}.
     * <p>
     * <p>This is a structural change event. Representations of other existing items in the
     * data set are still considered up to date and will not be rebound, though their
     * positions may be altered.</p>
     *
     * @param parentPosition    Position of the ParentListItem who has a child that has moved
     * @param fromChildPosition Previous position of the child list item
     * @param toChildPosition   New position of the child list item
     */
    public void notifyChildItemMoved(int parentPosition, int fromChildPosition, int toChildPosition) {
        ParentListItem parentListItem = mParentItemList.get(parentPosition);
        int parentWrapperIndex = getParentWrapperIndex(parentPosition);
        ParentWrapper parentWrapper = (ParentWrapper) mItemList.get(parentWrapperIndex);
        parentWrapper.setParentListItem(parentListItem);
        if (parentWrapper.isExpanded()) {
            Object fromChild = mItemList.remove(parentWrapperIndex + 1 + fromChildPosition);
            mItemList.add(parentWrapperIndex + 1 + toChildPosition, fromChild);
            notifyItemMoved(parentWrapperIndex + 1 + fromChildPosition, parentWrapperIndex + 1 + toChildPosition);
        }
    }


    // endregion

    /**
     * Generates a HashMap used to store expanded state for items in the list
     * on configuration change or whenever onResume is called.
     *
     * @return A HashMap containing the expanded state of all parent list items
     */
    private HashMap<Integer, Boolean> generateExpandedStateMap() {
        HashMap<Integer, Boolean> parentListItemHashMap = new HashMap<>();
        int childCount = 0;

        Object listItem;
        ParentWrapper parentWrapper;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) != null) {
                listItem = getListItem(i);
                if (listItem instanceof ParentWrapper) {
                    parentWrapper = (ParentWrapper) listItem;
                    parentListItemHashMap.put(i - childCount, parentWrapper.isExpanded());
                } else {
                    childCount++;
                }
            }
        }

        return parentListItemHashMap;
    }

    /**
     * Gets the index of a ParentWrapper within the helper item list based on
     * the index of the ParentWrapper.
     *
     * @param parentIndex The index of the parent in the list of parent items
     * @return The index of the parent in the list of all views in the adapter
     */
    private int getParentWrapperIndex(int parentIndex) {
        int parentCount = 0;
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            if (mItemList.get(i) instanceof ParentWrapper) {
                parentCount++;

                if (parentCount > parentIndex) {
                    return i;
                }
            }
        }

        return -1;
    }

    /**
     * Gets the ParentWrapper for a specified ParentListItem from the list of
     * parents.
     *
     * @param parentListItem A ParentListItem in the list of parents
     * @return If the parent exists on the list, returns its ParentWrapper.
     * Otherwise, returns null.
     */
    private ParentWrapper getParentWrapper(ParentListItem parentListItem) {
        int listItemCount = mItemList.size();
        for (int i = 0; i < listItemCount; i++) {
            Object listItem = mItemList.get(i);
            if (listItem instanceof ParentWrapper) {
                if (((ParentWrapper) listItem).getParentListItem().equals(parentListItem)) {
                    return (ParentWrapper) listItem;
                }
            }
        }

        return null;
    }
}