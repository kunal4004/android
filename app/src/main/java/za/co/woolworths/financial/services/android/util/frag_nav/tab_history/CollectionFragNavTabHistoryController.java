package za.co.woolworths.financial.services.android.util.frag_nav.tab_history;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

import za.co.woolworths.financial.services.android.util.frag_nav.FragNavPopController;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavSwitchController;
import za.co.woolworths.financial.services.android.util.frag_nav.FragNavTransactionOptions;

abstract class CollectionFragNavTabHistoryController extends BaseFragNavTabHistoryController {
    private static final String EXTRA_STACK_HISTORY = "EXTRA_STACK_HISTORY";

    private FragNavSwitchController fragNavSwitchController;

    CollectionFragNavTabHistoryController(FragNavPopController fragNavPopController,
                                          FragNavSwitchController fragNavSwitchController) {
        super(fragNavPopController);
        this.fragNavSwitchController = fragNavSwitchController;
    }

    @Override
    public boolean popFragments(int popDepth,
                                FragNavTransactionOptions transactionOptions) throws UnsupportedOperationException {
        boolean changed = false;
        boolean switched;
        do {
            switched = false;
            int count = fragNavPopController.tryPopFragments(popDepth, transactionOptions);
            if (count > 0) {
                changed = true;
                switched = true;
                popDepth -= count;
            } else if (getCollectionSize() > 1) {
                fragNavSwitchController.switchTab(getAndRemoveIndex(), transactionOptions);
                popDepth--;
                changed = true;
                switched = true;
            }
        } while (popDepth > 0 && switched);
        return changed;
    }

    abstract int getCollectionSize();

    abstract int getAndRemoveIndex();

    @NonNull
    abstract ArrayList<Integer> getHistory();

    abstract void setHistory(@NonNull ArrayList<Integer> history);

    @Override
    public void restoreFromBundle(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        ArrayList<Integer> arrayList = savedInstanceState.getIntegerArrayList(EXTRA_STACK_HISTORY);
        if (arrayList != null) {
            setHistory(arrayList);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        ArrayList<Integer> history = getHistory();
        if (history.isEmpty()) {
            return;
        }
        outState.putIntegerArrayList(EXTRA_STACK_HISTORY, history);
    }
}
