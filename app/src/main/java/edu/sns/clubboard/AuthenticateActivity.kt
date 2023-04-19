package edu.sns.clubboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.databinding.ActivityAuthenticateBinding
import edu.sns.clubboard.port.AuthInterface

class AuthenticateActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAuthenticateBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.authToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.mailBtn.setOnClickListener {
            val viewIntent = Intent("android.intent.action.VIEW", Uri.parse("https://mail.hansung.ac.kr/"))
            startActivity(viewIntent)
        }

        binding.authBtn.setOnClickListener {
            auth.authenticate(null)
        }

        binding.checkAuthBtn.setOnClickListener {
            auth.checkAuthenticated(null, onSuccess = {
                onSuccess()
            }, onFailed = {
                onFailed()
            })
        }
    }

    private fun onSuccess()
    {
        moveToMainActivity()
    }

    private fun onFailed()
    {

    }

    private fun moveToMainActivity()
    {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}