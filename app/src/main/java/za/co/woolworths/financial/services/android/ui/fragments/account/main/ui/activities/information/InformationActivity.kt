package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.activities.information

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.databinding.ActivityInformationBinding
import dagger.hilt.android.AndroidEntryPoint
import za.co.woolworths.financial.services.android.ui.fragments.account.main.domain.sealing.InformationData
import za.co.woolworths.financial.services.android.ui.fragments.account.main.util.Constants.INFORMATION_DATA

@AndroidEntryPoint
class InformationActivity : AppCompatActivity(), View.OnClickListener {

    val viewModel by viewModels<InformationViewModel>()
    lateinit var binding: ActivityInformationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setAdapter()
        binding.ivInformationClose.setOnClickListener(this)

    }


    private fun setAdapter() {
        val informationData = intent.extras?.getParcelable<InformationData>(INFORMATION_DATA)
        binding.informationRcv.adapter = InformationAdapter(informationData!!.info)
    }

    override fun onClick(view: View?) {
        when(view){
            binding.ivInformationClose->{
                finish()
            }
        }
    }
}