package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityWriteBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import java.util.Date

class WriteActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityWriteBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private lateinit var boardInterface: BoardInterface

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val boardId = intent.getStringExtra("board_id")
        boardId?.let {
            boardInterface = FBBoard(it)
        }

        binding.completeBtn.setOnClickListener {
            val user = auth.getUserInfo()

            val title = binding.postTitle.text.toString()
            val text = binding.postText.text.toString()

            val post = Post(title, text, Date(), "users/${user!!.id!!}")

            boardInterface.writePost(post, user, onComplete = {
                setResult(RESULT_OK)
                finish()
            })
        }
    }
}