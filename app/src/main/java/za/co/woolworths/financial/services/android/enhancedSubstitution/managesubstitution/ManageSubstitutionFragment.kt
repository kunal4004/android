package za.co.woolworths.financial.services.android.enhancedSubstitution.managesubstitution

import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import com.awfs.coordination.R
import com.awfs.coordination.databinding.LayoutManageSubstitutionBinding
import za.co.woolworths.financial.services.android.util.binding.BaseFragmentBinding

class ManageSubstitutionFragment : BaseFragmentBinding<LayoutManageSubstitutionBinding>(LayoutManageSubstitutionBinding::inflate),OnClickListener {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnConfirm?.setOnClickListener(this)
        binding.dontWantText?.setOnClickListener(this)
        binding.tvSearchProduct?.setOnClickListener(this)
        binding.rbShopperChoose?.setOnClickListener(this)
        binding.rbOwnSubstitute?.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
      when(v?.id) {
          R.id.btnConfirm -> confirmSubstitutionProduct()
          R.id.dontWantText -> confirmDontWantSubstitutionForProduct()
          R.id.tvSearchProduct -> searchForSubstitutions()
          R.id.rbShopperChoose -> clickOnShopperChooseOption()
          R.id.rbOwnSubstitute -> clickOnOwnSubstitutionsOption()
      }
    }

    private fun clickOnOwnSubstitutionsOption() {

    }

    private fun clickOnShopperChooseOption() {

    }

    private fun searchForSubstitutions() {

    }

    private fun confirmSubstitutionProduct() {

    }

    private fun confirmDontWantSubstitutionForProduct() {

    }


}