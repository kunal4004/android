package za.co.woolworths.financial.services.android.ui.fragments.colorandsize

import za.co.woolworths.financial.services.android.models.dto.OtherSkus

interface ColorAndSizeListener {
    fun onColorSelection(selectedColor: OtherSkus?, isFromVto: Boolean = true)
    fun onSizeSelection(selectedSku: OtherSkus)
}