package edu.sns.clubboard

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.chooser.ImageChooser
import edu.sns.clubboard.data.PathMaker
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityMakeClubBinding
import edu.sns.clubboard.ui.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MakeClubActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityMakeClubBinding.inflate(layoutInflater)
    }

    private val auth = FBAuthorization.getInstance()

    private val clubInterface = FBClub.getInstance()

    private val fileManager = FBFileManager.getInstance()

    private val loadingDialog = LoadingDialog()

    private var imageChooser: ImageChooser? = null

    private var selectedUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.makeClubToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageChooser = ImageChooser(this) {
            selectedUri = it
            val bitmap = fileManager.getImageFromUri(this, it)
            if(bitmap != null)
                binding.clubImg.setImageBitmap(bitmap)
        }

        binding.clubImg.setOnClickListener {
            imageChooser?.launch()
        }

        binding.submitBtn.setOnClickListener {
            loadingStart()

            val clubName = binding.inputTitle.editText?.text.toString().trim()
            val description = binding.inputDescription.editText?.text.toString()

            val user = auth.getUserInfo()!!

            if(clubName.isEmpty()) {
                binding.inputTitle.isErrorEnabled = true
                binding.inputTitle.error = resources.getString(R.string.error_empty, resources.getString(R.string.text_club_name))
                loadingEnd()
                return@setOnClickListener
            }

            if(selectedUri != null) {
                fileManager.uploadFile(this, selectedUri!!, PathMaker.makeWithClub(clubName)) {
                    if(it == null) {
                        loadingEnd()
                        return@uploadFile
                    }
                    createClub(clubName, description, it, user)
                }
            }
            else
                createClub(clubName, description, null, user)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    private fun loadingStart()
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
    }

    private fun createClub(clubName: String, description: String, imgPath: String?, master: User)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val isValidName = clubInterface.isValidName(clubName)
            runOnUiThread {
                if(!isValidName) {
                    binding.inputTitle.isErrorEnabled = true
                    binding.inputTitle.error = resources.getString(R.string.error_exists, resources.getString(R.string.text_club_name))
                    loadingEnd()
                }
                else {
                    clubInterface.createClub(clubName, description, imgPath, master, onComplete = {
                        finish()
                    }, onFailed = {
                        loadingEnd()
                    })
                }
            }
        }
    }
}