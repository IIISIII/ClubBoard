package edu.sns.clubboard.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.R
import edu.sns.clubboard.data.Post

class PostAdapter: RecyclerView.Adapter<PostAdapter.ViewHolder>()
{
    private var list = ArrayList<Post>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.board_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        private val postTitle = view.findViewById<TextView>(R.id.post_title)
        private val postImg = view.findViewById<ImageView>(R.id.post_img)

        fun bind(item: Post)
        {
            postTitle.text = item.title
        }
    }

    fun setPostList(list: ArrayList<Post>)
    {
        this.list = list
        notifyDataSetChanged()
    }
}