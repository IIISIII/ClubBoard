package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import edu.sns.clubboard.adapter.MyAuthorization
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivitySignupBinding
import edu.sns.clubboard.port.AuthInterface

class SignupActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = MyAuthorization()

    private var studentId: String? = null
    private var name: String? = null
    private var phone: String? = null
    private var email: String? = null
    private var nickname: String? = null

    private var loginId: String? = null
    private var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.signupBtn.setOnClickListener {
            try {
                email = binding.inputEmail.editText?.text.toString()
                loginId = binding.inputLoginid.editText?.text.toString()
                password = binding.inputPassword.editText?.text.toString()
                name = binding.inputName.editText?.text.toString()
                studentId = binding.inputStudentid.editText?.text.toString()
                phone = binding.inputPhone.editText?.text.toString()
                nickname = "test"

                val user = User(studentId!!, name!!, phone!!, email!!, nickname!!, loginId!!, null)

                auth.signUp(user, password!!, onSuccess = {
                    onSuccess()
                }, onFailed = {
                    onFailed()
                })
            } catch (err: Exception) {
                Log.e("signup", err.message.toString())
            }
        }
    }

    private fun onSuccess()
    {
        Toast.makeText(this, "success", Toast.LENGTH_SHORT).show()
        moveToAuthenticateActivity()
    }

    private fun onFailed()
    {
        Toast.makeText(this, "fail", Toast.LENGTH_SHORT).show()
    }

    private fun moveToAuthenticateActivity()
    {
        val intent = Intent(this, AuthenticateActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }
}