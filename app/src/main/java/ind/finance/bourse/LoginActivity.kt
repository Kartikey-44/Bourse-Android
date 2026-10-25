package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.bourse.data.local.TransactionPinStore
import ind.finance.bourse.databinding.ActivityLoginBinding
import ind.finance.bourse.databinding.DialogChangePinBinding
import ind.finance.bourse.ui.auth.AuthViewModel
import ind.finance.bourse.ui.auth.LoginState

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.btnSignIn) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.bottomMargin = systemBars.bottom + 16
            v.layoutParams = params
            insets
        }

        setupPasswordToggle(binding.etPassword)

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(email, password)
        }

        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }

        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Idle -> {
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.text = "Sign In"
                }
                is LoginState.Loading -> {
                    binding.btnSignIn.isEnabled = false
                    binding.btnSignIn.text = "Signing in..."
                }
                is LoginState.Success -> {
                    binding.btnSignIn.isEnabled = true
                    val pinStore = TransactionPinStore(this)
                    if (!pinStore.hasPin()) {
                        showPinSetupDialog {
                            navigateToHome()
                        }
                    } else {
                        navigateToHome()
                    }
                }
                is LoginState.Error -> {
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.text = "Sign In"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showPinSetupDialog(onPinSaved: () -> Unit) {
        val pinBinding = DialogChangePinBinding.inflate(LayoutInflater.from(this))
        pinBinding.tvChangePinTitle.text = "Set Transaction PIN"
        pinBinding.tvCurrentPinLabel.visibility = View.GONE
        pinBinding.etCurrentPin.visibility = View.GONE

        val dialog = AlertDialog.Builder(this)
            .setView(pinBinding.root)
            .setCancelable(false)
            .setPositiveButton("Save PIN", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val p1 = pinBinding.etNewPin.text.toString()
                    val p2 = pinBinding.etConfirmPin.text.toString()
                    val isValid = p1.matches(Regex("\\d{4}")) && p2.matches(Regex("\\d{4}")) && p1 == p2
                    saveButton.isEnabled = isValid
                    pinBinding.tvPinMatchError.visibility = View.GONE
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            pinBinding.etNewPin.addTextChangedListener(watcher)
            pinBinding.etConfirmPin.addTextChangedListener(watcher)

            saveButton.setOnClickListener {
                val p1 = pinBinding.etNewPin.text.toString()
                val p2 = pinBinding.etConfirmPin.text.toString()
                if (p1.matches(Regex("\\d{4}")) && p1 == p2) {
                    TransactionPinStore(this).savePin(p1)
                    Toast.makeText(this, "Transaction PIN saved", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    onPinSaved()
                } else {
                    pinBinding.tvPinMatchError.text = "PINs do not match or invalid length"
                    pinBinding.tvPinMatchError.visibility = View.VISIBLE
                }
            }
        }

        dialog.show()
    }

    private fun setupPasswordToggle(editText: EditText) {
        var isPasswordVisible = false
        editText.transformationMethod = PasswordTransformationMethod.getInstance()

        editText.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                val drawable = editText.compoundDrawablesRelative[2] ?: editText.compoundDrawables[2]
                if (drawable != null) {
                    val touchableStart = editText.width - editText.paddingEnd - drawable.intrinsicWidth - 20
                    if (event.x >= touchableStart) {
                        isPasswordVisible = !isPasswordVisible
                        val cursorPosition = editText.selectionEnd

                        if (isPasswordVisible) {
                            editText.transformationMethod = null
                        } else {
                            editText.transformationMethod = PasswordTransformationMethod.getInstance()
                        }

                        if (cursorPosition in 0..editText.text.length) {
                            editText.setSelection(cursorPosition)
                        } else {
                            editText.setSelection(editText.text.length)
                        }
                        view.performClick()
                        return@setOnTouchListener true
                    }
                }
            }
            false
        }
    }
}
