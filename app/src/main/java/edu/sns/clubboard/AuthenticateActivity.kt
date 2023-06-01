package edu.sns.clubboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivityAuthenticateBinding
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.ui.ErrorDialog
import edu.sns.clubboard.ui.LoadingDialog

class AuthenticateActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityAuthenticateBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private val errorDialog = ErrorDialog()

    private val loadingDialog = LoadingDialog()


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
            auth.authenticate(auth.getUserInfo()) {
                if(it)
                    binding.checkAuthBtn.isEnabled = true
                else
                    errorDialog.show(this, resources.getString(R.string.error_failed_send_email)) {}
            }
        }

        binding.checkAuthBtn.setOnClickListener {
            auth.checkAuthenticated(auth.getUserInfo()!!) {
                if(it)
                    onSuccess()
                else
                    onFailed()
            }
        }

        binding.btnDeleteAccount.setOnClickListener {
            loadingStart()

            val user = auth.getUserInfo()!!
            auth.deleteAccount(user.id!!) {
                if(it == User.SUCCESS_DELETE_ACCOUNT)
                    moveToLoginActivity()
                else
                    errorDialog.show(this, resources.getString(R.string.error_delete_account)) {}

                loadingEnd()
            }
        }
    }

    private fun onSuccess()
    {
        moveToMainActivity()
    }

    private fun onFailed()
    {
        errorDialog.show(this, resources.getString(R.string.error_not_verify)) {}
    }

    private fun loadingStart()
    {
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
    }

    private fun moveToMainActivity()
    {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun moveToLoginActivity()
    {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }
}