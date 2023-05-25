package edu.sns.clubboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Request
import edu.sns.clubboard.databinding.ActivityRequestManageBinding
import edu.sns.clubboard.ui.RequestAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RequestManageActivity : AppCompatActivity()
{
    private val leastCount = 2L

    private val binding by lazy {
        ActivityRequestManageBinding.inflate(layoutInflater)
    }

    private val clubInterface = FBClub.getInstance()

    private val requestAdapter = RequestAdapter()

    private lateinit var myClub: Club

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val clubId = intent.getStringExtra("club_id")

        clubInterface.getClubData(clubId!!, onComplete = { club, _ ->
            myClub = club
            loadRequestList(club)
        }, onFailed = {

        })
    }

    private fun init(club: Club, list: List<Request>)
    {
        binding.requestList.adapter = requestAdapter
        requestAdapter.setItemList(list)

        binding.btnAccept.setOnClickListener {
            val checkedList = requestAdapter.getCheckedRequestList()
            if(checkedList != null && checkedList.isNotEmpty()) {
                loadingStart()
                clubInterface.acceptRequests(checkedList, onComplete = {
                    CoroutineScope(Dispatchers.IO).launch {
                        val count = clubInterface.getClubMemberCount(club)
                        runOnUiThread {
                            if(count >= leastCount) {
                                clubInterface.activateClub(club, onSuccess = {
                                    loadRequestList(club)
                                }, onFailed = {
                                    loadRequestList(club)
                                })
                            }
                            else
                                loadRequestList(club)
                        }
                    }
                }, onFailed = {
                    loadRequestList(club)
                })
            }
        }

        binding.btnDecline.setOnClickListener {
            val checkedList = requestAdapter.getCheckedRequestList()
            if(checkedList != null && checkedList.isNotEmpty()) {
                loadingStart()
                clubInterface.declineRequests(club, checkedList, onComplete = {
                    loadRequestList(club)
                }, onFailed = {
                    loadRequestList(club)
                })
            }
        }

        loadingEnd()
    }

    private fun loadRequestList(club: Club)
    {
        clubInterface.getRequests(club, onComplete = {
            init(club, it)
        }, onFailed = {
            //error dialog
            finish()
        })
    }

    private fun loadingStart()
    {
        binding.btnAccept.isEnabled = false
        binding.btnDecline.isEnabled = false
        binding.loadingBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.loadingBackground.visibility = View.GONE
        binding.btnAccept.isEnabled = true
        binding.btnDecline.isEnabled = true
    }
}