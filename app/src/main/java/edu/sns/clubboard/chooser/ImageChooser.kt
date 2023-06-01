package edu.sns.clubboard.chooser

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class ImageChooser
{
    private val context: AppCompatActivity

    private val resultLauncher: ActivityResultLauncher<Intent>

    private val onImageSelected: (uri: Uri) -> Unit


    constructor(context: AppCompatActivity, onImageSelected: (uri: Uri) -> Unit)
    {
        this.context = context
        this.onImageSelected = onImageSelected

        resultLauncher = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                it.data?.data?.let { uri ->
                    onImageSelected(uri)
                }
            }
        }
    }

    fun launch()
    {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        resultLauncher.launch(intent)
    }
}