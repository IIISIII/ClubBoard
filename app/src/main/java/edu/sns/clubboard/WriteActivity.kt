package edu.sns.clubboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityWriteBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class WriteActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityWriteBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private var boardInterface: BoardInterface = FBBoard()

    private var board: Board? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val boardId = intent.getStringExtra("board_id")
        boardId?.let {
            boardInterface.getBoardData(boardId, onSuccess = {
                board = it
                binding.writeProgressBackground.visibility = View.GONE
            }, onFailed = {
                finish()
            })
        }

        binding.completeBtn.setOnClickListener {
            board?.let {
                val user = auth.getUserInfo()

                val title = binding.postTitle.text.toString()
                val text = binding.postText.text.toString()

                val post = Post("", title, text, Date(), "users/${user!!.id!!}")

                boardInterface.writePost(it, post, user, onComplete = {
                    setResult(RESULT_OK)
                    finish()
                })
            }
        }
    }
}