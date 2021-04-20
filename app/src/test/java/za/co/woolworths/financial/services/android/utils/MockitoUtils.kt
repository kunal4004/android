package za.co.woolworths.financial.services.android.utils

import org.mockito.Mockito
import java.lang.reflect.Field
import java.lang.reflect.Modifier

/**
 * Created by Kunal Uttarwar on 25/2/21.
 */

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)

@Throws(Exception::class)
fun setFinalStatic(field: Field, newValue: Any) {
    field.setAccessible(true)
    val modifiersField = Field::class.java.getDeclaredField("modifiers")
    modifiersField.setAccessible(true)
    modifiersField.setInt(field, field.getModifiers() and Modifier.FINAL.inv())
    field.set(null, newValue)
}