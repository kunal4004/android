package za.co.woolworths.financial.services.android.util

import android.content.Context
import za.co.woolworths.financial.services.android.models.WoolworthsApplication
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class Assets {
    companion object {
        @Throws(RuntimeException::class)
        fun readAsString(name: String): String {
            return try {
                val context: Context = WoolworthsApplication.getAppContext()
                val inputStream = context.assets.open(name)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line = bufferedReader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = bufferedReader.readLine()
                    if (line != null) {
                        stringBuilder.append("\n")
                    }
                }
                stringBuilder.toString()
            } catch (ioException: IOException) {
                throw RuntimeException("Failed to load asset $name", ioException)
            }
        }
    }
}