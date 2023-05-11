package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.databinding.ActivityMainBinding
import edu.sns.clubboard.port.AuthInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val auth : AuthInterface = FBAuthorization.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if(!auth.isLogined())
            moveToLoginActivity()
        else if(!auth.isAuthenticated(null))
            moveToAuthenticateActivity()
        else
            init()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId) {
            R.id.noti -> {

                true
            }
            R.id.profile -> {

                true
            }
            else -> false
        }
    }

    private fun init()
    {
        binding.searchClub.setOnClickListener {
            val intent = Intent(this, ClubListActivity::class.java)
            startActivity(intent)
        }

        binding.freeBoardBtn.setOnClickListener {
            val intent = Intent(this, BoardActivity::class.java)
            intent.putExtra("board_id", "0")
            startActivity(intent)
        }
    }

    private fun moveToLoginActivity()
    {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun moveToAuthenticateActivity()
    {
        startActivity(Intent(this, AuthenticateActivity::class.java))
        finish()
    }
}