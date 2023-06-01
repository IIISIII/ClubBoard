package edu.sns.clubboard

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.chooser.ImageChooser
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.PathMaker
import edu.sns.clubboard.databinding.ActivityClubProfileBinding
import edu.sns.clubboard.ui.LoadingDialog

class ClubProfileActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityClubProfileBinding.inflate(layoutInflater)
    }

    private val clubInterface = FBClub.getInstance()

    private val fileManager = FBFileManager.getInstance()

    private val loadingDialog = LoadingDialog()

    private lateinit var imageChooser: ImageChooser

    private var selectedUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val clubId = intent.getStringExtra("club_id")
        if(clubId == null) {
            finish()
            return
        }

        imageChooser = ImageChooser(this) {
            selectedUri = it
            val bitmap = fileManager.getImageFromUri(this, it)
            if(bitmap != null)
                binding.clubImg.setImageBitmap(bitmap)
        }

        init(clubId)
    }

    private fun init(clubId: String)
    {
        loadingStart()

        clubInterface.getClubData(clubId, onComplete = { club, _ ->
            club.imgPath?.run {
                fileManager.getImage(this) { img ->
                    if(img != null)
                        binding.clubImg.setImageBitmap(img)
                    else
                        binding.clubImg.setImageResource(R.drawable.ic_baseline_add_photo_alternate_24)
                }
            }

            binding.clubTitle.text = club.name

            binding.inputDescription.editText?.setText(club.description)

            binding.submitBtn.setOnClickListener {
                loadingStart()

                startModifyClubProfile(club)
            }

            binding.clubImg.setOnClickListener {
                imageChooser.launch()
            }

            loadingEnd()
        }, onFailed = {
            finish()
        })
    }

    private fun loadingStart()
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
    }

    private fun startModifyClubProfile(club: Club)
    {
        if(selectedUri != null) {
            fileManager.uploadFile(this, selectedUri!!, PathMaker.makeWithClub(club.name)) {
                if(it == null) {
                    loadingEnd()
                    return@uploadFile
                }

                modifyClubProfile(club, it)
            }
        }
        else
            modifyClubProfile(club, null)
    }

    private fun modifyClubProfile(club: Club, imgPath: String?)
    {
        val description = binding.inputDescription.editText?.text.toString()

        clubInterface.modifyClubInfo(club, description, imgPath) {
            setResult(RESULT_OK)
            finish()
        }
    }
}