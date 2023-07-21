package za.co.woolworths.financial.services.android.util.mappers.pma

import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderDomain
import za.co.woolworths.financial.services.android.models.dto.pma.payByDebitOrder.PMAPayByDebitOrderEntity
import za.co.woolworths.financial.services.android.util.mappers.pma.EntityMapper.toDomain

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
    fun `when toDomain is called on ConfigPMAPayByDebitOrderEntity_the expected PMAPayByDebitOrderDomain is returned`() {
        val result = configPMAPayByDebitOrderEntity.toDomain()
        assertThat(result).isEqualTo(pmaPayByDebitOrderDomain)
    }
}
