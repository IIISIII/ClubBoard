package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityLoginBinding
import edu.sns.clubboard.port.AuthInterface

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.loginToolbar)

        binding.signupBtn.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            loadingStart()

            val id = binding.inputLoginid.editText?.text.toString()
            val password = binding.inputPassword.editText?.text.toString()

            try {
                auth.login(id, password, onSuccess = {
                    onSuccess(it)
                }, onFailed = {
                    onFailed()
                })
            } catch (err: Exception) {

            }
        }
    }

    private fun loadingStart()
    {
        binding.loadingBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.loadingBackground.visibility = View.GONE
    }

    private fun onSuccess(user: User)
    {
        auth.checkAuthenticated(user) {
            if(it)
                moveToMainActivity()
            else
                moveToAuthenticateActivity()
        }
    }

    private fun onFailed()
    {
        loadingEnd()
        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
    }

    private fun moveToMainActivity()
    {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun moveToAuthenticateActivity()
    {
        startActivity(Intent(this, AuthenticateActivity::class.java))
        finish()
    }
}