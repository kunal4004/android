package za.co.woolworths.financial.services.android.util.mappers.pma

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderDomain
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntity
import za.co.woolworths.financial.services.android.util.mappers.pma.EntityMapper.toDomain
import za.co.woolworths.financial.services.android.util.mappers.pma.EntityMapper.toEntity

internal class EntityMapperTest {
    private lateinit var configPMAPayByDebitOrderEntity: PMAPayByDebitOrderEntity
    private lateinit var pmaPayByDebitOrderDomain: PMAPayByDebitOrderDomain

    @Before
    fun setUp() {
        configPMAPayByDebitOrderEntity =
            EntityMapperTestUtil.getSampleConfigPMAPayByDebitOrderEntity()
        pmaPayByDebitOrderDomain = EntityMapperTestUtil.getPMAPayByDebitOrderDomain()
    }

    @Test
    fun `when toDomain is called on an object of type PMAPayByDebitOrderEntity_the expected object of type PMAPayByDebitOrderDomain is returned`() {
        val result = configPMAPayByDebitOrderEntity.toDomain()
        assertThat(result).isEqualTo(pmaPayByDebitOrderDomain)
    }

    @Test
    fun `when toEntity is called on an object of type PMAPayByDebitOrderDomain_the expected object of type PMAPayByDebitOrderEntity is returned`() {
        val result = pmaPayByDebitOrderDomain.toEntity()
        assertThat(result).isEqualTo(configPMAPayByDebitOrderEntity)
    }
}
