package edu.sns.clubboard.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.R
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBFileManager
import edu.sns.clubboard.data.Request

class RequestAdapter: RecyclerView.Adapter<RequestAdapter.ViewHolder>()
{
    private var requestList = ArrayList<RequestWrapper>()

    private val fileManager = FBFileManager.getInstance()

    private val auth = FBAuthorization.getInstance()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder
    {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.request_item, parent, false)
        return ViewHolder(view, parent.context)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int)
    {
        holder.bind(requestList[position], position)
    }

    override fun getItemCount(): Int = requestList.size

    fun delete(position: Int)
    {
        if(position >= 0 && position < requestList.size) {
            requestList.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setItemList(list: List<Request>?)
    {
        if(list == null)
            requestList.clear()
        else {
            requestList = list.map {
                RequestWrapper(it, false)
            } as ArrayList<RequestWrapper>
        }
        notifyDataSetChanged()
    }

    fun getCheckedRequestList(): ArrayList<Request>?
    {
        if(requestList.isEmpty())
            return null
        val checkedList = requestList.filter {
            it.isChecked
        }
        return checkedList.map {
            it.request
        } as ArrayList<Request>
    }

    inner class ViewHolder(view: View, val context: Context): RecyclerView.ViewHolder(view)
    {
        val userImg = view.findViewById<ImageView>(R.id.user_img)
        val userName = view.findViewById<TextView>(R.id.user_name)
        val userCheck = view.findViewById<CheckBox>(R.id.user_check)

        fun bind(requestWrapper: RequestWrapper, position: Int)
        {
            auth.getUserInfo(requestWrapper.request.userId) {
                if(it != null) {
                    if(it.imagePath != null) {
                        fileManager.getImage(it.imagePath!!) { img ->
                            if(img != null)
                                userImg.setImageBitmap(img)
                            else
                                userImg.setImageResource(R.drawable.ic_baseline_account_circle_24)
                        }
                    }
                    else
                        userImg.setImageResource(R.drawable.ic_baseline_account_circle_24)

                    userName.text = it.name
                    userCheck.setOnCheckedChangeListener { _, isChecked ->
                        requestWrapper.isChecked = isChecked
                    }
                }
                else
                    delete(position)
            }
        }
    }

    data class RequestWrapper(val request: Request, var isChecked: Boolean = false)
}