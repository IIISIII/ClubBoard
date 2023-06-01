package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityMainBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.ui.PreviewAdapter

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val auth : AuthInterface = FBAuthorization.getInstance()

    private val freeBoardInterface = FBBoard()

    private val recruitBoardInterface = FBBoard()

    private val freeBoardId = "0"

    private val recruitBoardId = "1"

    private var freeAdapter: PreviewAdapter? = null

    private var recruitAdapter: PreviewAdapter? = null


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        if(!auth.isLogin())
            moveToLoginActivity()
        else {
            auth.getUserInfo(null) {
                if(it != null) {
                    auth.checkAuthenticated(it) { isAuthenticated ->
                        if (!isAuthenticated)
                            moveToAuthenticateActivity()
                        else
                            init()
                    }
                }
                else
                    moveToLoginActivity()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId) {
            R.id.profile -> {
                val user = auth.getUserInfo()!!

                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra(UserProfileActivity.USER_ID, user.id)
                startActivity(intent)

                true
            }
            else -> false
        }
    }

    override fun onDestroy()
    {
        super.onDestroy()

        FBAuthorization.remove()
    }

    override fun onResume() {
        super.onResume()

        previewInit()
    }

    override fun onPause() {
        super.onPause()

        previewPause()
    }

    private fun init()
    {
        binding.searchClub.setOnClickListener {
            val intent = Intent(this, ClubListActivity::class.java)
            startActivity(intent)
        }

        binding.myClubBtn.setOnClickListener {
            val intent = Intent(this, MyClubActivity::class.java)
            startActivity(intent)
        }

        binding.promoteBtn.setOnClickListener {
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_id", recruitBoardId)
            startActivity(intent)
        }

        binding.makeClubBtn.setOnClickListener {
            val intent = Intent(this, MakeClubActivity::class.java)
            startActivity(intent)
        }

        binding.freeBoardBtn.setOnClickListener {
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_id", freeBoardId)
            startActivity(intent)
        }
    }

    private fun previewInit()
    {
        freeBoardInterface.getBoardData(freeBoardId, onSuccess = {
            binding.previewBox1Title.text = it.name

            if(freeAdapter == null) {
                freeAdapter = PreviewAdapter(it)
                binding.previewBox1List.apply {
                    adapter = freeAdapter
                }
                freeAdapter?.setOnItemClick { post, board ->
                    val intent = Intent(this, PostActivity::class.java)
                    intent.putExtra("target_club", post.targetClubId)
                    intent.putExtra("board_name", board.name)
                    intent.putExtra("board_id", board.id)
                    intent.putExtra("post_id", post.id)
                    startActivity(intent)
                }
            }

            freeBoardInterface.registerBoardPreview(freeBoardId, 5) { list ->
                binding.loadingBackground.visibility = View.GONE
                binding.previewList1.visibility = View.VISIBLE

                freeAdapter?.setPostList(list as ArrayList<Post>)

                binding.previewBox1Empty.visibility = if(list.isEmpty())
                        View.VISIBLE
                    else
                        View.GONE
            }
        }, onFailed = {})

        recruitBoardInterface.getBoardData(recruitBoardId, onSuccess = {
            binding.previewBox2Title.text = it.name

            if(recruitAdapter == null) {
                recruitAdapter = PreviewAdapter(it)
                binding.previewBox2List.apply {
                    adapter = recruitAdapter
                }
                recruitAdapter?.setOnItemClick { post, board ->
                    val intent = Intent(this, PostActivity::class.java)
                    intent.putExtra("target_club", post.targetClubId)
                    intent.putExtra("board_name", board.name)
                    intent.putExtra("board_id", board.id)
                    intent.putExtra("post_id", post.id)
                    startActivity(intent)
                }
            }

            recruitBoardInterface.registerBoardPreview(recruitBoardId, 5) { list ->
                binding.loadingBackground.visibility = View.GONE
                binding.previewList2.visibility = View.VISIBLE

                recruitAdapter?.setPostList(list as ArrayList<Post>)

                binding.previewBox2Empty.visibility = if(list.isEmpty())
                    View.VISIBLE
                else
                    View.GONE
            }
        }, onFailed = {})
    }

    private fun previewPause()
    {
        freeBoardInterface.unregisterBoardPreview()
        recruitBoardInterface.unregisterBoardPreview()
    }

    private fun moveToLoginActivity()
    {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun moveToAuthenticateActivity()
    {
        startActivity(Intent(this, AuthenticateActivity::class.java))
        finish()
    }
}