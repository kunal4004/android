package za.co.woolworths.financial.services.android.util

import java.io.InputStreamReader

object TestHelper {

    fun readJsonFile(fileName: String): String {

        val inputStream = TestHelper::class.java.getResourceAsStream(fileName)
        val builder = StringBuilder()
        val reader = InputStreamReader(inputStream, "UTF-8")
        reader.readLines().forEach{
            builder.append(it)
        }
        return builder.toString()
    }
}