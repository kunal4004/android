package za.co.woolworths.financial.services.android.contracts

import android.location.Location

interface ILocationProvider {
    fun onLocationChange(location: Location?)
    fun onPopUpLocationDialogMethod(){
        //Optional
    }
}