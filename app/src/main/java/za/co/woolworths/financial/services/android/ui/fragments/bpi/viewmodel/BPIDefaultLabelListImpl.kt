package za.co.woolworths.financial.services.android.ui.fragments.bpi.viewmodel

import za.co.woolworths.financial.services.android.models.dto.bpi.DefaultLabel
import za.co.woolworths.financial.services.android.ui.fragments.bpi.contract.BPIDefaultLabelInterface

class BPIDefaultLabelListImpl : BPIDefaultLabelInterface {
    override fun defaultLabel(): DefaultLabel {
        return DefaultLabel()
    }
}