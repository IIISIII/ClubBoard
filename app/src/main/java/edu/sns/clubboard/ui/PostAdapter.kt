package edu.sns.clubboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.R
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post
import java.text.SimpleDateFormat
import java.util.Date

class PostAdapter(val board: Board): RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var list = ArrayList<Post>()

    private val fileManager = FBFileManager.getInstance()

    private var onItemClick: ((post: Post, board: Board) -> Unit)? = null

    private var onItemLongClick: ((post: Post, board: Board) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
        return ViewHolder(view, parent.context, board)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    @SuppressLint("NotifyDataSetChanged")
    fun setPostList(list: ArrayList<Post>)
    {
        this.list = list
        notifyDataSetChanged()
    }

    fun addPostList(list: ArrayList<Post>)
    {
        val size = this.list.size
        this.list.addAll(list)
        notifyItemRangeInserted(size, this.list.size)
    }

    fun setOnItemClick(itemClick: (post: Post, board: Board) -> Unit)
    {
        onItemClick = itemClick
    }

    fun setOnItemLongClick(itemLongClick: (post: Post, board: Board) -> Unit)
    {
        onItemLongClick = itemLongClick
    }

    inner class ViewHolder(view: View, val context: Context, val board: Board): RecyclerView.ViewHolder(view)
    {
        @SuppressLint("SimpleDateFormat")
        private val dateFormat = SimpleDateFormat("MM/dd")

        @SuppressLint("SimpleDateFormat")
        private val dateWithYearFormat = SimpleDateFormat("yyyy/MM/dd")

        @SuppressLint("SimpleDateFormat")
        private val yearFormat = SimpleDateFormat("yyyy")

        private val postTitle = view.findViewById<TextView>(R.id.post_title)
        private val postDate = view.findViewById<TextView>(R.id.post_date)
        private val postImg = view.findViewById<ImageView>(R.id.post_img)

        fun bind(item: Post)
        {
            if(item.postType == Post.TYPE_RECRUIT)
                postImg.setImageResource(R.mipmap.empty_club_logo)

            postImg.visibility = if(item.imgPath != null)
                    View.VISIBLE
                else
                    View.GONE

            if(item.imgPath != null) {
                fileManager.getImage(item.imgPath) {
                    if(it != null)
                        postImg.setImageBitmap(it)
                    else
                        postImg.visibility = View.GONE
                }
            }

            postTitle.text = item.title

            postDate.text = if(yearFormat.format(item.date) != yearFormat.format(Date()))
                dateWithYearFormat.format(item.date)
            else
                dateFormat.format(item.date)

            this.itemView.setOnClickListener {
                onItemClick?.invoke(item, board)
            }
            this.itemView.setOnLongClickListener {
                onItemLongClick?.invoke(item, board)
                true
            }
        }
    }
}