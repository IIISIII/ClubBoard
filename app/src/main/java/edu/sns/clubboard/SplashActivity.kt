package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.databinding.ActivitySplashBinding
import edu.sns.clubboard.port.AuthInterface

class SplashActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private var startTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        startTime = System.currentTimeMillis()

        auth.load {
            init()
        }
    }

    private fun init()
    {
        if(auth.isLogin()) {
            auth.getUserInfo(null) {
                if(it == null)
                    splash { moveToLoginActivity() }
                else {
                    auth.checkAuthenticated(it) { isAuthenticated ->
                        if(isAuthenticated)
                            splash { moveToMainActivity() }
                        else
                            splash { moveToAuthenticateActivity() }
                    }
                }
            }
        }
        else
            splash { moveToLoginActivity() }
    }

    private fun splash(func: () -> Unit)
    {
        val deltaTime = System.currentTimeMillis() - startTime
        val delay = 1500 - deltaTime
        Handler(Looper.getMainLooper()).postDelayed(func, if(delay < 0) 0 else delay)
    }

    private fun moveToMainActivity()
    {
        startActivity(Intent(this, MainActivity::class.java))
        finishWithAnimation()
    }

    private fun moveToLoginActivity()
    {
        startActivity(Intent(this, LoginActivity::class.java))
        finishWithAnimation()
    }

    private fun moveToAuthenticateActivity()
    {
        startActivity(Intent(this, AuthenticateActivity::class.java))
        finishWithAnimation()
    }

    private fun finishWithAnimation()
    {
        finish()
        overridePendingTransition(androidx.appcompat.R.anim.abc_fade_in, androidx.appcompat.R.anim.abc_fade_out)
    }
}