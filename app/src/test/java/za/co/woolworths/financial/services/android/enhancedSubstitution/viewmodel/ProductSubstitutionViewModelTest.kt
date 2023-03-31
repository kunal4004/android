package za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import za.co.woolworths.financial.services.android.enhancedSubstitution.getOrAwaitValue
import za.co.woolworths.financial.services.android.enhancedSubstitution.model.ProductSubstitution
import za.co.woolworths.financial.services.android.enhancedSubstitution.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource

class ProductSubstitutionViewModelTest {

    private val testDispathcer = StandardTestDispatcher()

    @Mock
    private lateinit var productSubstitutionRepository: ProductSubstitutionRepository

    @Mock
    private lateinit var productSubstitution: ProductSubstitution

    @Mock
    private lateinit var skusInventoryForStoreResponse: SkusInventoryForStoreResponse

    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispathcer)
    }

    @Test
    fun  test_emptylist_getSubstitutions() = runTest{
        Mockito.`when`(productSubstitutionRepository
                .getProductSubstitution("6009195203504")).thenReturn(Resource.success(productSubstitution))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getProductSubstitution("6009195203504")
        testDispathcer.scheduler.advanceUntilIdle()
        val result = sut.productSubstitution.getOrAwaitValue()
        Assert.assertEquals(0, result.peekContent().data?.data?.size)
    }

    @Test
    fun test_error_getSubstitutions() = runTest{
        Mockito.`when`(productSubstitutionRepository
                .getProductSubstitution("6009195203504")).thenReturn(Resource.error(R.string.error_unknown, null))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getProductSubstitution("6009195203504")
        testDispathcer.scheduler.advanceUntilIdle()
        val result = sut.productSubstitution.getOrAwaitValue()
        Assert.assertEquals(R.string.error_unknown , result.peekContent().message)
    }

    @Test
    fun  test_EmptyResponse_getInventory() = runTest{
        Mockito.`when`(productSubstitutionRepository
                .getInventoryForSubstitution("473","6001009025692")).thenReturn(Resource.success(skusInventoryForStoreResponse))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getInventoryForSubstitution("473","6001009025692")
        testDispathcer.scheduler.advanceUntilIdle()
        val result = sut.inventorySubstitution.getOrAwaitValue()
        Assert.assertEquals(skusInventoryForStoreResponse, result.peekContent().data)
        Assert.assertEquals(null, result.peekContent().data?.storeId)
    }

    @Test
    fun test_error_getInventory() = runTest{
        Mockito.`when`(productSubstitutionRepository
                .getInventoryForSubstitution("473","6001009025692")).thenReturn(Resource.error(R.string.error_unknown, null))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getInventoryForSubstitution("473","6001009025692")
        testDispathcer.scheduler.advanceUntilIdle()
        val result = sut.inventorySubstitution.getOrAwaitValue()
        Assert.assertEquals(R.string.error_unknown , result.peekContent().message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}