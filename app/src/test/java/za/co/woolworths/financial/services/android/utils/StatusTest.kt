package za.co.woolworths.financial.services.android.utils

import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.core.IsNull.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.service.network.ResponseStatus

/**
 * Created by Kunal Uttarwar on 25/2/21.
 */

@RunWith(MockitoJUnitRunner::class)
class StatusTest {

    @Test
    fun check_if_enum_value_exist() {
        assertThat(ResponseStatus.valueOf("SUCCESS"), `is`(notNullValue()))
        assertThat(ResponseStatus.valueOf("ERROR"), `is`(notNullValue()))
        assertThat(ResponseStatus.valueOf("LOADING"), `is`(notNullValue()))
    }
}