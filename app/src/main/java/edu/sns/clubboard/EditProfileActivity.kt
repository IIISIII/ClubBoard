package edu.sns.clubboard

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.chooser.ImageChooser
import edu.sns.clubboard.data.PathMaker
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityEditProfileBinding
import edu.sns.clubboard.ui.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class EditProfileActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityEditProfileBinding.inflate(layoutInflater)
    }

    private val auth = FBAuthorization.getInstance()

    private val fileManager = FBFileManager.getInstance()

    private val loadingDialog = LoadingDialog()

    private lateinit var imageChooser: ImageChooser

    private var selectedUri: Uri? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        imageChooser = ImageChooser(this) {
            selectedUri = it
            fileManager.getImageFromUri(this, it)?.run {
                binding.memberImg.setImageBitmap(this)
            }
        }

        intent.getStringExtra(UserProfileActivity.USER_ID)?.run {
            init(this)
        } ?: finish()
    }

    private fun init(userId: String)
    {
        loadingStart()

        auth.getUserInfo(userId) {
            if(it == null) {
                finish()
                return@getUserInfo
            }

            if(it.imagePath != null) {
                fileManager.getImage(it.imagePath!!) { img ->
                    if(img != null)
                        binding.memberImg.setImageBitmap(img)
                }
            }

            binding.inputNickname.editText?.setText(it.nickname)
            binding.inputPhone.editText?.setText(it.phone)
            binding.memberId.text = it.studentId
            binding.memberName.text = it.name

            binding.memberImg.setOnClickListener {
                imageChooser.launch()
            }

            binding.btnSubmit.setOnClickListener {
                checkAndUpdateProfile()
            }

            loadingEnd()
        }
    }

    private fun checkAndUpdateProfile()
    {
        loadingStart()

        val user = auth.getUserInfo()!!
        val nickname = binding.inputNickname.editText?.text.toString().trim()
        val phone = binding.inputPhone.editText?.text.toString().trim()

        val pattern = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")

        var isErrorNicknameConflict = false
        var isErrorNicknameBlank = nickname.isBlank()
        var isErrorPhonePatternWrong = !pattern.matcher(phone).matches()

        if(user.nickname != nickname) {
            CoroutineScope(Dispatchers.IO).launch {
                isErrorNicknameConflict = !auth.isValidNickname(nickname)
                runOnUiThread {
                    checkAndUpdateProfile(isErrorNicknameBlank, isErrorNicknameConflict, isErrorPhonePatternWrong, nickname, phone)
                }
            }
        }
        else
            checkAndUpdateProfile(isErrorNicknameBlank, isErrorNicknameConflict, isErrorPhonePatternWrong, nickname, phone)
    }

    private fun checkAndUpdateProfile(
        isErrorNicknameBlank: Boolean,
        isErrorNicknameConflict: Boolean,
        isErrorPhonePatternWrong: Boolean,
        nickname: String,
        phone: String
    ) {
        binding.inputNickname.isErrorEnabled = isErrorNicknameBlank || isErrorNicknameConflict
        binding.inputPhone.isErrorEnabled = isErrorPhonePatternWrong

        val textNickname = resources.getString(R.string.text_nickname)
        val textPhone = resources.getString(R.string.text_phone)

        if(isErrorNicknameBlank)
            binding.inputNickname.error = resources.getString(R.string.error_empty, textNickname)
        else if(isErrorNicknameConflict)
            binding.inputNickname.error = resources.getString(R.string.error_exists, textNickname)

        if(isErrorPhonePatternWrong)
            binding.inputPhone.error = resources.getString(R.string.error_pattern_mismatch, textPhone)

        if(isErrorNicknameBlank || isErrorNicknameConflict || isErrorPhonePatternWrong) {
            loadingEnd()
            return
        }

        val user = auth.getUserInfo()!!

        updateProfile(user, nickname, phone)
    }

    private fun updateProfile(user: User, nickname: String, phone: String)
    {
        if(selectedUri != null) {
            fileManager.uploadFile(this, selectedUri!!, PathMaker.makeWithUser(user)) {
                if(it != null)
                    updateProfile(user, nickname, phone, it)
                else {
                    loadingEnd()
                    //error dialog
                }
            }
        }
        else
            updateProfile(user, nickname, phone, null)
    }

    private fun updateProfile(user: User, nickname: String, phone: String, imgPath: String?)
    {
        auth.updateProfile(user.id!!, nickname, phone, imgPath) {
            if(it) {
                setResult(RESULT_OK)
                finish()
            }
            else {
                loadingEnd()
                //error dialog
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
}