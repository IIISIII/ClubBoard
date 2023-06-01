package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityLoginBinding
import edu.sns.clubboard.listener.SpaceBlockingTextWatcher
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.ui.ErrorDialog
import edu.sns.clubboard.ui.LoadingDialog

class LoginActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private val loadingDialog = LoadingDialog()

    private val errorDialog = ErrorDialog()


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.loginToolbar)

        binding.inputLoginid.editText?.apply {
            addTextChangedListener(SpaceBlockingTextWatcher(this))
        }

        binding.inputPassword.editText?.apply {
            addTextChangedListener(SpaceBlockingTextWatcher(this))
        }

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
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
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

        errorDialog.show(this, resources.getString(R.string.error_login_failed)) {}
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