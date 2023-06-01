package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Member
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityManageMemberBinding
import edu.sns.clubboard.ui.LoadingDialog
import edu.sns.clubboard.ui.MemberAdapter

class ManageMemberActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityManageMemberBinding.inflate(layoutInflater)
    }

    private val clubInterface = FBClub.getInstance()

    private val loadingDialog = LoadingDialog()

    private lateinit var memberAdapter: MemberAdapter

    private lateinit var launcher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        memberAdapter = MemberAdapter()
        memberAdapter.setOnListChange {
            binding.memberCount.text = it.size.toString()
        }

        binding.memberList.apply {
            adapter = memberAdapter
            addItemDecoration(DividerItemDecoration(this@ManageMemberActivity, LinearLayout.VERTICAL))
        }

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                it.data?.run {
                    val type = this.getIntExtra(MemberProfileActivity.RESULT_CODE_TYPE, -1)
                    when(type) {
                        MemberProfileActivity.RESULT_CODE_DELEGATE -> {
                            setResult(RESULT_OK)
                            finish()
                        }
                        MemberProfileActivity.RESULT_CODE_UPDATE -> {
                            val index = this.getIntExtra(MemberProfileActivity.DATA_INDEX, -1)
                            val level = this.getLongExtra(MemberProfileActivity.DATA_PERMISSION_LEVEL, User.PERMISSION_LEVEL_MEMBER)
                            if(index >= 0)
                                memberAdapter.updateMemberPermission(index, level)
                        }
                        MemberProfileActivity.RESULT_CODE_KICK -> {
                            val index = this.getIntExtra(MemberProfileActivity.DATA_INDEX, -1)
                            if(index >= 0)
                                memberAdapter.deleteMember(index)
                        }
                    }
                }
            }
        }

        intent.getStringExtra("club_id")?.let {
            loadingStart()
            clubInterface.getClubData(it, onComplete = { club, list ->
                init(club)
            }, onFailed = {
                finish()
            })
        } ?: finish()
    }

    private fun init(club: Club)
    {
        clubInterface.getClubMembers(club, onComplete = {
            memberAdapter.setOnItemClick { member, index ->
                val intent = Intent(this, MemberProfileActivity::class.java)
                intent.putExtra(MemberProfileActivity.DATA_USER_ID, member.user.id)
                intent.putExtra(MemberProfileActivity.DATA_CLUB_ID, club.id)
                intent.putExtra(MemberProfileActivity.DATA_IS_MASTER, member.level == User.PERMISSION_LEVEL_MASTER)
                intent.putExtra(MemberProfileActivity.DATA_IS_MEMBER, member.level == User.PERMISSION_LEVEL_MANAGER)
                intent.putExtra(MemberProfileActivity.DATA_INDEX, index)
                launcher.launch(intent)
            }

            memberAdapter.setMemberList(it as ArrayList<Member>)

            loadingEnd()
        }, onFailed = {

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
}