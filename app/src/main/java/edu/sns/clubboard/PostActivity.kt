package edu.sns.clubboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import edu.sns.clubboard.databinding.ActivityPostBinding

class PostActivity : AppCompatActivity()
{
    private val binding by lazy {
        ActivityPostBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.postToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //intent.getStringExtra("club_name")
        //intent.getStringExtra("board_name")
        val postId = intent.getStringExtra("post_id")


    }
}