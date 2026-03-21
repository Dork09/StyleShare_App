/**
 * מטרת הקובץ:
 * מסך התחברות - UI שמדבר עם AuthViewModel.
 */
package com.example.styleshare.ui.auth

import android.os.LocaleList
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentLoginBinding
import com.example.styleshare.utils.Result
import com.example.styleshare.utils.Validators

class LoginFragment : Fragment(R.layout.fragment_login) {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels()

    /** חיבור UI + מאזינים */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLoginBinding.bind(view)
        val imeLocales = LocaleList.forLanguageTags("he,en")
        binding.etEmail.imeHintLocales = imeLocales
        binding.etPassword.imeHintLocales = imeLocales

        if (vm.isLoggedIn()) {
            // --- Added: Using navigation action to clear LoginFragment from backstack so back button closes app ---
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString().orEmpty()
            val pass = binding.etPassword.text?.toString().orEmpty()

            if (!Validators.isEmailValid(email)) {
                binding.etEmail.error = getString(R.string.auth_error_email)
                return@setOnClickListener
            } else binding.etEmail.error = null

            if (!Validators.isPasswordValid(pass)) {
                binding.etPassword.error = getString(R.string.auth_error_password)
                return@setOnClickListener
            } else binding.etPassword.error = null

            vm.login(email, pass)
        }



        binding.tvTabRegister.setOnClickListener {
            findNavController().navigate(R.id.registerFragment)
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    // --- Added: Using navigation action to clear LoginFragment from backstack so back button closes app ---
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                }
                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                    binding.etPassword.error = state.message
                }
            }
        }
    }

    /** ניקוי Binding */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
