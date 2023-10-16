package za.co.woolworths.financial.services.android.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ConnectOnline( val isFreeSimAvailable: Boolean,
                          val freeSimTextMsg: String,
                          val connectOnlineCounterUrl: String,
                          val nearestCounterUrl: String): Parcelable
