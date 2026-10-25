package ind.finance.bourse

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import ind.finance.bourse.databinding.ActivityProfileBinding
import ind.finance.bourse.databinding.DialogChangePinBinding
import ind.finance.bourse.data.local.TokenManager
import ind.finance.bourse.ui.profile.ProfileState
import ind.finance.bourse.ui.profile.ProfileViewModel
import ind.finance.bourse.ui.profile.TransactionPinState
import ind.finance.bourse.ui.profile.TransactionPinViewModel
import androidx.activity.viewModels

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by viewModels()
    private val pinViewModel: TransactionPinViewModel by viewModels()
    private var loadingDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupListeners()
        setupThemeSelector()
        setupProfileObserver()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadProfile()
    }

    private fun setupProfileObserver() {
        viewModel.state.observe(this) { state ->
            when (state) {
                ProfileState.Loading -> showProfileLoading()
                is ProfileState.Success -> {
                    dismissProfileLoading()
                    bindProfile(state.profile)
                }

                is ProfileState.Error -> {
                    dismissProfileLoading()
                    showProfileError()
                }
                ProfileState.Unauthorized -> {
                    dismissProfileLoading()
                    navigateToLogin()
                }
            }
        }
    }

    private fun bindProfile(profile: ind.finance.bourse.data.model.UserDto) {
        val name = profile.name.orEmpty().ifBlank { "—" }
        val email = profile.email.orEmpty().ifBlank { "—" }
        binding.tvProfileName.text = name
        binding.tvProfileEmail.text = email
        binding.tvProfileInitials.text = name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "—" }
    }

    private fun showProfileLoading() {
        if (loadingDialog?.isShowing == true) return
        val progressBar = ProgressBar(this)
        loadingDialog = AlertDialog.Builder(this)
            .setTitle("Loading profile")
            .setView(progressBar)
            .setCancelable(false)
            .create()
        loadingDialog?.show()
    }

    private fun dismissProfileLoading() {
        loadingDialog?.dismiss()
        loadingDialog = null
    }

    private fun showProfileError() {
        AlertDialog.Builder(this)
            .setMessage("Unable to load profile")
            .setPositiveButton("Retry") { _, _ -> viewModel.loadProfile() }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun setupListeners() {
        binding.btnPersonalInfo.setOnClickListener {
            Toast.makeText(this, "Personal Information", Toast.LENGTH_SHORT).show()
        }

        binding.btnChangePin.setOnClickListener {
            showChangePinDialog()
        }

        binding.cardLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }

        // Bottom Navigation
        binding.navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navMarket.setOnClickListener {
            val intent = Intent(this, MarketActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navPortfolio.setOnClickListener {
            val intent = Intent(this, PortfolioActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            overridePendingTransition(0, 0)
        }

        binding.navProfile.setOnClickListener {
            // Already on Profile
        }
    }

    private fun showChangePinDialog() {
        val pinBinding = DialogChangePinBinding.inflate(LayoutInflater.from(this))
        val hasPin = pinViewModel.hasPin()
        pinBinding.tvChangePinTitle.text = if (hasPin) {
            "Change Transaction PIN"
        } else {
            "Set Transaction PIN"
        }
        pinBinding.tvCurrentPinLabel.visibility = if (hasPin) View.VISIBLE else View.GONE
        pinBinding.etCurrentPin.visibility = if (hasPin) View.VISIBLE else View.GONE

        val dialog = AlertDialog.Builder(this)
            .setView(pinBinding.root)
            .setPositiveButton("Save PIN", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialog.setOnShowListener {
            val saveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            saveButton.isEnabled = false

            val watcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    val current = pinBinding.etCurrentPin.text.toString()
                    val p1 = pinBinding.etNewPin.text.toString()
                    val p2 = pinBinding.etConfirmPin.text.toString()
                    val currentValid = !hasPin || current.matches(Regex("\\d{4}"))
                    val isValid = currentValid && p1.matches(Regex("\\d{4}")) && p2.matches(Regex("\\d{4}")) && p1 == p2
                    saveButton.isEnabled = isValid
                    pinBinding.tvPinMatchError.visibility = View.GONE
                }
                override fun afterTextChanged(s: Editable?) {}
            }

            pinBinding.etCurrentPin.addTextChangedListener(watcher)
            pinBinding.etNewPin.addTextChangedListener(watcher)
            pinBinding.etConfirmPin.addTextChangedListener(watcher)

            saveButton.setOnClickListener {
                pinViewModel.savePin(
                    pinBinding.etCurrentPin.text.toString(),
                    pinBinding.etNewPin.text.toString(),
                    pinBinding.etConfirmPin.text.toString()
                )
                when (val state = pinViewModel.state.value) {
                    TransactionPinState.Success -> {
                        pinBinding.etCurrentPin.text?.clear()
                        pinBinding.etNewPin.text?.clear()
                        pinBinding.etConfirmPin.text?.clear()
                        Toast.makeText(this, "Transaction PIN updated successfully", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        pinViewModel.reset()
                    }
                    is TransactionPinState.Error -> {
                        pinBinding.tvPinMatchError.text = state.message
                        pinBinding.tvPinMatchError.visibility = View.VISIBLE
                    }
                    else -> Unit
                }
            }
        }

        dialog.show()
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Log out of Bourse?")
            .setMessage("You will need to sign in again to access your account.")
            .setPositiveButton("Log Out") { _, _ ->
                TokenManager(this).clearToken()
                navigateToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupThemeSelector() {
        val buttons = listOf(binding.btnThemeSystem, binding.btnThemeLight, binding.btnThemeDark)

        binding.btnThemeSystem.setOnClickListener {
            updateThemeUI(binding.btnThemeSystem, buttons)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }

        binding.btnThemeLight.setOnClickListener {
            updateThemeUI(binding.btnThemeLight, buttons)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        binding.btnThemeDark.setOnClickListener {
            updateThemeUI(binding.btnThemeDark, buttons)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun updateThemeUI(selectedBtn: View, allBtns: List<View>) {
        for (btn in allBtns) {
            val tv = btn as TextView
            if (btn == selectedBtn) {
                tv.background = getDrawable(R.drawable.bg_pill_selected)
                tv.setTextColor(getColor(R.color.bourse_button_text))
            } else {
                tv.background = getDrawable(R.drawable.bg_pill_unselected)
                tv.setTextColor(getColor(R.color.bourse_text_secondary))
            }
        }
    }
}
