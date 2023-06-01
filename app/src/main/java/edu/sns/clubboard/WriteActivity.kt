package edu.sns.clubboard

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.chooser.ImageChooser
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.PathMaker
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityWriteBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.ui.LoadingDialog
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

    private val loadingDialog = LoadingDialog()

    private val fileManager = FBFileManager.getInstance()

    private var boardInterface: BoardInterface = FBBoard()

    private var board: Board? = null

    private lateinit var imageChooser: ImageChooser

    private var selectedUri: Uri? = null

    private var postId: String? = null

    private var postImgPath: String? = null

    private var postDate: Date? = null

    private lateinit var date: Date


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.writeToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingStart()

        imageChooser = ImageChooser(this) {
            selectedUri = it

            val bitmap = fileManager.getImageFromUri(this, it)
            binding.postImg.setImageBitmap(bitmap)
        }

        val boardId = intent.getStringExtra("board_id")
        postId = intent.getStringExtra("post_id")
        boardId?.let {
            boardInterface.getBoardData(boardId, onSuccess = {
                board = it
                if(postId == null)
                    loadingEnd()
                else {
                    binding.postTitle.isEnabled = false

                    boardInterface.readPost(it.id, postId!!, onComplete = { post ->
                        binding.postTitle.editText?.setText(post.title)
                        binding.postText.setText(post.text)

                        postDate = post.date
                        postImgPath = post.imgPath

                        if(post.imgPath != null) {
                            fileManager.getImage(post.imgPath) { img ->
                                if(img != null)
                                    binding.postImg.setImageBitmap(img)
                                loadingEnd()
                            }
                        }
                        else
                            loadingEnd()
                    }, onFailed = {
                        finish()
                    })
                }
            }, onFailed = {
                finish()
            })
        }

        binding.inputPostImg.setOnClickListener {
            imageChooser.launch()
        }

        binding.completeBtn.setOnClickListener {
            val title = binding.postTitle.editText?.text.toString().trim()
            binding.postTitle.isErrorEnabled = title.isEmpty()
            if(title.isEmpty()) {
                binding.postTitle.error = getString(R.string.error_post_no_title)
                return@setOnClickListener
            }

            board?.run {
                loadingStart()

                date = Date()

                boardInterface.checkWritePermission(auth.getUserInfo()!!, this, onComplete = { isWriteable ->
                    if(isWriteable) {
                        if(selectedUri != null)
                            fileManager.uploadFile(this@WriteActivity, selectedUri!!, postImgPath ?: PathMaker.makeForPost(title, postDate ?: date, this)) {
                                if(it != null)
                                    uploadPost(title, this, it)
                                else {
                                    //alert
                                    loadingEnd()
                                }
                            }
                        else
                            uploadPost(title, this, postImgPath)
                    }
                    else {
                        //alert
                        loadingEnd()
                    }
                })
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    private fun uploadPost(title: String, board: Board, imgPath: String?)
    {
        val user = auth.getUserInfo()

        val text = binding.postText.text.toString()

        val post = Post(postId ?: "", title, text, postDate ?: date, user!!.id!!, imgPath = imgPath)

        boardInterface.writePost(board, post, user) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun loadingStart()
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
    }
}