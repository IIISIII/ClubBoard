package edu.sns.clubboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.transition.Scene
import android.transition.TransitionManager
import android.widget.Button
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.databinding.ActivityPostBinding
import edu.sns.clubboard.ui.RequestDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

class PostActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }

    private val clubInterface = FBClub.getInstance()

    private val boardInterface = FBBoard()

    private val auth = FBAuthorization.getInstance()

    private lateinit var scene1: Scene
    private lateinit var scene2: Scene

    private var targetClubId: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.postToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        loadingStart(resources.getString(R.string.text_load_post))

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
            //read failed dialog
            //finish()
            loadingEndWithError(resources.getString(R.string.error_post_not_found))
        })
    }

    private fun loadingStart(message: String)
    {
        binding.loadingText.text = message
        binding.loadingBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.sceneRoot.visibility = View.VISIBLE
        binding.loadingBackground.visibility = View.GONE
    }

    private fun loadingEndWithError(message: String)
    {
        binding.loadingProgress.visibility = View.GONE

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

        loadingEnd()
    }

    private fun initRecruitPost(post: Post)
    {
        val clubName = binding.sceneRoot.findViewById<TextView>(R.id.club_name)
        val clubDescription = binding.sceneRoot.findViewById<TextView>(R.id.club_description)
        val submitButton = binding.sceneRoot.findViewById<Button>(R.id.submit_btn)

        clubInterface.getClubData(targetClubId!!, onComplete = { club: Club, boards: List<Board> ->
            clubName.text = club.name
            clubDescription.text = post.text

            val user = auth.getUserInfo()!!

            submitButton.setOnClickListener {
                val dialog = RequestDialog()

                dialog.setOnMenuItemClickListener(object: RequestDialog.OnMenuButtonClickListener {
                    override fun onSubmit(requestDialog: RequestDialog, description: String)
                    {
                        requestDialog.dismiss()
                        loadingStart(resources.getString(R.string.text_send_request))
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