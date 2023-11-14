package za.co.woolworths.financial.services.android.ui.fragments.shop.domain

import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.util.wenum.Delivery

interface CotextualTooltipShowcase {

    fun toolTipToDisplay(
        delivery: Delivery?, isNewSession: Boolean, isUserAuthenticated: Boolean
    ): TooltipShown?

    fun markTooltipShown(
        delivery: Delivery, tooltipShown: TooltipShown, isUserAuthenticated: Boolean
    )

}

class ContextualTooltipShowcaseManager : CotextualTooltipShowcase {

    override fun toolTipToDisplay(
        delivery: Delivery?, isNewSession: Boolean, isUserAuthenticated: Boolean
    ): TooltipShown? {
        if (delivery == null) {
            return null
        }
        val data = AppInstanceObject.get().featureWalkThrough.tooltipData
        val userType = getCurrentUserType(data, isUserAuthenticated)
        return if (userType == UserType.NEW) {
            getTooltipToShow(data, delivery)
        } else {
            val newUserType = if (isUserAuthenticated) {
                UserType.EXISTING
            } else {
                UserType.NEW
            }
            var isPreviouslyNewUser = false
            if (newUserType == UserType.EXISTING && data?.userType == UserType.NEW) {
                isPreviouslyNewUser = true
            }
            getExistingUserTooltipFlow(
                data?.existingUserStatus, isNewSession, isPreviouslyNewUser, isUserAuthenticated
            )
        }
    }

    override fun markTooltipShown(
        delivery: Delivery, tooltipShown: TooltipShown, isUserAuthenticated: Boolean
    ) {
        val appInstanceObject = AppInstanceObject.get()
        val data = appInstanceObject.featureWalkThrough.tooltipData

        when (val userType = getCurrentUserType(data, isUserAuthenticated)) {
            UserType.NEW -> {
                saveNewUserData(data, userType, delivery, tooltipShown, appInstanceObject)
            }
            UserType.EXISTING -> {
                saveExistingUserData(data, userType, tooltipShown, appInstanceObject)
            }
        }
    }

    private fun getExistingUserTooltipFlow(
        currentStatus: TooltipShown?,
        isNewSession: Boolean,
        previouslyNewUser: Boolean,
        isUserAuthenticated: Boolean
    ): TooltipShown {
        return if (previouslyNewUser) {
            TooltipShown.FULFILMENT_SECOND
        } else if (currentStatus == null) {
            TooltipShown.FULFILMENT
        } else if (currentStatus == TooltipShown.FULFILMENT) {
            TooltipShown.LOCATION
        } else if (currentStatus == TooltipShown.LOCATION) {
            if (isNewSession && isUserAuthenticated) {
                TooltipShown.FULFILMENT_SECOND
            } else {
                TooltipShown.COMPLETED
            }
        } else {
            TooltipShown.COMPLETED
        }
    }

    private fun getTooltipToShow(
        data: TooltipData?,
        delivery: Delivery,
    ) = if (data?.map?.contains(delivery) == true) {
        nextToolTipToDisplay(data.map?.get(delivery))
    } else {
        TooltipShown.FULFILMENT
    }

    private fun nextToolTipToDisplay(tooltipShown: TooltipShown?): TooltipShown {
        return when (tooltipShown) {
            TooltipShown.FULFILMENT -> {
                TooltipShown.LOCATION
            }

            TooltipShown.LOCATION -> {
                TooltipShown.COMPLETED
            }

            TooltipShown.COMPLETED -> {
                TooltipShown.COMPLETED
            }

            else -> {
                TooltipShown.FULFILMENT
            }
        }
    }

    private fun saveNewUserData(
        data: TooltipData?,
        userType: UserType,
        delivery: Delivery,
        tooltipShown: TooltipShown,
        appInstanceObject: AppInstanceObject
    ) {
        if (userType != UserType.NEW) {
            return
        }
        if (data != null) {
            data.userType = userType
            val map = data.map
            if (map != null) {
                map[delivery] = tooltipShown
            } else {
                val newMap = mutableMapOf<Delivery, TooltipShown?>()
                newMap[delivery] = tooltipShown
                data.map = newMap
            }
        } else {
            val newMap = mutableMapOf<Delivery, TooltipShown?>()
            newMap[delivery] = tooltipShown
            val newData = TooltipData(
                map = newMap, userType = userType, existingUserStatus = null
            )
            appInstanceObject.featureWalkThrough.tooltipData = newData
        }
        appInstanceObject.save()
    }

    private fun saveExistingUserData(
        data: TooltipData?,
        userType: UserType,
        tooltipShown: TooltipShown,
        appInstanceObject: AppInstanceObject
    ) {
        if (userType != UserType.EXISTING) {
            return
        }
        if (data != null) {
            data.userType = userType
            data.existingUserStatus = tooltipShown
        } else {
            val newData = TooltipData(
                map = null, userType = userType, existingUserStatus = tooltipShown
            )
            appInstanceObject.featureWalkThrough.tooltipData = newData
        }
        appInstanceObject.save()
    }

    private fun getCurrentUserType(data: TooltipData?, isUserAuthenticated: Boolean): UserType {
        var userType = data?.userType
        if (userType == null) {
            userType = if (isUserAuthenticated) {
                UserType.EXISTING
            } else {
                UserType.NEW
            }
        }
        return userType
    }
}

data class TooltipData(
    var map: MutableMap<Delivery, TooltipShown?>?,
    var userType: UserType?,
    var existingUserStatus: TooltipShown?
)

enum class UserType {
    NEW, EXISTING
}

enum class TooltipShown {
    FULFILMENT, FULFILMENT_SECOND, LOCATION, COMPLETED
}