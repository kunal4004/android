package za.co.woolworths.financial.services.android.ui.fragments.shop.domain

import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import za.co.woolworths.financial.services.android.models.dao.AppInstanceObject
import za.co.woolworths.financial.services.android.util.SessionUtilities
import za.co.woolworths.financial.services.android.util.wenum.Delivery

interface CotextualTooltipShowcase {

    fun updateToolTipUserSession()

    fun toolTipToDisplay(delivery: Delivery?): TooltipShown?

    fun markTooltipShown(delivery: Delivery, tooltipShown: TooltipShown)

}

class ContextualTooltipShowcaseManager : CotextualTooltipShowcase {
    private fun saveSession(session: ShopTooltipUserSession) {
        val appInstanceObject = AppInstanceObject.get()
        val data = appInstanceObject.featureWalkThrough.tooltipData
        if (data != null) {
            data.session = session
        } else {
            val newData = TooltipData(
                map = null, session = session
            )
            appInstanceObject.featureWalkThrough.tooltipData = newData
        }
        appInstanceObject.save()
        WoolworthsApplication.getInstance().toolTipUserSession = session
    }

    override fun updateToolTipUserSession() {
        val savedSession = getSession()
        val localSession = getLocalSession()
        if (localSession == null) {
            //This is a new session (User has launched the app again after killing it but with shop page visited before)
            when(savedSession) {
                ShopTooltipUserSession.FIRST -> {
                    saveSession(ShopTooltipUserSession.SECOND)
                }
                ShopTooltipUserSession.SECOND -> {
                    saveSession(ShopTooltipUserSession.COMPLETED)
                }
                ShopTooltipUserSession.COMPLETED -> {
                    //TODO, do nothing as of now, we'll update here if required
                }
                null -> {
                    saveSession(ShopTooltipUserSession.FIRST)
                }
            }
        }
    }

    private fun getSession(): ShopTooltipUserSession? {
        return AppInstanceObject.get().featureWalkThrough.tooltipData?.session
    }

    private fun getLocalSession(): ShopTooltipUserSession? {
        return WoolworthsApplication.getInstance().toolTipUserSession
    }

    override fun toolTipToDisplay(delivery: Delivery?): TooltipShown? {
        if (delivery == null) {
            return null
        }
        if (!SessionUtilities.getInstance().isUserAuthenticated) {
            val data = AppInstanceObject.get().featureWalkThrough.tooltipData
            return when (data.session) {
                ShopTooltipUserSession.FIRST -> {
                    //This is the first time session case
                    getTooltipToShow(data, delivery)
                }

                ShopTooltipUserSession.SECOND -> {
                    //This is the second time session case
                    getTooltipToShow(data, delivery)
                }

                ShopTooltipUserSession.COMPLETED -> {
                    //This is the third time onwards session case
                    getTooltipToShow(data, delivery)
                }

                else -> {
                    return null
                }
            }
        }
        return null
    }

    private fun getTooltipToShow(
        data: TooltipData,
        delivery: Delivery,
    ) = if (data.map?.contains(delivery) == true) {
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

    override fun markTooltipShown(
        delivery: Delivery, tooltipShown: TooltipShown
    ) {
        val session = getSession()
        val appInstanceObject = AppInstanceObject.get()
        val data = appInstanceObject.featureWalkThrough.tooltipData
        if (data != null) {
            data.session = (session ?: ShopTooltipUserSession.FIRST)
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
                map = newMap, session = (session ?: ShopTooltipUserSession.FIRST)
            )
            appInstanceObject.featureWalkThrough.tooltipData = newData
        }
        appInstanceObject.save()
    }
}

data class TooltipData(
    var session: ShopTooltipUserSession?, var map: MutableMap<Delivery, TooltipShown?>?
)


enum class ShopTooltipUserSession {
    FIRST, SECOND, COMPLETED
}

enum class TooltipShown {
    FULFILMENT, LOCATION, COMPLETED
}