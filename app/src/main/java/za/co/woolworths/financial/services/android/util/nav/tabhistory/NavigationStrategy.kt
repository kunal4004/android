package za.co.woolworths.financial.services.android.util.nav.tabhistory

import za.co.woolworths.financial.services.android.util.nav.FragNavSwitchController

sealed class NavigationStrategy

class CurrentTabStrategy : NavigationStrategy()

class UnlimitedTabHistoryStrategy(val fragNavSwitchController: FragNavSwitchController) : NavigationStrategy()

class UniqueTabHistoryStrategy(val fragNavSwitchController: FragNavSwitchController) : NavigationStrategy()