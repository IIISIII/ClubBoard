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
import edu.sns.clubboard.data.Member
import edu.sns.clubboard.data.User

class MemberAdapter: RecyclerView.Adapter<MemberAdapter.ViewHolder>()
{
    private val fileManager = FBFileManager.getInstance()

    private var memberList = ArrayList<Member>()

    private var onClick: ((Member, Int) -> Unit)? = null

    private var onListChange: ((List<Member>) -> Unit)? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.member_item, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(memberList[position], position)
    }

    override fun getItemCount(): Int = memberList.size

    fun setOnItemClick(onClick: (Member, Int) -> Unit)
    {
        this.onClick = onClick
    }

    fun setOnListChange(onListChange: (List<Member>) -> Unit)
    {
        this.onListChange = onListChange
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setMemberList(list: ArrayList<Member>)
    {
        list.sort()
        memberList = list
        onListChange?.invoke(memberList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMemberPermission(member: Member, permissionLevel: Long)
    {
        val index = memberList.indexOf(member)
        if(index < 0)
            return

        memberList[index].level = permissionLevel
        memberList.sort()
        onListChange?.invoke(memberList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMemberPermission(index: Int, permissionLevel: Long)
    {
        if(index < 0)
            return

        memberList[index].level = permissionLevel
        memberList.sort()
        onListChange?.invoke(memberList)
        notifyDataSetChanged()
    }

    fun deleteMember(member: Member)
    {
        val index = memberList.indexOf(member)
        if(index >= 0) {
            memberList.removeAt(index)
            onListChange?.invoke(memberList)
            notifyItemRemoved(index)
        }
    }

    fun deleteMember(index: Int)
    {
        if(index >= 0) {
            memberList.removeAt(index)
            onListChange?.invoke(memberList)
            notifyItemRemoved(index)
        }
    }

    inner class ViewHolder(view: View, val context: Context): RecyclerView.ViewHolder(view)
    {
        val memberImg = view.findViewById<ImageView>(R.id.member_img)
        val memberName = view.findViewById<TextView>(R.id.member_name)
        val memberLevel = view.findViewById<ImageView>(R.id.member_level)

        fun bind(member: Member, position: Int)
        {
            member.user.imagePath?.run {
                fileManager.getImage(this) {
                    if(it != null)
                        memberImg.setImageBitmap(it)
                }
            }

            memberName.text = member.user.name

            val drawable = when(member.level) {
                User.PERMISSION_LEVEL_MASTER -> R.drawable.member_level_master
                User.PERMISSION_LEVEL_MANAGER -> R.drawable.member_level_manager
                else -> R.drawable.member_level_normal
            }

            memberLevel.setImageResource(drawable)

            this.itemView.setOnClickListener {
                onClick?.invoke(member, position)
            }
        }
    }
}