package edu.sns.clubboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityClubListBinding
import edu.sns.clubboard.ui.*
import java.util.*

class ClubListActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityClubListBinding.inflate(layoutInflater)
    }

    private lateinit var listAdapter: ClubListAdapter

    private val auth = FBAuthorization.getInstance()

    private val clubInterface = FBClub.getInstance()

    private lateinit var infiniteScrollListener: InfiniteScrollListener

    private val pageItemCount = 10L

    private val loadingDialog = LoadingDialog()

    private val errorDialog = ErrorDialog()

    private var cantReadMore = false

    private var alreadyInit = false

    private var searchName: String? = null

    private lateinit var launcher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK)
                init()
        }

        listAdapter = ClubListAdapter(this)
        listAdapter.setOnItemClick { club ->
            clubItemClick(club)
        }

        init()
    }

    private fun init()
    {
        loadingStart()

        binding.clubList.scrollToPosition(0)

        if(!alreadyInit) {
            infiniteScrollListener = InfiniteScrollListener()
            infiniteScrollListener.setItemSizeGettr {
                listAdapter.itemCount
            }
            infiniteScrollListener.setOnLoadingStart {
                if (!alreadyInit || this.cantReadMore)
                    return@setOnLoadingStart
                searchClub(searchName, false)
            }

            binding.clubList.apply {
                adapter = listAdapter
                addOnScrollListener(infiniteScrollListener)
                addItemDecoration(DividerItemDecoration(this@ClubListActivity, LinearLayout.VERTICAL))
            }

            binding.inputClubName.setOnKeyListener { _, keyCode, keyEvent ->
                if (keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                    startSearchClub()
                    return@setOnKeyListener true
                }
                return@setOnKeyListener false
            }

            binding.btnReset.setOnClickListener {
                binding.btnReset.visibility = View.GONE

                hideKeyboard(binding.inputClubName)

                binding.inputClubName.text.clear()

                searchName = null
                init()
            }

            binding.btnSubmit.setOnClickListener {
                startSearchClub()
            }
        }

        searchClub(searchName, true)
    }

    private fun startSearchClub()
    {
        if(binding.inputClubName.text.isBlank())
            return

        hideKeyboard(binding.inputClubName)

        loadingStart()

        searchName = binding.inputClubName.text.toString()
        searchClub(searchName, true)

        binding.btnReset.visibility = View.VISIBLE
    }

    private var searchCount = 0

    private fun searchClub(name: String?, start: Boolean)
    {
        searchCount = 0
        this.cantReadMore = false
        searchClub(name, start, pageItemCount)
    }

    private fun searchClub(name: String?, start: Boolean, limit: Long)
    {
        if(this.cantReadMore || searchCount >= limit) {
            alreadyInit = true
            loadingEnd()
            return
        }

        clubInterface.getClubListLimited(start, limit) { list, cantReadMore ->
            val searchedList = if(name != null) {
                list.filter {
                    it.name.lowercase().contains(name.lowercase())
                }
            } else list

            searchCount += searchedList.size

            this.cantReadMore = searchCount == 0 || cantReadMore

            if(start)
                listAdapter.setClubList(searchedList as ArrayList<Club>)
            else
                listAdapter.addClubList(searchedList as ArrayList<Club>)

            searchClub(name, false, limit)
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
                val intent = Intent(this@ClubListActivity, ManageMemberActivity::class.java)
                intent.putExtra("club_id", club.id)
                launcher.launch(intent)

                manageDialog.dismiss()
            }

            override fun onSecondItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@ClubListActivity, RequestManageActivity::class.java)
                intent.putExtra("club_id", club.id)
                startActivity(intent)

                manageDialog.dismiss()
            }

            override fun onThirdItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@ClubListActivity, ClubProfileActivity::class.java)
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
                        else
                            errorDialog.show(this, resources.getString(R.string.error_not_activated)) {}
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
                                        errorDialog.show(this@ClubListActivity, resources.getString(R.string.error_send_request)) {}
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
                            errorDialog.show(this, resources.getString(R.string.error_processing_request)) {}
                        }
                        loadingEnd()
                    }
                }
            }
        }
    }

    private fun hideKeyboard(view: View)
    {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
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