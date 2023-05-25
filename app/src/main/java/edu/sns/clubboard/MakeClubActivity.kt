package edu.sns.clubboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.chooser.ImageChooser
import edu.sns.clubboard.databinding.ActivityMakeClubBinding
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

    private var imageChooser: ImageChooser? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.makeClubToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        imageChooser = ImageChooser(this) {
            Toast.makeText(this, "${it}", Toast.LENGTH_SHORT).show()
        }

        binding.clubImg.setOnClickListener {
            imageChooser?.startActivity()
        }

        binding.submitBtn.setOnClickListener {
            loadingStart()

            val clubName = binding.inputTitle.editText?.text.toString().trim()
            val description = binding.inputDescription.editText?.text.toString()

            val user = auth.getUserInfo()

            if(clubName.isEmpty()) {
                binding.inputTitle.isErrorEnabled = true
                binding.inputTitle.error = resources.getString(R.string.error_empty, resources.getString(R.string.text_club_name))
                loadingEnd()
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val isValidName = clubInterface.isValidName(clubName)
                runOnUiThread {
                    if(!isValidName) {
                        binding.inputTitle.isErrorEnabled = true
                        binding.inputTitle.error = resources.getString(R.string.error_exists, resources.getString(R.string.text_club_name))
                        loadingEnd()
                    }
                    else {
                        clubInterface.createClub(clubName, description, null, user!!, onComplete = {
                            finish()
                        }, onFailed = {
                            loadingEnd()
                        })
                    }
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loadingStart()
    {
        binding.loadingBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.loadingBackground.visibility = View.GONE
    }
}