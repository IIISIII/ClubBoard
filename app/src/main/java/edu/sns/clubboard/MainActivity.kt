package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.adapter.FBBoard
import edu.sns.clubboard.data.Board
import edu.sns.clubboard.databinding.ActivityMainBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.port.BoardInterface
import edu.sns.clubboard.ui.PreviewAdapter

class MainActivity : AppCompatActivity() {
    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val auth : AuthInterface = FBAuthorization.getInstance()

    private val boardInterface: BoardInterface = FBBoard()

    private lateinit var adapter: PreviewAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

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

    override fun onDestroy()
    {
        super.onDestroy()

        FBAuthorization.remove()
    }

    private fun init()
    {
        adapter = PreviewAdapter()
        binding.previewList.adapter = adapter

        auth.getPreviewList(auth.getUserInfo()!!, onSuccess = {
            boardInterface.getBoardListByIdList(it, onSuccess = { boards ->
                adapter.setList(boards as ArrayList<Board>)
                loadingEnd()
            }, onFailed = {
                loadingEnd()
            })
        }, onFailed = {
            loadingEnd()
        })

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

    private fun loadingStart()
    {
        binding.previewProgressBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.previewProgressBackground.visibility = View.GONE
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