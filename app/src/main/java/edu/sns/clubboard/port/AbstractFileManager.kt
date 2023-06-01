package edu.sns.clubboard.port

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri

abstract class AbstractFileManager
{
    abstract fun uploadFile(context: Context, uri: Uri, path: String, onComplete: (String?) -> Unit)

    abstract fun getImage(path: String, onComplete: (Bitmap?) -> Unit)

    abstract fun removeImage(path: String)
}