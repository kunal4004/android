package za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.controller

import com.google.gson.Gson
import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.fetchFromLocalDatabase
import za.co.woolworths.financial.services.android.ui.wfs.my_accounts_landing.extensions.saveToLocalDatabase
import za.co.woolworths.financial.services.android.ui.wfs.shoptimiser.dto.ShopOptimiserSQLiteModel
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import javax.inject.Inject


interface IManageShopOptimiserSQLite {
    fun isDefaultPdpDisplayed() : Boolean
    fun saveShopOptimiserTimestampInSQLiteModel()
    fun saveDefaultPdpDisplayedInSQLiteModel(isDisplayed: Boolean)
    fun retrieveShopOptimiserSQLiteModel(): ShopOptimiserSQLiteModel
    fun isAccountResponseCachedWithin3Hours(): Boolean
}

class ManageShopOptimiserSQLiteImpl @Inject constructor() : IManageShopOptimiserSQLite {

    private var shopOptimiserSQLiteModel = retrieveShopOptimiserSQLiteModel()

    /**
     * Saves a ShopOptimiserSQLiteModel to the local database.
     * @param model The ShopOptimiserSQLiteModel to be saved.
     */
    private fun saveToLocalDb(model: ShopOptimiserSQLiteModel?) {
        saveToLocalDatabase(SessionDao.KEY.SHOP_OPTIMISER_SQLITE_MODEL, Gson().toJson(model))
    }

    /**
     * Retrieves a ShopOptimiserSQLiteModel from the local database.
     * @return The retrieved ShopOptimiserSQLiteModel, or a new instance if not found.
     */
    override fun retrieveShopOptimiserSQLiteModel(): ShopOptimiserSQLiteModel {
        val serializedModel : String? = fetchFromLocalDatabase(SessionDao.KEY.SHOP_OPTIMISER_SQLITE_MODEL)
        return serializedModel?.let { Gson().fromJson(it, ShopOptimiserSQLiteModel::class.java) }  ?: ShopOptimiserSQLiteModel()
    }

    /**
     * Updates the timestamp in the ShopOptimiserSQLiteModel and saves it to the local database.
     * The timestamp represents the last update time for the ShopOptimiser data.
     */
    override fun saveShopOptimiserTimestampInSQLiteModel() {
        // Get the current timestamp in ISO_INSTANT format
        val currentTimestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        // Update the accountCacheTimestamp in the ShopOptimiserSQLiteModel
        shopOptimiserSQLiteModel = shopOptimiserSQLiteModel.copy(accountCacheTimestamp = currentTimestamp)

        // Save the updated model to the local database
        saveToLocalDb(shopOptimiserSQLiteModel)
    }

    /**
     * Updates and saves the 'isDefaultPdpDisplayed' flag in the ShopOptimiserSQLiteModel.
     * This flag indicates whether the default PDP (Product Display Page) is displayed.
     * @param isDisplayed Boolean flag indicating whether the default PDP is displayed or not.
     */
    override fun saveDefaultPdpDisplayedInSQLiteModel(isDisplayed: Boolean) {
        // Update the 'isDefaultPdpDisplayed' flag in the ShopOptimiserSQLiteModel
        shopOptimiserSQLiteModel = shopOptimiserSQLiteModel.copy(isDefaultPdpDisplayed = isDisplayed)

        // Save the updated model to the local database
        saveToLocalDb(shopOptimiserSQLiteModel)
    }

    /**
     * Retrieves and returns the state of the 'isDefaultPdpDisplayed' flag
     * from the ShopOptimiserSQLiteModel.
     * @return Boolean indicating whether the default PDP (Product Display Page) is displayed or not.
     */
    override fun isDefaultPdpDisplayed(): Boolean {
        // Retrieve the ShopOptimiserSQLiteModel and access the 'isDefaultPdpDisplayed' flag
        return retrieveShopOptimiserSQLiteModel().isDefaultPdpDisplayed
    }


    /**
     * Checks if the account response data is cached and the cache is not older than 3 hours.
     * @return Boolean indicating whether the cached data is within the 3-hour window.
     */
    override fun isAccountResponseCachedWithin3Hours(): Boolean {
        // Get the current timestamp
        val currentInstant = Instant.now()

        // Retrieve the timestamp of the cached data from the ShopOptimiserSQLiteModel
        val timestamp = retrieveShopOptimiserSQLiteModel().accountCacheTimestamp

        // Define a formatter for ISO_INSTANT timestamps
        val formatter = DateTimeFormatter.ISO_INSTANT

        return if (timestamp != null) {
            // Parse the stored timestamp
            val parsedInstant = Instant.from(formatter.parse(timestamp))

            // Calculate the time difference between the current time and the stored timestamp
            val duration = Duration.between(parsedInstant, currentInstant)

            // Check if the time difference is less than 3 hours (3 * 60 minutes)
            duration.toMinutes() < 3 * 60
        } else {
            // If no timestamp is available, consider it as not cached (return true)
            true
        }
    }
}