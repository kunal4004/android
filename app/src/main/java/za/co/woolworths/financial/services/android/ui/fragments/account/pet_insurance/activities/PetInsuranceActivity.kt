package za.co.woolworths.financial.services.android.ui.fragments.account.pet_insurance.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.R
import com.awfs.coordination.databinding.ActivityApplyNowBinding
import com.awfs.coordination.databinding.ActivityPetInsuranceBinding

class PetInsuranceActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPetInsuranceBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPetInsuranceBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}