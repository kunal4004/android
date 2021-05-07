package za.co.woolworths.financial.services.android.startup.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository

/**
 * Created by Kunal Uttarwar on 23/2/21.
 */

/**
 * This class is used to connect to view model class from view.
 */
class ViewModelFactory(private val startUpRepository: StartUpRepository, private val startupApiHelper: StartupApiHelper) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StartupViewModel::class.java)) {
            return StartupViewModel(startUpRepository, startupApiHelper) as T
        }
        throw IllegalArgumentException("Unknown class name")
    }
}