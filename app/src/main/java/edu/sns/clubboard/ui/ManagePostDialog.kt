package edu.sns.clubboard.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import edu.sns.clubboard.WriteActivity
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.DialogManagePostBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ManagePostDialog(val activity: AppCompatActivity, val user: User, val board: Board, val post: Post): DialogFragment()
{
    private var _binding: DialogManagePostBinding? = null
    private val binding get() = _binding!!

    private val clubInterface = FBClub.getInstance()

    private val boardInterface = FBBoard()

    private var onDeleted: (() -> Unit)? = null

    private var resultLauncher: ActivityResultLauncher<Intent>? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = DialogManagePostBinding.inflate(inflater, container, false)
        val view = binding.root

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)

        binding.btnModify.setOnClickListener {
            val intent = Intent(activity, WriteActivity::class.java)
            intent.putExtra("board_id", board.id)
            intent.putExtra("post_id", post.id)
            resultLauncher?.launch(intent)

            this.dismiss()
        }

        binding.btnDelete.setOnClickListener {
            this.isCancelable = false

            binding.progressBar.visibility = View.VISIBLE
            binding.manageLayout.visibility = View.GONE

            boardInterface.deletePost(board, post, user) {
                onDeleted?.invoke()
                this.dismiss()
            }
        }

        if(post.authorId != user.id)
            binding.btnModify.visibility = View.GONE

        if(post.authorId == user.id) {
            binding.progressBar.visibility = View.GONE
            binding.manageLayout.visibility = View.VISIBLE
        }
        else {
            if(user.isAdmin) {
                binding.progressBar.visibility = View.GONE
                binding.manageLayout.visibility = View.VISIBLE
            }
            else if(board.parent != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val level = clubInterface.getPermissionLevel(user, board.parent!!)
                    activity.runOnUiThread {
                        if(level != null && level <= User.PERMISSION_LEVEL_MANAGER) {
                            binding.progressBar.visibility = View.GONE
                            binding.manageLayout.visibility = View.VISIBLE
                        }
                        else
                            dismiss()
                    }
                }
            }
            else
                this.dismiss()
        }

        return view
    }

    override fun onDestroy()
    {
        super.onDestroy()
        _binding = null
    }

    fun show(activity: AppCompatActivity, launcher: ActivityResultLauncher<Intent>, onDeleted: () -> Unit)
    {
        if(board.readOnly)
            return

        this.onDeleted = onDeleted
        this.resultLauncher = launcher

        this.show(activity.supportFragmentManager, "ManagePostDialog")
    }
}