/**
 * מטרת הקובץ:
 * מסך הרשמה - UI שמדבר עם AuthViewModel.
 */
package com.example.styleshare.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentRegisterBinding
import com.example.styleshare.utils.Result
import com.example.styleshare.utils.Validators

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val vm: AuthViewModel by viewModels()

    /** חיבור UI + מאזינים */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentRegisterBinding.bind(view)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text?.toString().orEmpty()
            val email = binding.etEmail.text?.toString().orEmpty()
            val pass = binding.etPassword.text?.toString().orEmpty()

            if (username.isBlank()) {
                binding.etUsername.error = "יש להזין שם משתמש"
                return@setOnClickListener
            } else binding.etUsername.error = null

            if (!Validators.isEmailValid(email)) {
                binding.etEmail.error = getString(R.string.auth_error_email)
                return@setOnClickListener
            } else binding.etEmail.error = null

            if (!Validators.isPasswordValid(pass)) {
                binding.etPassword.error = getString(R.string.auth_error_password)
                return@setOnClickListener
            } else binding.etPassword.error = null

            vm.register(email, pass, username)
        }

        binding.tvTabLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        vm.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    findNavController().navigate(R.id.homeFragment)
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
