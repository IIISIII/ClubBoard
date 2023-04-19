package edu.sns.clubboard

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import edu.sns.clubboard.adapter.FBClub
import edu.sns.clubboard.data.Club
import edu.sns.clubboard.databinding.ActivityClubListBinding
import edu.sns.clubboard.port.ClubInterface

class ClubListActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityClubListBinding.inflate(layoutInflater)
    }

    private lateinit var listAdapter: ClubAdapter

    private val clubInterface: ClubInterface = FBClub.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.clubListToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        listAdapter = ClubAdapter(this)
        binding.clubList.adapter = listAdapter

        clubInterface.getClubListLimited(true, 30, onComplete = {
            listAdapter.setClubList(it as ArrayList<Club>)
        })


    }

    class ClubAdapter(private val context: Context): RecyclerView.Adapter<ClubAdapter.ViewHolder>()
    {
        private var list = ArrayList<Club>()

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

        inner class ViewHolder(view: View): RecyclerView.ViewHolder(view)
        {
            private val clubTitle = view.findViewById<TextView>(R.id.club_title)
            private val clubImg = view.findViewById<ImageView>(R.id.club_img)

            fun bind(item: Club)
            {
                clubTitle.text = item.name
                this.itemView.setOnClickListener {
                    val intent = Intent(context, ClubActivity::class.java)
                    intent.putExtra("club_id", item.id)
                    context.startActivity(intent)
                }
            }
        }

        fun setClubList(list: ArrayList<Club>)
        {
            this.list = list
            notifyDataSetChanged()
        }
    }
}