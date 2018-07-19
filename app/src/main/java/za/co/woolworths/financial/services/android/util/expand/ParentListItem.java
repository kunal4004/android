package za.co.woolworths.financial.services.android.util.expand;

import java.util.List;

public interface ParentListItem {

	List<?> getChildItemList();

	boolean isInitiallyExpanded();
}