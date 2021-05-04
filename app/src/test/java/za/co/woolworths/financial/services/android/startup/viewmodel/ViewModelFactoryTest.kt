package za.co.woolworths.financial.services.android.startup.viewmodel

import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import za.co.woolworths.financial.services.android.startup.service.network.StartupApiHelper
import za.co.woolworths.financial.services.android.startup.service.repository.StartUpRepository
import za.co.woolworths.financial.services.android.utils.mock


/**
 * Created by Kunal Uttarwar on 25/2/21.
 */

@RunWith(MockitoJUnitRunner::class)
class ViewModelFactoryTest {
    private lateinit var viewModelFactory: ViewModelFactory
    private lateinit var startUpRepository: StartUpRepository
    private lateinit var startupApiHelper: StartupApiHelper

    @Before
    fun setUp() {
        startUpRepository = mock()
        startupApiHelper = mock()
        viewModelFactory = ViewModelFactory(startUpRepository, startupApiHelper)
    }

    @Test
    fun check_if_return_MainViewModel_object() {
        val viewModelObj: StartupViewModel = viewModelFactory.create(StartupViewModel::class.java)
        assertThat(viewModelObj, instanceOf(StartupViewModel::class.java))
    }

    @Test
    fun check_if_method_throws_exception() {
        val exception: Exception = assertThrows(IllegalArgumentException::class.java) {
            viewModelFactory.create(TestViewModel::class.java)
        }
        val actualMessage = exception.message
        val expectedMessage = "Unknown class name"
        assertTrue(actualMessage.equals(expectedMessage))
    }
}