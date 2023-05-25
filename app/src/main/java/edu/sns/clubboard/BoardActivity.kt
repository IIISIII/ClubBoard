package edu.sns.clubboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityBoardBinding
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.ui.InfiniteScrollListener
import edu.sns.clubboard.ui.PostAdapter

class BoardActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityBoardBinding.inflate(layoutInflater)
    }

    private val boardInterface: BoardInterface = FBBoard()

    private val auth = FBAuthorization.getInstance()

    private val pageItemCount = 10L

    private lateinit var listAdapter: PostAdapter

    private var board: Board? = null

    private var alreadyInit = false

    private var cantReadMore = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.boardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val boardId = intent.getStringExtra("board_id")

        boardId?.let {
            boardInterface.getBoardData(it, onSuccess = { board ->
                onSuccess(board)
            }, onFailed = {
                onFailed()
            })
        } ?: finish()

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode == Activity.RESULT_OK) {
                boardInterface.getPostListLimited(board!!, true, pageItemCount, onComplete = { list, cantReadMore ->
                    this.cantReadMore = cantReadMore
                    init(board!!, list)
                })
            }
        }

        binding.writeBtn.setOnClickListener {
            board?.let { board ->
                val intent = Intent(this, WriteActivity::class.java)
                intent.putExtra("board_id", board.id)
                resultLauncher.launch(intent)
            }
        }
    }

    private fun onSuccess(board: Board)
    {
        this.board = board

        supportActionBar?.title = board.name

        listAdapter = PostAdapter(board)
        listAdapter.setOnItemClick { post, board ->
            val intent = Intent(this@BoardActivity, PostActivity::class.java)
            intent.putExtra("target_club", post.targetClubId)
            intent.putExtra("board_name", board.name)
            intent.putExtra("board_id", board.id)
            intent.putExtra("post_id", post.id)
            startActivity(intent)
        }
        listAdapter.setOnItemLongClick { post, board ->

        }
        binding.boardList.adapter = listAdapter

        val infiniteScrollListener = InfiniteScrollListener()
        infiniteScrollListener.setItemSizeGettr {
            listAdapter.itemCount
        }
        infiniteScrollListener.setOnLoadingStart {
            if(!alreadyInit || this.cantReadMore)
                return@setOnLoadingStart
            boardInterface.getPostListLimited(board, false, pageItemCount, onComplete = { list, cantReadMore ->
                this.cantReadMore = cantReadMore
                listAdapter.addPostList(list as ArrayList<Post>)
                infiniteScrollListener.loadingEnd()
            })
        }

        binding.boardList.addOnScrollListener(infiniteScrollListener)

        boardInterface.getPostListLimited(board, true, pageItemCount, onComplete = { list, cantReadMore ->
            init(board, list)
        })
    }

    private fun onFailed()
    {
        //show board not found message
        finish()
    }

    private fun init(board: Board, list: List<Post>)
    {
        binding.boardList.scrollToPosition(0)

        listAdapter.setPostList(list as ArrayList<Post>)

        boardInterface.checkWritePermission(auth.getUserInfo()!!, board, onComplete = {
            binding.writeBtn.visibility = if(it) View.VISIBLE else View.GONE
        })

        alreadyInit = true
    }
}