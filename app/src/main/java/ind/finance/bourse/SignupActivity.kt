package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.bourse.databinding.ActivitySignupBinding
import ind.finance.bourse.ui.auth.AuthViewModel
import ind.finance.bourse.ui.auth.RegisterState

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.btnCreateAccount) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val params = v.layoutParams as android.view.ViewGroup.MarginLayoutParams
            params.bottomMargin = systemBars.bottom + 16
            v.layoutParams = params
            insets
        }

        binding.btnCreateAccount.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            viewModel.register(name, email, password, confirmPassword)
        }

        binding.tvSignIn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Idle -> {
                    binding.btnCreateAccount.isEnabled = true
                    binding.btnCreateAccount.text = "Create Account"
                }
                is RegisterState.Loading -> {
                    binding.btnCreateAccount.isEnabled = false
                    binding.btnCreateAccount.text = "Creating account..."
                }
                is RegisterState.Success -> {
                    binding.btnCreateAccount.isEnabled = true
                    Toast.makeText(this, "Account created successfully. Please sign in.", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                    startActivity(intent)
                    finish()
                }
                is RegisterState.Error -> {
                    binding.btnCreateAccount.isEnabled = true
                    binding.btnCreateAccount.text = "Create Account"
                    Toast.makeText(this, state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
