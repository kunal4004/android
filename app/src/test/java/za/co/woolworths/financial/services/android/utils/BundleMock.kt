package za.co.woolworths.financial.services.android.utils

import android.os.Bundle
import android.os.Parcelable
import android.util.SparseArray
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doAnswer
import org.mockito.stubbing.Answer
import java.io.Serializable
import java.util.*

/**
 * Created by Kunal Uttarwar on 07/12/21.
 */
/**
 * Returns the mock object of Bundle. Can be use to create any bundle object.
 */
object BundleMock {
    @JvmOverloads
    fun mock(map: HashMap<String?, Any?> = HashMap()): Bundle {
        val unsupported: Answer<*> = Answer<Any?> { throw UnsupportedOperationException() }
        val put: Answer<*> = Answer<Any?> { invocation ->
            map[invocation.arguments[0] as String] = invocation.arguments[1]
            null
        }
        val get: Answer<Any> = Answer { invocation -> map[invocation.arguments[0]] }
        val getOrDefault: Answer<Any> = Answer { invocation ->
            val key = invocation.arguments[0]
            if (map.containsKey(key)) map[key] else invocation.arguments[1]
        }
        val bundle = Mockito.mock(Bundle::class.java)
        doAnswer { map.size }.`when`(bundle).size()
        doAnswer { map.isEmpty() }.`when`(bundle).isEmpty
        doAnswer {
            map.clear()
            null
        }.`when`(bundle).clear()
        doAnswer { invocation -> map.containsKey(invocation.arguments[0]) }.`when`(bundle)
            .containsKey(anyString())
        doAnswer { invocation -> map[invocation.arguments[0]] }
            .`when`(bundle)[anyString()]
        doAnswer { invocation ->
            map.remove(invocation.arguments[0])
            null
        }.`when`(bundle).remove(anyString())
        doAnswer { map.keys }.`when`(bundle).keySet()
        doAnswer { BundleMock::class.java.simpleName + "{map=" + map.toString() + "}" }
            .`when`(bundle).toString()
        doAnswer(put).`when`(bundle)
            .putBoolean(anyString(), ArgumentMatchers.anyBoolean())
        `when`(bundle.getBoolean(anyString())).thenAnswer(get)
        `when`(
            bundle.getBoolean(
                anyString(),
                ArgumentMatchers.anyBoolean()
            )
        ).thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putByte(anyString(), ArgumentMatchers.anyByte())
        `when`(bundle.getByte(anyString())).thenAnswer(get)
        `when`(bundle.getByte(anyString(), ArgumentMatchers.anyByte()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putChar(anyString(), ArgumentMatchers.anyChar())
        `when`(bundle.getChar(anyString())).thenAnswer(get)
        `when`(bundle.getChar(anyString(), ArgumentMatchers.anyChar()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putInt(anyString(), ArgumentMatchers.anyShort().toInt())
        `when`(bundle.getShort(anyString())).thenAnswer(get)
        `when`(bundle.getShort(anyString(), ArgumentMatchers.anyShort()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putLong(anyString(), ArgumentMatchers.anyLong())
        `when`(bundle.getLong(anyString())).thenAnswer(get)
        `when`(bundle.getLong(anyString(), ArgumentMatchers.anyLong()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putFloat(anyString(), ArgumentMatchers.anyFloat())
        `when`(bundle.getFloat(anyString())).thenAnswer(get)
        `when`(bundle.getFloat(anyString(), ArgumentMatchers.anyFloat()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putDouble(anyString(), ArgumentMatchers.anyDouble())
        `when`(bundle.getDouble(anyString())).thenAnswer(get)
        `when`(bundle.getDouble(anyString(), ArgumentMatchers.anyDouble()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle)
            .putString(anyString(), anyString())
        `when`(bundle.getString(anyString())).thenAnswer(get)
        `when`(bundle.getString(anyString(), anyString()))
            .thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle).putBooleanArray(anyString(), any(BooleanArray::class.java))
        `when`(bundle.getBooleanArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putLongArray(anyString(), any(LongArray::class.java))
        `when`(bundle.getLongArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putDoubleArray(anyString(), any(DoubleArray::class.java))
        `when`(bundle.getDoubleArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putIntArray(anyString(), any(IntArray::class.java))
        `when`(bundle.getIntArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putInt(anyString(), ArgumentMatchers.anyInt())
        `when`(bundle.getInt(anyString())).thenAnswer(get)
        `when`(bundle.getInt(anyString(), ArgumentMatchers.anyInt())).thenAnswer(getOrDefault)
        doAnswer(unsupported).`when`(bundle).putAll(any(Bundle::class.java))
        `when`(bundle.hasFileDescriptors()).thenAnswer(unsupported)
        doAnswer(put).`when`(bundle).putShort(anyString(), ArgumentMatchers.anyShort())
        `when`(bundle.getShort(anyString())).thenAnswer(get)
        `when`(bundle.getShort(anyString(), ArgumentMatchers.anyShort())).thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle).putFloat(anyString(), ArgumentMatchers.anyFloat())
        `when`(bundle.getFloat(anyString())).thenAnswer(get)
        `when`(bundle.getFloat(anyString(), ArgumentMatchers.anyFloat())).thenAnswer(getOrDefault)
        doAnswer(put).`when`(bundle).putCharSequence(anyString(), any(CharSequence::class.java))
        `when`(bundle.getCharSequence(anyString())).thenAnswer(get)
        `when`(bundle.getCharSequence(anyString(), any(CharSequence::class.java))).thenAnswer(
            getOrDefault
        )
        doAnswer(put).`when`(bundle).putBundle(anyString(), any(Bundle::class.java))
        `when`(bundle.getBundle(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putParcelable(anyString(), any(Parcelable::class.java))
        `when`<Any?>(bundle.getParcelable(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle)
            .putParcelableArray(anyString(), any(Array<Parcelable>::class.java))
        `when`(bundle.getParcelableArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putParcelableArrayList(
            anyString(),
            any(ArrayList::class.java) as ArrayList<out Parcelable>?
        )
        `when`(bundle.getParcelableArrayList<Parcelable>(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putSparseParcelableArray(
            anyString(),
            any(SparseArray::class.java) as SparseArray<out Parcelable>?
        )
        `when`(bundle.getSparseParcelableArray<Parcelable>(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putSerializable(anyString(), any(Serializable::class.java))
        `when`(bundle.getSerializable(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle)
            .putIntegerArrayList(anyString(), any(ArrayList::class.java) as ArrayList<Int>?)
        `when`(bundle.getIntegerArrayList(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle)
            .putStringArrayList(anyString(), any(ArrayList::class.java) as ArrayList<String>?)
        `when`(bundle.getStringArrayList(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putCharSequenceArrayList(
            anyString(),
            any(ArrayList::class.java) as ArrayList<CharSequence>?
        )
        `when`(bundle.getCharSequenceArrayList(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putCharArray(anyString(), any(CharArray::class.java))
        `when`(bundle.getCharArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putByteArray(anyString(), any(ByteArray::class.java))
        `when`(bundle.getByteArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putShortArray(anyString(), any(ShortArray::class.java))
        `when`(bundle.getShortArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle).putFloatArray(anyString(), any(FloatArray::class.java))
        `when`(bundle.getFloatArray(anyString())).thenAnswer(get)
        doAnswer(put).`when`(bundle)
            .putCharSequenceArray(anyString(), any(Array<CharSequence>::class.java))
        `when`(bundle.getCharSequenceArray(anyString())).thenAnswer(get)

        return bundle
    }
}