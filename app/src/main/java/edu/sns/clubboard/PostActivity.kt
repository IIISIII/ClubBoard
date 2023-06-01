package edu.sns.clubboard

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.transition.Scene
import android.transition.TransitionManager
import android.widget.Button
import android.widget.ImageView
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityPostBinding
import edu.sns.clubboard.ui.LoadingDialog
import edu.sns.clubboard.ui.RequestDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class PostActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }

    private val clubInterface = FBClub.getInstance()

    private val boardInterface = FBBoard()

    private val auth = FBAuthorization.getInstance()

    private val fileManager = FBFileManager.getInstance()

    private val loadingDialog = LoadingDialog()

    private lateinit var scene1: Scene
    private lateinit var scene2: Scene

    private var targetClubId: String? = null

    @SuppressLint("SimpleDateFormat")
    private var dateFormat = SimpleDateFormat("yyyy.MM.dd")


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.postToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingStart()

        targetClubId = intent.getStringExtra("target_club")
        val boardName = intent.getStringExtra("board_name").toString()
        val boardId = intent.getStringExtra("board_id").toString()
        val postId = intent.getStringExtra("post_id").toString()

        supportActionBar?.title = boardName

        scene1 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_post_normal, this)
        scene2 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_post_recruit, this)

        boardInterface.readPost(boardId, postId, onComplete = {
            init(it)
        }, onFailed = {
            loadingEndWithError(resources.getString(R.string.error_post_not_found))
        })
    }

    private fun loadingStart()
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        binding.sceneRoot.visibility = View.VISIBLE
        loadingDialog.dismiss()
    }

    private fun loadingEndWithError(message: String)
    {
        loadingDialog.dismiss()

        //show error dialog
    }

    private fun init(post: Post)
    {
        when(post.postType) {
            Post.TYPE_NORMAL -> {
                TransitionManager.go(scene1)

                initNormalPost(post)
            }
            else -> {
                TransitionManager.go(scene2)

                initRecruitPost(post)
            }
        }
    }

    private fun initNormalPost(post: Post)
    {
        val userImg = binding.sceneRoot.findViewById<ImageView>(R.id.user_icon)
        val userName = binding.sceneRoot.findViewById<TextView>(R.id.user_name)
        val postDate = binding.sceneRoot.findViewById<TextView>(R.id.post_date)
        val postTitle = binding.sceneRoot.findViewById<TextView>(R.id.post_title)
        val postContent = binding.sceneRoot.findViewById<TextView>(R.id.post_content)
        val postImg = binding.sceneRoot.findViewById<ImageView>(R.id.post_img)

        auth.getUserInfo(post.authorId) {
            if(it != null) {
                it.imagePath?.run {
                    fileManager.getImage(this) { img ->
                        if(img != null)
                            userImg.setImageBitmap(img)
                    }
                }
                userName.text = it.nickname
            }
            else
                userName.text = "(삭제된 사용자)"

            if(post.imgPath != null) {
                postImg.visibility = View.VISIBLE

                fileManager.getImage(post.imgPath) { img ->
                    if(img != null)
                        postImg.setImageBitmap(img)
                }
            }

            postDate.text = dateFormat.format(post.date)
            postTitle.text = post.title
            postContent.text = post.text

            loadingEnd()
        }
    }

    private fun initRecruitPost(post: Post)
    {
        val clubImg = binding.sceneRoot.findViewById<ImageView>(R.id.club_img)
        val clubName = binding.sceneRoot.findViewById<TextView>(R.id.club_name)
        val clubDescription = binding.sceneRoot.findViewById<TextView>(R.id.club_description)
        val submitButton = binding.sceneRoot.findViewById<Button>(R.id.submit_btn)

        clubInterface.getClubData(targetClubId!!, onComplete = { club: Club, boards: List<Board> ->
            if(club.imgPath != null) {
                fileManager.getImage(club.imgPath!!) { img ->
                    if(img != null)
                        clubImg.setImageBitmap(img)
                }
            }

            clubName.text = club.name
            clubDescription.text = club.description

            val user = auth.getUserInfo()!!

            submitButton.setOnClickListener {
                val dialog = RequestDialog()

                dialog.setOnMenuItemClickListener(object: RequestDialog.OnMenuButtonClickListener {
                    override fun onSubmit(requestDialog: RequestDialog, description: String)
                    {
                        requestDialog.dismiss()
                        loadingStart()
                        clubInterface.sendRequest(user, description, Date(), club, onComplete = {
                            submitButton.isEnabled = false
                            submitButton.text = resources.getString(R.string.text_already_request)
                            loadingEnd()
                        }, onFailed = {
                            //show error dialog
                            finish()
                        })
                    }

                    override fun onCancel(requestDialog: RequestDialog)
                    {
                        requestDialog.dismiss()
                    }
                })
                dialog.show(supportFragmentManager, "RequestDialog")
            }

            CoroutineScope(Dispatchers.IO).launch {
                val isClubMember = clubInterface.isClubMember(user, club)
                runOnUiThread {
                    if(isClubMember)
                        submitButton.visibility = View.GONE
                    else {
                        clubInterface.checkProcessingRequest(user, club, onComplete = {
                            submitButton.isEnabled = !it
                            submitButton.text = resources.getString(if(it) R.string.text_already_request else R.string.text_join_request)
                        })
                    }
                    loadingEnd()
                }
            }
        }, onFailed = {
            finish()
        })
    }
}