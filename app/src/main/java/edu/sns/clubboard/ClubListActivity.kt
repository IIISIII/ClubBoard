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
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityClubListBinding
import edu.sns.clubboard.ui.InfiniteScrollListener
import edu.sns.clubboard.ui.ManageDialog
import edu.sns.clubboard.ui.RequestDialog
import java.util.*

class ClubListActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityClubListBinding.inflate(layoutInflater)
    }

    private lateinit var listAdapter: ClubAdapter

    private val auth = FBAuthorization.getInstance()

    private val clubInterface = FBClub.getInstance()

    private val pageItemCount = 10L

    private var cantReadMore = false

    private var alreadyInit = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.clubListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter = ClubAdapter(this)
        binding.clubList.adapter = listAdapter

        resetList()
    }

    private fun onComplete(list: List<Club>)
    {
        val infiniteScrollListener = InfiniteScrollListener()
        infiniteScrollListener.setItemSizeGettr {
            listAdapter.itemCount
        }
        infiniteScrollListener.setOnLoadingStart {
            if(!alreadyInit || this.cantReadMore)
                return@setOnLoadingStart
            clubInterface.getClubListLimited(false, pageItemCount, onComplete = { list, cantReadMore ->
                this.cantReadMore = cantReadMore
                listAdapter.addClubList(list as ArrayList<Club>)
                infiniteScrollListener.loadingEnd()
            })
        }

        binding.clubList.addOnScrollListener(infiniteScrollListener)

        init(list)
    }

    private fun resetList()
    {
        clubInterface.getClubListLimited(true, pageItemCount) { list, cantReadMore ->
            this.cantReadMore = cantReadMore
            onComplete(list)
        }
    }

    private fun init(list: List<Club>)
    {
        binding.clubList.scrollToPosition(0)

        listAdapter.setClubList(list as ArrayList<Club>)

        loadingEnd()

        binding.inputClubName.setOnKeyListener { _, keyCode, keyEvent ->
            if(keyEvent.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                startSearchClub()
                return@setOnKeyListener true
            }
            return@setOnKeyListener false
        }

        binding.btnReset.setOnClickListener {
            binding.btnReset.visibility = View.GONE

            hideKeyboard(binding.inputClubName)

            binding.inputClubName.text.clear()

            loadingStart()
            resetList()
        }

        binding.btnSubmit.setOnClickListener {
            startSearchClub()
        }

        alreadyInit = true
    }

    private fun hideKeyboard(view: View)
    {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun loadingStart()
    {
        binding.loadingBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.loadingBackground.visibility = View.GONE
    }

    private fun startSearchClub()
    {
        if(binding.inputClubName.text.isBlank())
            return

        hideKeyboard(binding.inputClubName)

        loadingStart()

        val name = binding.inputClubName.text.toString()
        searchClub(name)

        binding.btnReset.visibility = View.VISIBLE
    }

    private var searchCount = 0

    private fun searchClub(name: String)
    {
        searchCount = 0
        this.cantReadMore = false
        searchClub(name, true, 5L)
    }

    private fun searchClub(name: String, start: Boolean, limit: Long)
    {
        if(this.cantReadMore || searchCount >= limit) {
            loadingEnd()
            return
        }

        clubInterface.getClubListLimited(start, 5L) { list, cantReadMore ->
            this.cantReadMore = cantReadMore

            val searchedLiist = list.filter {
                it.name.lowercase().contains(name.lowercase())
            }
            searchCount += searchedLiist.size

            if(start)
                init(searchedLiist)
            else
                listAdapter.addClubList(searchedLiist as ArrayList<Club>)

            searchClub(name, start, limit)
        }
    }

    inner class ClubAdapter(private val context: Context): RecyclerView.Adapter<ClubAdapter.ViewHolder>()
    {
        private var list = ArrayList<Club>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
        {
            val view = LayoutInflater.from(context).inflate(R.layout.club_list_item, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int)
        {
            holder.bind(list[position])
        }

        override fun getItemCount(): Int = list.size

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
        {
            private val clubTitle = view.findViewById<TextView>(R.id.club_title)
            private val clubImg = view.findViewById<ImageView>(R.id.club_img)

            fun bind(item: Club)
            {
                clubTitle.text = item.name

                val manageDialog = ManageDialog()
                manageDialog.setOnMenuItemClickListener(object: ManageDialog.OnMenuButtonClickListener {
                    override fun onFirstItemClick(manageDialog: ManageDialog)
                    {
                        manageDialog.dismiss()
                    }

                    override fun onSecondItemClick(manageDialog: ManageDialog)
                    {
                        val intent = Intent(context, RequestManageActivity::class.java)
                        intent.putExtra("club_id", item.id)
                        startActivity(intent)

                        manageDialog.dismiss()
                    }

                    override fun onThirdItemClick(manageDialog: ManageDialog)
                    {
                        manageDialog.dismiss()
                    }
                })

                this.itemView.setOnClickListener {
                    loadingStart()

                    val user = auth.getUserInfo()!!

                    clubInterface.checkIsActivated(item.id) { isActivated ->
                        clubInterface.checkClubMember(user.id!!, item.id) { isMember, permissionLevel ->
                            if(isMember) {
                                if(isActivated) {
                                    val intent = Intent(context, ClubActivity::class.java)
                                    intent.putExtra("club_id", item.id)
                                    context.startActivity(intent)
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
                                clubInterface.checkProcessingRequest(user, item) { isProcessing ->
                                    if(!isProcessing) {
                                        val dialog = RequestDialog()

                                        dialog.setOnMenuItemClickListener(object: RequestDialog.OnMenuButtonClickListener {
                                            override fun onSubmit(requestDialog: RequestDialog, description: String)
                                            {
                                                requestDialog.dismiss()
                                                loadingStart()
                                                clubInterface.sendRequest(user, description, Date(), item, onComplete = {
                                                    loadingEnd()
                                                }, onFailed = {
                                                    //show error dialog
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
            }
        }

        @SuppressLint("NotifyDataSetChanged")
        fun setClubList(list: ArrayList<Club>)
        {
            this.list = list
            notifyDataSetChanged()
        }

        fun addClubList(list: ArrayList<Club>)
        {
            val size = this.list.size
            this.list.addAll(list)
            notifyItemRangeInserted(size, this.list.size)
        }
    }
}