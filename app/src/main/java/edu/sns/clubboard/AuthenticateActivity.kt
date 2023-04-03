package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import edu.sns.clubboard.adapter.MyAuthorization
import edu.sns.clubboard.databinding.ActivityAuthenticateBinding
import edu.sns.clubboard.port.AuthInterface

class AuthenticateActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAuthenticateBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = MyAuthorization()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

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