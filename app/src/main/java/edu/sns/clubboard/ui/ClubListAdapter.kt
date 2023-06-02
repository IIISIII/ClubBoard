package edu.sns.clubboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.*
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Club
import java.util.*

class ClubListAdapter(private val context: Context): RecyclerView.Adapter<ClubListAdapter.ViewHolder>()
{
    private val fileManager = FBFileManager.getInstance()

    private var list = ArrayList<Club>()

    private var onClick: ((Club) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(context).inflate(R.layout.club_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun setOnItemClick(onClick: (Club) -> Unit)
    {
        this.onClick = onClick
    }

    @SuppressLint("NotifyDataSetChanged")
    fun clear()
    {
        this.list.clear()
        notifyDataSetChanged()
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
    {
        private val clubTitle = view.findViewById<TextView>(R.id.input_title)
        private val clubImg = view.findViewById<ImageView>(R.id.club_img)

        fun bind(item: Club)
        {
            clubTitle.text = item.name

            if(item.imgPath != null) {
                fileManager.getImage(item.imgPath!!) {
                    if(it != null)
                        clubImg.setImageBitmap(it)
                    else
                        clubImg.setImageResource(R.mipmap.empty_club_logo)
                }
            }
            else
                clubImg.setImageResource(R.mipmap.empty_club_logo)

            this.itemView.setOnClickListener {
                onClick?.invoke(item)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setClubList(list: ArrayList<Club>)
    {
        this.list = list
        notifyDataSetChanged()
    }

    fun addClubList(list: ArrayList<Club>)
    {
        val size = this.list.size
        this.list.addAll(list)
        notifyItemRangeInserted(size, this.list.size)
    }
}