package edu.sns.clubboard.ui

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.PostActivity
import edu.sns.clubboard.R
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.data.Post

class PostAdapter(val board: Board): RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var list = ArrayList<Post>()

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

    inner class ViewHolder(view: View, val context: Context, val board: Board): RecyclerView.ViewHolder(view)
    {
        private val postTitle = view.findViewById<TextView>(R.id.post_title)
        private val postImg = view.findViewById<ImageView>(R.id.post_img)

        fun bind(item: Post)
        {
            postTitle.text = item.title
            this.itemView.setOnClickListener {
                val intent = Intent(context, PostActivity::class.java)
                intent.putExtra("post_id", item.id)
                context.startActivity(intent)
            }
        }
    }

    fun setPostList(list: ArrayList<Post>)
    {
        this.list = list
        notifyDataSetChanged()
    }
}