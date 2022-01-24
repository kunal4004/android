package za.co.woolworths.financial.services.android.models.repository

import za.co.woolworths.financial.services.android.models.dao.SessionDao
import za.co.woolworths.financial.services.android.models.dto.linkdevice.UserDevice
import za.co.woolworths.financial.services.android.util.db.DatabaseManager
import java.util.ArrayList

class AppStateRepository: DatabaseManager() {
    companion object {
        private val KEY_LINKED_DEVICE_LIST_DB = SessionDao.KEY.LINKED_DEVICE_LIST
    }

    fun saveLinkedDevices(devices: ArrayList<UserDevice>?) =
        saveToDB(KEY_LINKED_DEVICE_LIST_DB, devices)

    fun getLinkedDevices() =
        getDataFromDB(KEY_LINKED_DEVICE_LIST_DB, Array<UserDevice>::class.java)
}