package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.DividerItemDecoration
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityMyClubBinding
import edu.sns.clubboard.ui.*
import java.util.*
import kotlin.collections.ArrayList

class MyClubActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityMyClubBinding.inflate(layoutInflater)
    }

    private val auth = FBAuthorization.getInstance()

    private val clubInterface = FBClub.getInstance()

    private val loadingDialog = LoadingDialog()

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private lateinit var listAdapter: ClubListAdapter


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.myClubToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK)
                init()
        }

        listAdapter = ClubListAdapter(this)
        listAdapter.setOnItemClick { club ->
            clubItemClick(club)
        }

        binding.clubList.apply {
            adapter = listAdapter
            addItemDecoration(DividerItemDecoration(this@MyClubActivity, LinearLayout.VERTICAL))
        }

        init()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    private fun init()
    {
        val user = auth.getUserInfo()!!

        clubInterface.getUserClubList(user) {
            binding.textNoJoin.visibility = if(it.isNotEmpty())
                    View.GONE
                else
                    View.VISIBLE

            if(it.isNotEmpty()) {
                val list = it.sortedBy { club ->
                    club.name
                }
                binding.clubList.scrollToPosition(0)
                listAdapter.setClubList(ArrayList(list))
            }
            else
                listAdapter.clear()
        }
    }

    private fun clubItemClick(club: Club)
    {
        loadingStart()

        val user = auth.getUserInfo()!!

        val manageDialog = ManageDialog()
        manageDialog.setOnMenuItemClickListener(object: ManageDialog.OnMenuButtonClickListener {
            override fun onFirstItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@MyClubActivity, ManageMemberActivity::class.java)
                intent.putExtra("club_id", club.id)
                launcher.launch(intent)

                manageDialog.dismiss()
            }

            override fun onSecondItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@MyClubActivity, RequestManageActivity::class.java)
                intent.putExtra("club_id", club.id)
                startActivity(intent)

                manageDialog.dismiss()
            }

            override fun onThirdItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@MyClubActivity, ClubProfileActivity::class.java)
                intent.putExtra("club_id", club.id)
                launcher.launch(intent)

                manageDialog.dismiss()
            }
        })

        clubInterface.checkIsActivated(club.id) { isActivated ->
            clubInterface.checkClubMember(user.id!!, club.id) { isMember, permissionLevel ->
                if(isMember) {
                    if(isActivated) {
                        val intent = Intent(this, ClubActivity::class.java)
                        intent.putExtra("club_id", club.id)
                        launcher.launch(intent)
                    }
                    else {
                        if(permissionLevel == User.PERMISSION_LEVEL_MASTER)
                            manageDialog.show(supportFragmentManager, "ManageDialog")
                        else {
                            // error dialog club is not activated
                        }
                    }
                    loadingEnd()
                }
                else {
                    clubInterface.checkProcessingRequest(user, club) { isProcessing ->
                        if(!isProcessing) {
                            val dialog = RequestDialog()

                            dialog.setOnMenuItemClickListener(object: RequestDialog.OnMenuButtonClickListener {
                                override fun onSubmit(requestDialog: RequestDialog, description: String)
                                {
                                    requestDialog.dismiss()
                                    loadingStart()
                                    clubInterface.sendRequest(user, description, Date(), club, onComplete = {
                                        loadingEnd()
                                    }, onFailed = {
                                        //show error dialog
                                        loadingEnd()
                                    })
                                }

                                override fun onCancel(requestDialog: RequestDialog)
                                {
                                    requestDialog.dismiss()
                                }
                            })
                            dialog.show(supportFragmentManager, "RequestDialog")
                        }
                        else {
                            // error dialog already request
                        }
                        loadingEnd()
                    }
                }
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