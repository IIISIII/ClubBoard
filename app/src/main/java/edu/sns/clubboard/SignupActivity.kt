package edu.sns.clubboard

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Fade
import android.transition.Scene
import android.transition.TransitionManager
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputLayout
import edu.sns.clubboard.adapter.FBAuthorization
import edu.sns.clubboard.data.User
import edu.sns.clubboard.databinding.ActivitySignupBinding
import edu.sns.clubboard.listener.SpaceBlockingTextWatcher
import edu.sns.clubboard.port.AuthInterface
import edu.sns.clubboard.ui.LoadingDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class SignupActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivitySignupBinding.inflate(layoutInflater)
    }

    private val auth: AuthInterface = FBAuthorization.getInstance()

    private val loadingDialog = LoadingDialog()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home)
            finish()
        return super.onOptionsItemSelected(item)
    }

    private fun init(flag: Int)
    {
        if(flag == 0) {
            val inputId = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_id)
            val inputPassword = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_pw)
            val inputPasswordCheck = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_pw_check)
            val inputMail = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_mail)
            val textPasswordPattern = binding.sceneRoot.findViewById<TextView>(R.id.text_password_pattern)
            val nextBtn = binding.sceneRoot.findViewById<Button>(R.id.btn_next)

            inputId.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }
            inputPassword.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }
            inputPasswordCheck.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }
            inputMail.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }

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
                    textPasswordPattern.visibility = if(errorPassword != null)
                            View.INVISIBLE
                        else
                            View.VISIBLE

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
                    errorId = resources.getString(R.string.error_empty, resources.getString(R.string.text_id))
                if(password?.length == 0)
                    errorPassword = resources.getString(R.string.error_empty, resources.getString(R.string.text_pw))
                else if(password!!.length < 6)
                    errorPassword = resources.getString(R.string.error_password_length)
                if(passwordCheck != password)
                    errorPasswordCheck = resources.getString(R.string.error_password_check)
                if(mail?.length == 0)
                    errorMail = resources.getString(R.string.error_empty, resources.getString(R.string.text_mail))

                CoroutineScope(Dispatchers.IO).launch {
                    if(!auth.isValidId(loginId!!))
                        errorId = resources.getString(R.string.error_exists, resources.getString(R.string.text_id))
                    if(!auth.isValidMail(mailHansung!!))
                        errorMail = resources.getString(R.string.error_exists, resources.getString(R.string.text_mail))

                    runOnUiThread {
                        if(checkError())
                            goToScene(1)
                        loadingEnd()
                    }
                }
            }
        }
        else if(flag == 1) {
            val inputNickname = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_nickname)
            val inputName = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_name)
            val inputStudentId = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_student_id)
            val inputPhone = binding.sceneRoot.findViewById<TextInputLayout>(R.id.input_phone)
            val previousBtn = binding.sceneRoot.findViewById<Button>(R.id.btn_previous)
            val submitBtn = binding.sceneRoot.findViewById<Button>(R.id.btn_submit)

            inputNickname.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }
            inputName.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }
            inputStudentId.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }
            inputPhone.editText?.apply {
                addTextChangedListener(SpaceBlockingTextWatcher(this))
            }

            val pattern = Pattern.compile("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$")

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
                    errorNickname = resources.getString(R.string.error_empty, resources.getString(R.string.text_nickname))
                if(name?.length == 0)
                    errorName = resources.getString(R.string.error_empty, resources.getString(R.string.text_name))
                if(studentId?.length == 0)
                    errorStudentId = resources.getString(R.string.error_empty, resources.getString(R.string.text_student_id))
                if(!pattern.matcher(phone).matches())
                    errorPhone = resources.getString(R.string.error_pattern_mismatch, resources.getString(R.string.text_phone))

                CoroutineScope(Dispatchers.IO).launch {
                    if(!auth.isValidNickname(nickname!!))
                        errorNickname = resources.getString(R.string.error_exists, resources.getString(R.string.text_nickname))

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
                        else
                            loadingEnd()
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
        loadingDialog.show(supportFragmentManager, "LoadingDialog")
    }

    private fun loadingEnd()
    {
        loadingDialog.dismiss()
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