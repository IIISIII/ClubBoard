package edu.sns.clubboard.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.R
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Club
import java.text.SimpleDateFormat

class PreviewAdapter: RecyclerView.Adapter<PreviewAdapter.ViewHolder>()
{
    private var list = ArrayList<Board>()
    private var clubList = ArrayList<Club?>()

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

    fun setList(boardList: ArrayList<Board>)
    {
        list = boardList
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View, val context: Context): RecyclerView.ViewHolder(view)
    {
        val boardInterface = FBBoard()

        val boardTitle = view.findViewById<TextView>(R.id.preview_board_title)
        val clubTitle = view.findViewById<TextView>(R.id.preview_club_title)
        val loadingBar = view.findViewById<ProgressBar>(R.id.preview_loading)
        val emptyText = view.findViewById<TextView>(R.id.preview_empty_text)
        val postList = view.findViewById<LinearLayout>(R.id.preview_post_list)

        val dateFormat = SimpleDateFormat("MM.dd")

        fun bind(board: Board)
        {
            boardTitle.text = board.name
            boardInterface.getParent(board, onSuccess = {
                clubTitle.text = it?.name
            }, onFailed = {
                clubTitle.text = ""
            })
            boardInterface.getPostListLimited(board, true, 4, onComplete = {
                if(it.isEmpty())
                    emptyText.visibility = View.VISIBLE
                else {
                    for(post in it) {
                        val itemView = LayoutInflater.from(context).inflate(R.layout.preview_post_item, null)
                        val itemTitle = itemView.findViewById<TextView>(R.id.preview_post_title)
                        val itemDate = itemView.findViewById<TextView>(R.id.preview_post_date)

                        itemTitle.text = post.title
                        itemDate.text = dateFormat.format(post.date)

                        postList.addView(itemView)
                    }
                    postList.visibility = View.VISIBLE
                }
                loadingBar.visibility = View.GONE
            })
        }
    }
}