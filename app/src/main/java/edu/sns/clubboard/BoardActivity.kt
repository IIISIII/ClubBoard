package edu.sns.clubboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityBoardBinding
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.ui.PostAdapter

class BoardActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityBoardBinding.inflate(layoutInflater)
    }

    private lateinit var boardInterface: BoardInterface

    private lateinit var listAdapter: PostAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.boardToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter = PostAdapter()
        binding.boardList.adapter = listAdapter

        val boardId = intent.getStringExtra("board_id")
        boardId?.let {
            boardInterface = FBBoard(it)
            boardInterface.load(onSuccess = { board -> onSuccess(board) }, onFailed = { onFailed() })
        } ?: finish()

        val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            if(activityResult.resultCode == Activity.RESULT_OK) {
                boardInterface.getPostListLimited(true, 30, onComplete = { list ->
                    listAdapter.setPostList(list as ArrayList<Post>)
                })
            }
        }

        binding.writeBtn.setOnClickListener {
            boardInterface.getBoardData()?.let { board ->
                val intent = Intent(this, WriteActivity::class.java)
                intent.putExtra("board_id", board.id)
                resultLauncher.launch(intent)
            }
        }
    }

    private fun onSuccess(board: Board)
    {
        supportActionBar?.title = board.name

        boardInterface.getPostListLimited(true, 30, onComplete = {
            listAdapter.setPostList(it as ArrayList<Post>)
        })
    }

    private fun onFailed()
    {
        finish()
    }
}