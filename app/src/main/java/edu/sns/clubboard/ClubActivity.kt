package edu.sns.clubboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityClubBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.port.ClubInterface
import edu.sns.clubboard.ui.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ClubActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityClubBinding.inflate(layoutInflater)
    }

    private val clubInterface: ClubInterface = FBClub.getInstance()

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private val fileManager = FBFileManager.getInstance()

    private var loadingDialog = LoadingDialog()

    private var clubId: String? = null

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    private lateinit var launcher: ActivityResultLauncher<Intent>

    private var permissionLevel: Long? = null

    private var isClubDataChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.clubToolbar)

        binding.clubTitle.isSelected = true

        viewPager = binding.boardViewPager
        tabLayout = binding.boardTab

        launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == RESULT_OK) {
                isClubDataChanged = true
                init(clubId!!)
            }
        }

        clubId = intent.getStringExtra("club_id")

        clubId?.let {
            init(it)
        } ?: finish()
    }

    override fun finish()
    {
        if(isClubDataChanged)
            setResult(RESULT_OK)
        super.finish()
    }

    private fun init(clubId: String)
    {
        loadingStart()
        clubInterface.getClubData(clubId, onComplete = { club, boardList ->
            initData(club, boardList)
        }, onFailed = {
            finish()
        })
    }

    private fun initData(club: Club, boardList: List<Board>)
    {
        binding.clubTitle.text = club.name

        club.imgPath?.let {
            fileManager.getImage(it) { img ->
                if(img != null)
                    binding.clubImg.setImageBitmap(img)
                else
                    binding.clubImg.setImageResource(R.mipmap.empty_club_logo)
            }
        }

        val user = auth.getUserInfo()!!

        val manageDialog = ManageDialog()
        manageDialog.setOnMenuItemClickListener(object: ManageDialog.OnMenuButtonClickListener {
            override fun onFirstItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@ClubActivity, ManageMemberActivity::class.java)
                intent.putExtra("club_id", club.id)
                launcher.launch(intent)

                manageDialog.dismiss()
            }

            override fun onSecondItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@ClubActivity, RequestManageActivity::class.java)
                intent.putExtra("club_id", club.id)
                startActivity(intent)

                manageDialog.dismiss()
            }

            override fun onThirdItemClick(manageDialog: ManageDialog)
            {
                val intent = Intent(this@ClubActivity, ClubProfileActivity::class.java)
                intent.putExtra("club_id", club.id)
                launcher.launch(intent)

                manageDialog.dismiss()
            }
        })

        CoroutineScope(Dispatchers.IO).launch {
            permissionLevel = clubInterface.getPermissionLevel(user, club)
            runOnUiThread {
                if(permissionLevel == User.PERMISSION_LEVEL_MASTER) {
                    binding.manageBtn.visibility = View.VISIBLE
                    binding.manageBtn.setOnClickListener {
                        manageDialog.show(supportFragmentManager, "ManageDialog")
                    }
                }
                else if(permissionLevel == null) {

                    finish()
                    return@runOnUiThread
                }
                else
                    binding.manageBtn.visibility = View.GONE
                loadingEnd()
            }
        }

        viewPager.adapter = ViewPagerAdapter(this, supportFragmentManager, lifecycle, user, club, boardList)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val board = boardList[position]
            tab.text = board.name
        }.attach()
    }

    private fun loadingStart()
    {
        binding.mainLayout.visibility = View.GONE
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        binding.mainLayout.visibility = View.VISIBLE
        loadingDialog.dismiss()
    }

    class ViewPagerAdapter(val activity: AppCompatActivity, fragmentManager: FragmentManager, lifecycle: Lifecycle, val user: User, val club: Club, val list: List<Board>): FragmentStateAdapter(fragmentManager, lifecycle)
    {
        override fun getItemCount(): Int
        {
            return list.size
        }

        override fun createFragment(position: Int): Fragment
        {
            return TabFragment(activity, user, club, list[position])
        }

        class TabFragment(val activity: AppCompatActivity, val user: User, val club: Club, private val board: Board): Fragment(R.layout.board_fragment)
        {
            private val pageItemCount = 10L

            private var boardInterface = FBBoard()

            private lateinit var writeBtn: Button

            private lateinit var postList: RecyclerView

            private lateinit var postAdapter: PostAdapter

            private var alreadyInit = false

            private var cantReadMore = false

            private lateinit var resultLauncher: ActivityResultLauncher<Intent>


            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                writeBtn = view.findViewById(R.id.write_post_btn)
                postList = view.findViewById(R.id.post_list)

                postAdapter = PostAdapter(board)
                postAdapter.setOnItemClick { post, board ->
                    val intent = Intent(context, PostActivity::class.java)
                    intent.putExtra("target_club", club.id)
                    intent.putExtra("board_name", board.name)
                    intent.putExtra("board_id", board.id)
                    intent.putExtra("post_id", post.id)
                    startActivity(intent)
                }
                postAdapter.setOnItemLongClick { post, board ->
                    checkAndShowManageDialog(post, board)
                }


                val infiniteScrollListener = InfiniteScrollListener()
                infiniteScrollListener.setItemSizeGettr {
                    postAdapter.itemCount
                }
                infiniteScrollListener.setOnLoadingStart {
                    if(!alreadyInit || this.cantReadMore)
                        return@setOnLoadingStart
                    boardInterface.getPostListLimited(board, false, pageItemCount, onComplete = { list, cantReadMore ->
                        this.cantReadMore = cantReadMore
                        postAdapter.addPostList(list as ArrayList<Post>)
                    })
                }

                postList.apply {
                    adapter = postAdapter
                    addOnScrollListener(infiniteScrollListener)
                    addItemDecoration(DividerItemDecoration(context, LinearLayout.VERTICAL))
                }

                resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                    if (activityResult.resultCode == Activity.RESULT_OK)
                        init(board)
                }

                writeBtn.setOnClickListener {
                    val intent = Intent(context, WriteActivity::class.java)
                    intent.putExtra("board_id", board.id)
                    resultLauncher.launch(intent)
                }

                init(board)
            }

            private fun init(board: Board)
            {
                postList.scrollToPosition(0)

                boardInterface.getPostListLimited(board, true, pageItemCount, onComplete = { list, cantReadMore ->
                    this.cantReadMore = cantReadMore

                    postAdapter.setPostList(list as ArrayList<Post>)

                    boardInterface.checkWritePermission(user, board) {
                        writeBtn.visibility = if(it) View.VISIBLE else View.GONE
                    }

                    alreadyInit = true
                })
            }

            private fun checkAndShowManageDialog(post: Post, board: Board)
            {
                val managePostDialog = ManagePostDialog(activity, user, board, post)

                managePostDialog.show(activity, resultLauncher) {
                    init(board)
                }
            }
        }
    }
}