package edu.sns.clubboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityClubBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.port.ClubInterface
import edu.sns.clubboard.ui.PostAdapter

class ClubActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityClubBinding.inflate(layoutInflater)
    }

    private val clubInterface: ClubInterface = FBClub.getInstance()

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.clubToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewPager = binding.boardViewPager
        tabLayout = binding.boardTab

        intent.getStringExtra("club_id")?.let {
            clubInterface.load(it, onComplete = { club, boardList ->
                clubInterface.getClubMembers(club, onComplete = {}, onFailed = {})
                init(club, boardList)
            }, onFailed = {
                finish()
            })
        }
    }

    private fun init(club: Club, boardList: List<Board>)
    {
        supportActionBar?.title = club.name

        viewPager.adapter = ViewPagerAdapter(supportFragmentManager, lifecycle, auth.getUserInfo()!!, club, boardList)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            val board = boardList[position]
            tab.text = board.name
        }.attach()
    }

    class ViewPagerAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle, val user: User, val club: Club, val list: List<Board>): FragmentStateAdapter(fragmentManager, lifecycle)
    {
        override fun getItemCount(): Int
        {
            return list.size
        }

        override fun createFragment(position: Int): Fragment
        {
            return TabFragment(user, club, list[position])
        }

        class TabFragment(val user: User, val club: Club, private val board: Board): Fragment(R.layout.board_fragment)
        {
            private val clubInterface: ClubInterface = FBClub.getInstance()

            private lateinit var boardInterface: BoardInterface

            private lateinit var writeBtn: Button

            private lateinit var postList: RecyclerView

            private lateinit var postAdapter: PostAdapter

            override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)

                writeBtn = view.findViewById(R.id.write_post_btn)
                postList = view.findViewById(R.id.post_list)

                postAdapter = PostAdapter(board)
                postList.adapter = postAdapter

                val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
                    if (activityResult.resultCode == Activity.RESULT_OK) {
                        boardInterface.getPostListLimited(true, 30, onComplete = { list ->
                            init(list)
                        })
                    }
                }

                writeBtn.setOnClickListener {
                    val intent = Intent(context, WriteActivity::class.java)
                    intent.putExtra("board_id", board.id)
                    resultLauncher.launch(intent)
                }

                boardInterface = FBBoard(board)
                boardInterface.getPostListLimited(true, 30, onComplete = {
                    init(it)
                })
            }

            private fun init(list: List<Post>)
            {
                postAdapter.setPostList(list as ArrayList<Post>)

                boardInterface.checkWritePermission(user, board, onComplete = {
                    writeBtn.visibility = if(it) View.VISIBLE else View.GONE
                })
            }
        }
    }
}