package za.co.woolworths.financial.services.android.util

import org.mockito.Mockito

/**
 * Created by Kunal Uttarwar on 25/2/21.
 */

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)
