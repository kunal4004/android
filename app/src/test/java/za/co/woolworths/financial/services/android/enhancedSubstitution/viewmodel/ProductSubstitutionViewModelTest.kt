package za.co.woolworths.financial.services.android.enhancedSubstitution.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.awfs.coordination.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import za.co.woolworths.financial.services.android.enhancedSubstitution.EnhanceSubstitutionHelperTest
import za.co.woolworths.financial.services.android.enhancedSubstitution.apihelper.SubstitutionApiHelperTest
import za.co.woolworths.financial.services.android.enhancedSubstitution.getOrAwaitValue
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.model.*
import za.co.woolworths.financial.services.android.enhancedSubstitution.service.repository.ProductSubstitutionRepository
import za.co.woolworths.financial.services.android.models.dto.SkusInventoryForStoreResponse
import za.co.woolworths.financial.services.android.models.network.Resource

@ExperimentalCoroutinesApi
class ProductSubstitutionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Mock
    private lateinit var productSubstitutionRepository: ProductSubstitutionRepository

    @Mock
    private lateinit var productSubstitution: ProductSubstitution

    @Mock
    private lateinit var skusInventoryForStoreResponse: SkusInventoryForStoreResponse

    @Mock
    private lateinit var addSubstitutionResponse: AddSubstitutionResponse

    @Mock
    private lateinit var kiboProductResponse: KiboProductResponse


    @get:Rule
    val testInstantTaskExecutorRule: TestRule = InstantTaskExecutorRule()

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun getSubstitutions_withEmptyList() = runTest {
        `when`(
            productSubstitutionRepository
                .getProductSubstitution("6009195203504")
        ).thenReturn(Resource.success(productSubstitution))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getProductSubstitution("6009195203504")
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.productSubstitution.getOrAwaitValue()
        Assert.assertEquals(0, result.peekContent().data?.data?.size)
    }

    @Test
    fun getSubstitutions_withError() = runTest {
        `when`(
            productSubstitutionRepository
                .getProductSubstitution("6009195203504")
        ).thenReturn(Resource.error(R.string.error_unknown, null))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getProductSubstitution("6009195203504")
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.productSubstitution.getOrAwaitValue()
        Assert.assertEquals(R.string.error_unknown, result.peekContent().message)
    }

    @Test
    fun getInventory_withEmptyResponse() = runTest {
        `when`(
            productSubstitutionRepository
                .fetchInventoryForKiboProducts("473", "6001009025692")
        ).thenReturn(Resource.success(skusInventoryForStoreResponse))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getInventoryForKiboProducts("473", "6001009025692")
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.stockInventoryResponse.getOrAwaitValue()
        Assert.assertEquals(skusInventoryForStoreResponse, result.peekContent().data)
        Assert.assertEquals(null, result.peekContent().data?.storeId)
    }

    @Test
    fun getInventory_withError() = runTest {
        `when`(
            productSubstitutionRepository
                .fetchInventoryForKiboProducts("473", "6001009025692")
        ).thenReturn(Resource.error(R.string.error_unknown, null))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getInventoryForKiboProducts("473", "6001009025692")
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.stockInventoryResponse.getOrAwaitValue()
        Assert.assertEquals(R.string.error_unknown, result.peekContent().message)
    }


    @Test
    fun addSubstitution_withEmptyResponse() = runTest {
        val addSubstitutionRequest = AddSubstitutionRequest(
            SubstitutionApiHelperTest.USER_CHOICE,
            EnhanceSubstitutionHelperTest.SUBSTITUTION_ID,
            EnhanceSubstitutionHelperTest.COMMARCE_ITEM_ID
        )
        `when`(
            productSubstitutionRepository
                .addSubstitution(addSubstitutionRequest)
        ).thenReturn(Resource.success(addSubstitutionResponse))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.addSubstitutionForProduct(addSubstitutionRequest)
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.addSubstitutionResponse.getOrAwaitValue()
        Assert.assertEquals(addSubstitutionResponse, result.peekContent().data)
        Assert.assertEquals(0, result.peekContent().data?.data?.size)
    }

    @Test
    fun addSubstitution_withError() = runTest {
        val addSubstitutionRequest = AddSubstitutionRequest(
            SubstitutionApiHelperTest.USER_CHOICE,
            EnhanceSubstitutionHelperTest.SUBSTITUTION_ID,
            EnhanceSubstitutionHelperTest.COMMARCE_ITEM_ID
        )
        `when`(
            productSubstitutionRepository
                .addSubstitution(addSubstitutionRequest)
        ).thenReturn(Resource.error(R.string.error_unknown, null))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.addSubstitutionForProduct(addSubstitutionRequest)
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.addSubstitutionResponse.getOrAwaitValue()
        Assert.assertEquals(R.string.error_unknown, result.peekContent().message)
    }

    @Test
    fun getKibo_withError() = runTest {
        val getProductRequest = EnhanceSubstitutionHelperTest.prepareKiboProductRequest()
        `when`(
            productSubstitutionRepository
                .fetchKiboProducts(getProductRequest)
        ).thenReturn(Resource.error(R.string.error_unknown, null))

        val sut = ProductSubstitutionViewModel(repository = productSubstitutionRepository)
        sut.getKiboProducts(getProductRequest)
        testDispatcher.scheduler.advanceUntilIdle()
        val result = sut.kiboProductResponse.getOrAwaitValue()
        Assert.assertEquals(R.string.error_unknown, result.peekContent().message)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }
}