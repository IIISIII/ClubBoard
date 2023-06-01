package edu.sns.clubboard.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import edu.sns.clubboard.port.AbstractFileManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FBFileManager: AbstractFileManager()
{
    companion object
    {
        private var instance: FBFileManager? = null

        fun getInstance() =
            instance ?: FBFileManager().also {
                instance = it
            }
    }

    private val storage = Firebase.storage

    private val imageMap = HashMap<String, Bitmap>()

    override fun uploadFile(context: Context, uri: Uri, path: String, onComplete: (String?) -> Unit)
    {
        val ref = storage.reference.child(path)

        ref.putFile(uri).addOnCompleteListener {
            if(it.isSuccessful) {
                it.result.metadata?.let { meta ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val img = getImageFromUri(context, uri)
                        if(img != null)
                            imageMap[meta.path] = img
                    }
                }
            }

            onComplete(it.result?.metadata?.path)
        }
    }

    override fun getImage(path: String, onComplete: (Bitmap?) -> Unit)
    {
        val ref = storage.reference

        CoroutineScope(Dispatchers.IO).launch {
            getImage(ref, path) {}
        }

        getImage(ref, path) {
            onComplete(it)
        }
    }

    private fun getImage(reference: StorageReference, path: String, onComplete: (Bitmap?) -> Unit)
    {
        reference.child(path).getBytes(Long.MAX_VALUE).addOnCompleteListener {
            var img: Bitmap? = null
            if(it.isSuccessful) {
                val bitmap = BitmapFactory.decodeByteArray(it.result, 0, it.result.size)
                imageMap[path] = bitmap
                img = bitmap
            }

            onComplete(img)
        }
    }

    fun getImageFromUri(context: Context, uri: Uri): Bitmap?
    {
        var bit: Bitmap? = null

        bit = try {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
            else
                MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        } catch (_: Exception) {
            null
        }

        return bit
    }

    override fun removeImage(path: String)
    {
        val ref = storage.reference

        ref.child(path).delete()
    }
}