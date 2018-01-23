package za.co.woolworths.financial.services.android.util.frag_nav.tab_history;


import za.co.woolworths.financial.services.android.util.frag_nav.FragNavPopController;

abstract class BaseFragNavTabHistoryController implements FragNavTabHistoryController {
    FragNavPopController fragNavPopController;

    BaseFragNavTabHistoryController(FragNavPopController fragNavPopController) {
        this.fragNavPopController = fragNavPopController;
    }
}
