package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityUserProfileBinding
import edu.sns.clubboard.ui.ErrorDialog
import edu.sns.clubboard.ui.LoadingDialog

class UserProfileActivity : AppCompatActivity()
{
    companion object {
        val USER_ID = "user_id"
    }

    private val binding by lazy {
        ActivityUserProfileBinding.inflate(layoutInflater)
    }

    private val auth = FBAuthorization.getInstance()

    private val fileManager = FBFileManager.getInstance()

    private val loadingDialog = LoadingDialog()

    private val errorDialog = ErrorDialog()

    private lateinit var userId: String


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        intent.getStringExtra(USER_ID)?.run {
            userId = this
            init(userId)
        } ?: finish()

        val lacunher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK)
                init(userId)
        }

        binding.btnEditProfile.setOnClickListener {
            val intent = Intent(this, EditProfileActivity::class.java)
            intent.putExtra(USER_ID, userId)
            lacunher.launch(intent)
        }

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.btnDeleteAccount.setOnClickListener {
            loadingStart()

            auth.deleteAccount(userId) {
                when(it) {
                    User.ERROR_DELETE_ACCOUNT_MASTER -> {
                        errorDialog.show(this, resources.getString(R.string.error_delete_account_master)) {}
                    }
                    User.ERROR_DELETE_ACCOUNT -> {
                        errorDialog.show(this, resources.getString(R.string.error_delete_account)) {}
                    }
                    User.SUCCESS_DELETE_ACCOUNT -> {
                        moveToLoginActivity()
                    }
                }
                loadingEnd()
            }
        }
    }

    private fun logout()
    {
        auth.logout {
            moveToLoginActivity()
        }
    }

    private fun init(userId: String)
    {
        loadingStart()

        auth.getUserInfo(userId) {
            if(it != null) {
                binding.memberNickname.text = it.nickname
                binding.memberId.text = it.studentId
                binding.memberName.text = it.name
                binding.memberPhone.text = it.phone

                it.imagePath?.run {
                    fileManager.getImage(this) { img ->
                        if(img != null)
                            binding.memberImg.setImageBitmap(img)
                    }
                }

                loadingEnd()
            }
        }
    }

    private fun loadingStart()
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
    }

    private fun moveToLoginActivity()
    {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}