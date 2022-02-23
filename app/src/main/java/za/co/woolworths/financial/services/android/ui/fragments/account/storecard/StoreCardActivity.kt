package za.co.woolworths.financial.services.android.ui.fragments.account.storecard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.awfs.coordination.databinding.AccountSignOutActivityBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StoreCardActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //TODO:Change to storecard view
        val binding: AccountSignOutActivityBinding = AccountSignOutActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}