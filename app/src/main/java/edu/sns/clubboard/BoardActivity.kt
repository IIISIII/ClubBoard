package edu.sns.clubboard

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
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

    private val boardInterface: BoardInterface = FBBoard()

    private lateinit var listAdapter: PostAdapter

    private var board: Board? = null

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
                boardInterface.getPostListLimited(board!!, true, 30, onComplete = { list ->
                    listAdapter.setPostList(list as ArrayList<Post>)
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
        binding.boardList.adapter = listAdapter

        boardInterface.getPostListLimited(board, true, 30, onComplete = {
            listAdapter.setPostList(it as ArrayList<Post>)
        })
    }

    private fun onFailed()
    {
        finish()
    }
}