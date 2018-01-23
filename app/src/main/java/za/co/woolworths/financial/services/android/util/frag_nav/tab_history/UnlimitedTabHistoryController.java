package za.co.woolworths.financial.services.android.util.frag_nav.tab_history;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Stack;

import za.co.woolworths.financial.services.android.util.frag_nav.FragNavPopController;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavSwitchController;

public class UnlimitedTabHistoryController extends CollectionFragNavTabHistoryController {
    private Stack<Integer> tabHistory = new Stack<>();

    public UnlimitedTabHistoryController(FragNavPopController fragNavPopController,
                                         FragNavSwitchController fragNavSwitchController) {
        super(fragNavPopController, fragNavSwitchController);
    }

    @Override
    int getCollectionSize() {
        return tabHistory.size();
    }

    @Override
    int getAndRemoveIndex() {
        tabHistory.pop();
        return tabHistory.pop();
    }

    @Override
    public void switchTab(int index) {
        tabHistory.push(index);
    }

    @NonNull
    @Override
	ArrayList<Integer> getHistory() {
        return new ArrayList<>(tabHistory);
    }

    @Override
    void setHistory(@NonNull ArrayList<Integer> history) {
        tabHistory.clear();
        tabHistory.addAll(history);
    }
}
