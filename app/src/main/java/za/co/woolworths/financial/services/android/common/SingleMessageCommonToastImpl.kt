package za.co.woolworths.financial.services.android.common

import android.app.Activity
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Toast
import com.awfs.coordination.databinding.SingleLineCommonToastBinding
import javax.inject.Inject

class SingleMessageCommonToastImpl @Inject constructor(

) : SingleMessageCommonToast {

    override fun showMessage(context: Activity, mesage: String,yOffset:Int) {
        val binding = SingleLineCommonToastBinding.inflate(LayoutInflater.from(context), null, false)
        binding.toastMessage?.text = mesage

        Toast(context).apply {
            duration = Toast.LENGTH_LONG
            setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, yOffset)
            view = binding.root
            show()
        }

    }
}