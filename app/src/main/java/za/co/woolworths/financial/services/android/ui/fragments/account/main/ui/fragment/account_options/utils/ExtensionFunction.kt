package za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.NavigationRes
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.awfs.coordination.R
import za.co.woolworths.financial.services.android.models.dto.account.ServerErrorResponse
import za.co.woolworths.financial.services.android.ui.fragments.account.main.ui.fragment.account_options.error_handler.GeneralErrorDialogPopupFragment
import za.co.woolworths.financial.services.android.util.AuthenticateUtils.mContext
import za.co.woolworths.financial.services.android.util.Utils
import java.util.regex.Matcher
import java.util.regex.Pattern

fun Fragment.setupGraph(
    @NavigationRes graphResId: Int,
    @IdRes containerId: Int,
    @IdRes startDestination: Int,
    @Nullable startDestinationArgs: Bundle? =  bundleOf()
) {
    val navHostFragment = childFragmentManager.findFragmentById(containerId) as? NavHostFragment
    val navController = navHostFragment?.navController
    val navGraph = navController?.navInflater?.inflate(graphResId)
    navGraph?.setStartDestination(startDestination)
    navGraph?.let { navController.setGraph(it, startDestinationArgs) }
}
fun AppCompatActivity.setupGraph(
    @NavigationRes graphResId: Int,
    @IdRes containerId: Int,
    @IdRes startDestination: Int,
    @Nullable startDestinationArgs: Bundle? =  bundleOf()
) {
    val navHostFragment = supportFragmentManager.findFragmentById(containerId) as? NavHostFragment
    val navController = navHostFragment?.navController
    val navGraph = navController?.navInflater?.inflate(graphResId)
    navGraph?.setStartDestination(startDestination)
    navGraph?.let { navController.setGraph(it, startDestinationArgs) }
}


fun showErrorDialog(activity: AppCompatActivity?, serverErrorResponse: ServerErrorResponse) {
    val dialog = GeneralErrorDialogPopupFragment.newInstance(serverErrorResponse)
    activity?.supportFragmentManager?.let { fragmentManager ->
        dialog.show(
            fragmentManager,
            GeneralErrorDialogPopupFragment::class.java.simpleName
        )
    }
}

// v is the Button view that you want the Toast to appear above
// and messageId is the id of your string resource for the message
@SuppressLint("InflateParams")
fun setToast(v: View?, messageId: Int) {
    v ?: return
    val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val layout: View = inflater.inflate(R.layout.w_toast, null)
    val toast = Toast(mContext)
    val text = layout.findViewById(R.id.toastMessageTextView) as TextView
    text.setText(messageId)
    toast.view = layout
    toast.setGravity(Gravity.BOTTOM,  0, Utils.dp2px(24f))
    toast.show()
}

fun extractPhoneNumberFromSentence(sentence : String?): Pair<Pair<Int, Int>, String>? {
    sentence ?: return null
    val listOfNumbers = mutableListOf<String>()
    var (startIndex, endIndex) = -1 to -1
    val pattern: Pattern = Pattern.compile("\\d+")
    val matcher: Matcher = pattern.matcher(sentence)
    while (matcher.find()) { listOfNumbers.add(matcher.group()) }
    val phoneNumber = listOfNumbers.joinToString(" ")

    if (listOfNumbers.isNotEmpty()){
        val first = listOfNumbers[0]
        val last = listOfNumbers.last()
        startIndex = sentence.indexOf(first)
        endIndex = sentence.lastIndexOf(last).plus(last.length)
    }

    return startIndex to endIndex to phoneNumber
}