package edu.sns.clubboard

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityMemberProfileBinding
import edu.sns.clubboard.ui.ConfirmDialog
import edu.sns.clubboard.ui.LoadingDialog

class MemberProfileActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityMemberProfileBinding.inflate(layoutInflater)
    }

    private val auth = FBAuthorization.getInstance()

    private val clubInterface = FBClub.getInstance()

    private val loadingDialog = LoadingDialog()

    private val confirmDialog = ConfirmDialog()

    companion object {
        val RESULT_CODE_DELEGATE = 0
        val RESULT_CODE_UPDATE = 1
        val RESULT_CODE_KICK = 2

        val RESULT_CODE_TYPE = "type"

        val DATA_USER_ID = "user_id"
        val DATA_CLUB_ID = "club_id"
        val DATA_IS_MASTER = "is_master"
        val DATA_IS_MEMBER = "is_member"
        val DATA_INDEX = "index"
        val DATA_PERMISSION_LEVEL = "permission_level"
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val id = intent.getStringExtra(DATA_USER_ID)
        val clubId = intent.getStringExtra(DATA_CLUB_ID)
        val isMaster = intent.getBooleanExtra(DATA_IS_MASTER, false)
        val isManager = intent.getBooleanExtra(DATA_IS_MEMBER, false)
        val index = intent.getIntExtra(DATA_INDEX, -1)
        if(id == null || clubId == null || index == -1)
            finish()

        if(isMaster)
            binding.manageGroup.visibility = View.GONE

        binding.btnManagerAppoint.text = if(isManager)
            resources.getString(R.string.text_disappoint)
        else
            resources.getString(R.string.text_appoint)

        auth.getUserInfo(id) { user ->
            if(user == null) {
                finish()
                return@getUserInfo
            }
            binding.memberNickname.text = user.nickname
            binding.memberId.text = user.studentId
            binding.memberName.text = user.name
            binding.memberPhone.text = user.phone

            if(!isMaster) {
                binding.memberPhone.setOnClickListener {
                    phoneCall(user.phone)
                }
            }
            else {
                binding.memberPhone.setTextColor(this.getColor(R.color.black))
                binding.memberPhone.isClickable = false
            }

            binding.btnDelegate.setOnClickListener { _ ->
                confirmDialog.show(this, user.name) {
                    delegateMaster(clubId!!, user)
                }
            }

            binding.btnManagerAppoint.setOnClickListener {
                confirmDialog.show(this, user.name) {
                    appointOrDisappointManager(clubId!!, user, index, isManager)
                }
            }

            binding.btnKick.setOnClickListener {
                confirmDialog.show(this, user.name) {
                    kick(clubId!!, user, index)
                }
            }
        }
    }

    private fun delegateMaster(clubId: String, user: User)
    {
        val master = auth.getUserInfo()!!

        loadingDialog.show(supportFragmentManager, "LoadingDialog")
        clubInterface.setUserPemissionLevel(clubId, user, master, User.PERMISSION_LEVEL_MASTER) {
            loadingDialog.dismiss()
            if(it) {
                val intent = Intent()
                intent.putExtra(RESULT_CODE_TYPE, RESULT_CODE_DELEGATE)
                setResult(RESULT_OK, intent)
                finish()
            }
            else
                Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun appointOrDisappointManager(clubId: String, user: User, index: Int, isManager: Boolean)
    {
        val master = auth.getUserInfo()!!

        val level = if(!isManager) User.PERMISSION_LEVEL_MANAGER else User.PERMISSION_LEVEL_MEMBER

        loadingDialog.show(supportFragmentManager, "LoadingDialog")
        clubInterface.setUserPemissionLevel(clubId, user, master, level) {
            loadingDialog.dismiss()
            if(it) {
                val intent = Intent()
                intent.putExtra(RESULT_CODE_TYPE, RESULT_CODE_UPDATE)
                intent.putExtra(DATA_INDEX, index)
                intent.putExtra(DATA_PERMISSION_LEVEL, level)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun kick(clubId: String, user: User, index: Int)
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
        clubInterface.kick(clubId, user) {
            loadingDialog.dismiss()
            if(it) {
                val intent = Intent()
                intent.putExtra(RESULT_CODE_TYPE, RESULT_CODE_KICK)
                intent.putExtra(DATA_INDEX, index)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }

    private fun phoneCall(phone: String)
    {
        if(checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.CALL_PHONE), 1)
            return
        }
        val intent = Intent()
        intent.action = Intent.ACTION_DIAL
        intent.data = Uri.parse("tel:${phone}")
        startActivity(intent)
    }
}