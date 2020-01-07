package za.co.woolworths.financial.services.android.models.dto.account

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable

open class MoreBenefitItems : Parcelable {

    var description: String? = null
        private set

    constructor(description: String?) {
        this.description = description
    }

    protected constructor(`in`: Parcel) {
        description = `in`.readString()
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is MoreBenefitItems) return false

        val moreBenefit = o as MoreBenefitItems?

        return if (description != null) description == moreBenefit?.description else moreBenefit?.description == null

    }

    override fun hashCode(): Int {
        var result = if (description != null) description!!.hashCode() else 0
        result = 31 * result + 0
        return result
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(description)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {

        @SuppressLint("ParcelCreator")
        val CREATOR: Parcelable.Creator<MoreBenefitItems> =
                object : Parcelable.Creator<MoreBenefitItems> {
                    override fun createFromParcel(`in`: Parcel): MoreBenefitItems {
                        return MoreBenefitItems(`in`)
                    }

                    override fun newArray(size: Int): Array<MoreBenefitItems?> {
                        return arrayOfNulls(size)
                    }
                }
    }
}

