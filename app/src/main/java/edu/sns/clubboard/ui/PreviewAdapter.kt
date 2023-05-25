package edu.sns.clubboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.R
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.data.Post
import edu.sns.clubboard.data.User
import java.text.SimpleDateFormat

class PreviewAdapter: RecyclerView.Adapter<PreviewAdapter.ViewHolder>()
{
    private val auth = FBAuthorization.getInstance()

    private val clubInterface = FBClub.getInstance()

    private var list = ArrayList<Board>()

    private var onLoadingStart: (() -> Unit)? = null
    private var onLoadingEnd: ((havePermission: Boolean, post: Post?, board: Board?) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.preview_item, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun setList(boardList: ArrayList<Board>)
    {
        list = boardList
        notifyDataSetChanged()
    }

    fun setOnLoadingStart(loadingStart: () -> Unit)
    {
        onLoadingStart = loadingStart
    }

    fun setOnLoadingEnd(loadingEnd: (havePermission: Boolean, post: Post?, board: Board?) -> Unit)
    {
        onLoadingEnd = loadingEnd
    }

    inner class ViewHolder(view: View, val context: Context): RecyclerView.ViewHolder(view)
    {
        val boardInterface = FBBoard()

        val boardTitle = view.findViewById<TextView>(R.id.preview_board_title)
        val clubTitle = view.findViewById<TextView>(R.id.preview_club_title)
        val loadingBar = view.findViewById<ProgressBar>(R.id.preview_loading)
        val emptyText = view.findViewById<TextView>(R.id.preview_empty_text)
        val postList = view.findViewById<LinearLayout>(R.id.preview_post_list)

        val dateFormat = SimpleDateFormat("MM/dd")

        fun bind(board: Board)
        {
            boardTitle.text = board.name
            boardInterface.getParent(board, onSuccess = {
                init(board, it)
            }, onFailed = {
                init(board, null)
            })
        }

        private fun init(board: Board, club: Club?)
        {
            clubTitle.text = club?.name ?: ""

            val user = auth.getUserInfo()!!

            if(club != null) {
                clubInterface.checkClubMember(user.id!!, club.id) { isMember, permissionLevel ->
                    if(isMember)
                        loadView(board, club, user)
                    else {
                        emptyText.text = context.resources.getString(R.string.error_no_permission_read_board)
                        emptyText.visibility = View.VISIBLE
                        loadingBar.visibility = View.GONE
                    }
                }
            }
            else
                loadView(board, club, user);
        }

        private fun loadView(board: Board, club: Club?, user: User)
        {
            boardInterface.getPostListLimited(board, true, 4, onComplete = { list, cantReadMore ->
                if(list.isEmpty()) {
                    emptyText.text = context.resources.getString(R.string.text_empty_board)
                    emptyText.visibility = View.VISIBLE
                }
                else {
                    for(post in list) {
                        val itemView = LayoutInflater.from(context).inflate(R.layout.preview_post_item, null)
                        val itemTitle = itemView.findViewById<TextView>(R.id.preview_post_title)
                        val itemDate = itemView.findViewById<TextView>(R.id.preview_post_date)

                        itemTitle.text = post.title
                        itemDate.text = dateFormat.format(post.date)

                        itemView.setOnClickListener {
                            onLoadingStart?.invoke()
                            if(club != null) {
                                clubInterface.checkClubMember(user.id!!, club.id) { isMember, permissionLevel ->
                                    if(isMember)
                                        onLoadingEnd?.invoke(true, post, board)
                                    else
                                        onLoadingEnd?.invoke(false, null, null)
                                }
                            }
                            else
                                onLoadingEnd?.invoke(true, post, board)
                        }

                        postList.addView(itemView)
                    }
                    postList.visibility = View.VISIBLE
                }
                loadingBar.visibility = View.GONE
            })
        }
    }
}