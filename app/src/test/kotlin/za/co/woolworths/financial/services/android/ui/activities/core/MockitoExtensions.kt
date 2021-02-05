package za.co.woolworths.financial.services.android.ui.activities.core

/**
 * Created by Kunal Uttarwar on 29/1/21.
 */

import org.mockito.Mockito

inline fun <reified T> mock(): T = Mockito.mock(T::class.java)
