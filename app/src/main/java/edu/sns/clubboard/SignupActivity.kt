package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Fade
import android.transition.Scene
import android.transition.TransitionManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivitySignupBinding
import edu.sns.clubboard.port.AuthInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private var loginId: String? = null
    private var password: String? = null
    private var passwordCheck: String? = null
    private var mail: String? = null
    private var mailHansung: String? = null
    private var nickname: String? = null
    private var name: String? = null
    private var studentId: String? = null
    private var phone: String? = null

    private lateinit var scene1: Scene
    private lateinit var scene2: Scene

    private var signupFlag: Int = 0

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.signupToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        scene1 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_signup_1, this)
        scene2 = Scene.getSceneForLayout(binding.sceneRoot, R.layout.scene_signup_2, this)

        scene1.setEnterAction {
            init(0)
        }
        scene2.setEnterAction {
            init(1)
        }

        goToScene(0)
    }

    private fun init(flag: Int)
    {
        if(flag == 0) {
            val inputId = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_id)
            val inputPassword = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_pw)
            val inputPasswordCheck = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_pw_check)
            val inputMail = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_mail)
            val nextBtn = binding.sceneRoot.findViewById<Button>(R.id.btn_next)

            inputId.editText?.setText(loginId ?: "")
            inputPassword.editText?.setText(password ?: "")
            inputPasswordCheck.editText?.setText(passwordCheck ?: "")
            inputMail.editText?.setText(mail ?: "")

            nextBtn.setOnClickListener {
                loadingStart()

                var errorId: String? = null
                var errorPassword: String? = null
                var errorPasswordCheck: String? = null
                var errorMail: String? = null

                fun checkError(): Boolean {
                    if(errorId != null || errorPassword != null || errorPasswordCheck != null || errorMail != null) {
                        inputId.isErrorEnabled = errorId != null
                        inputId.error = errorId
                        inputPassword.isErrorEnabled = errorPassword != null
                        inputPassword.error = errorPassword
                        inputPasswordCheck.isErrorEnabled = errorPasswordCheck != null
                        inputPasswordCheck.error = errorPasswordCheck
                        inputMail.isErrorEnabled = errorMail != null
                        inputMail.error = errorMail
                        return false
                    }
                    return true
                }

                loginId = inputId.editText?.text.toString()
                password = inputPassword.editText?.text.toString()
                passwordCheck = inputPasswordCheck.editText?.text.toString()
                mail = inputMail.editText?.text.toString()
                mailHansung = "${mail}@hansung.ac.kr"

                if(loginId?.length == 0)
                    errorId = "Please enter ID"
                if(password?.length == 0)
                    errorPassword = "Please enter Password"
                else if(password!!.length < 6)
                    errorPassword = "Password must be 6 characters or more"
                if(passwordCheck != password)
                    errorPasswordCheck = "Please check your Password"
                if(mail?.length == 0)
                    errorMail = "Please enter E-mail"

                CoroutineScope(Dispatchers.IO).launch {
                    if(!auth.isValidId(loginId!!))
                        errorId = "Same ID is already exists"
                    if(!auth.isValidMail(mailHansung!!))
                        errorMail = "Same Mail is already exists"

                    runOnUiThread {
                        if(checkError())
                            goToScene(1)
                        loadingEnd()
                    }
                }
            }
        }
        else if(flag == 1) {
            val inputImg = binding.sceneRoot.findViewById<ImageView>(R.id.img_profile)
            val inputNickname = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_nickname)
            val inputName = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_name)
            val inputStudentId = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_student_id)
            val inputPhone = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_phone)
            val previousBtn = binding.sceneRoot.findViewById<Button>(R.id.btn_previous)
            val submitBtn = binding.sceneRoot.findViewById<Button>(R.id.btn_submit)

            inputNickname.editText?.setText(nickname ?: "")
            inputName.editText?.setText(name ?: "")
            inputStudentId.editText?.setText(studentId ?: "")
            inputPhone.editText?.setText(phone ?: "")

            previousBtn.setOnClickListener {
                nickname = null
                name = null
                studentId = null
                phone = null
                goToScene(0)
            }

            submitBtn.setOnClickListener {
                loadingStart()

                var errorNickname: String? = null
                var errorName: String? = null
                var errorStudentId: String? = null
                var errorPhone: String? = null

                fun checkError(): Boolean {
                    if(errorNickname != null || errorName != null || errorStudentId != null || errorPhone != null) {
                        inputNickname.isErrorEnabled = errorNickname != null
                        inputNickname.error = errorNickname
                        inputName.isErrorEnabled = errorName != null
                        inputName.error = errorName
                        inputStudentId.isErrorEnabled = errorStudentId != null
                        inputStudentId.error = errorStudentId
                        inputPhone.isErrorEnabled = errorPhone != null
                        inputPhone.error = errorPhone
                        return false
                    }
                    return true
                }

                nickname = inputNickname.editText?.text.toString()
                name = inputName.editText?.text.toString()
                studentId = inputStudentId.editText?.text.toString()
                phone = inputPhone.editText?.text.toString()

                if(nickname?.length == 0)
                    errorNickname = "Please enter Nickname"
                if(name?.length == 0)
                    errorName = "Please enter Name"
                if(studentId?.length == 0)
                    errorStudentId = "Please enter Student ID"
                if(phone?.length == 0)
                    errorPhone = "Please enter Phone Number"

                CoroutineScope(Dispatchers.IO).launch {
                    if(!auth.isValidNickname(nickname!!))
                        errorNickname = "Same Nickname is already exists"

                    runOnUiThread {
                        if(checkError()) {
                            val user = User(null, studentId!!, name!!, phone!!, mailHansung!!, nickname!!, loginId!!, null)
                            auth.signUp(user, password!!, onSuccess = {
                                moveToAuthenticateActivity()
                                loadingEnd()
                            }, onFailed = {
                                Toast.makeText(this@SignupActivity, "Failed to Signup", Toast.LENGTH_SHORT).show()
                                loadingEnd()
                            })
                        }
                    }
                }
            }
        }
    }

    private fun goToScene(flag: Int)
    {
        signupFlag = flag

        if(signupFlag == 0)
            TransitionManager.go(scene1, Fade())
        else if(signupFlag == 1)
            TransitionManager.go(scene2, Fade())
    }

    private fun loadingStart()
    {
        binding.progressBackground.visibility = View.VISIBLE
    }

    private fun loadingEnd()
    {
        binding.progressBackground.visibility = View.GONE
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