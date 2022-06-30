package za.co.woolworths.financial.services.android.common

import java.math.BigDecimal
import java.math.RoundingMode

fun Any.changeMeterToKM(distance: Int): String {

    return if (distance >= 1000) {
        var bd: BigDecimal = BigDecimal.valueOf(distance / 1000.0)
        bd = bd.setScale(1, RoundingMode.HALF_UP)
        bd.toString() + "km"
    } else {
        distance.toString() + "m"
    }

}

fun Any.convertToTitleCase(input: String): String {
    val titleCase = java.lang.StringBuilder(input.length)
    var nextTitleCase = true
    for (c1 in input.lowercase().toCharArray()) {
        var c = c1
        if (Character.isSpaceChar(c)) {
            nextTitleCase = true
        } else if (nextTitleCase) {
            c = Character.toTitleCase(c)
            nextTitleCase = false
        }
        titleCase.append(c)
    }
    return titleCase.toString()
}