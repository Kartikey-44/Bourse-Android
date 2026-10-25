package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.bourse.databinding.ActivityLoginBinding
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
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    binding.btnSignIn.isEnabled = true
                    binding.btnSignIn.text = "Sign In"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
