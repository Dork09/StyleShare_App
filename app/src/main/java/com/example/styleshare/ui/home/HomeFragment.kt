package com.example.styleshare.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.styleshare.R
import com.example.styleshare.databinding.FragmentHomeBinding
import com.example.styleshare.utils.Result
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private data class ResolvedWeatherLocation(
        val latitude: Double,
        val longitude: Double,
        val cityName: String
    )

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    private lateinit var adapterAllLooks: LooksAdapter
    private lateinit var adapterRecommended: LooksAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        adapterAllLooks = LooksAdapter(
            items = emptyList(),
            onItemClick = { look ->
                val action = HomeFragmentDirections.actionHomeFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            },
            onFavClick = { look ->
                vm.toggleFavorite(look.id)
            }
        )

        adapterRecommended = LooksAdapter(
            items = emptyList(),
            onItemClick = { look ->
                val action = HomeFragmentDirections.actionHomeFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            },
            onFavClick = { look ->
                vm.toggleFavorite(look.id)
            }
        )

        binding.rvRecommended.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommended.adapter = adapterRecommended

        binding.rvLooks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLooks.adapter = adapterAllLooks

        vm.greeting.observe(viewLifecycleOwner) { text ->
            binding.tvHello.text = text
        }

        vm.weatherLocation.observe(viewLifecycleOwner) { text ->
            binding.tvLocation.text = text
        }

        vm.weatherTemp.observe(viewLifecycleOwner) { text ->
            binding.tvTemperature.text = text
            binding.tvTemperature.visibility = if (text.isBlank()) View.GONE else View.VISIBLE
        }

        vm.feedState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Result.Loading -> binding.progress.visibility = View.VISIBLE
                is Result.Success -> {
                    binding.progress.visibility = View.GONE
                    adapterAllLooks.submitList(state.data)
                }

                is Result.Error -> {
                    binding.progress.visibility = View.GONE
                }
            }
        }

        vm.recommended.observe(viewLifecycleOwner) { list ->
            adapterRecommended.submitList(list)
        }

        vm.loadFeed()
        loadWeatherByLocation()
    }

    private fun loadWeatherByLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(requireActivity())

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setGranularity(Granularity.GRANULARITY_FINE)
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(10_000)
            .build()

        val cancellationTokenSource = CancellationTokenSource()

        fused.getCurrentLocation(request, cancellationTokenSource.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    bindWeatherLocation(location)
                } else {
                    fused.lastLocation
                        .addOnSuccessListener { lastLocation ->
                            if (lastLocation != null) {
                                bindWeatherLocation(lastLocation)
                            } else {
                                binding.tvLocation.text = getString(R.string.home_weather_error)
                                binding.tvTemperature.visibility = View.GONE
                            }
                        }
                        .addOnFailureListener {
                            binding.tvLocation.text = getString(R.string.home_weather_error)
                            binding.tvTemperature.visibility = View.GONE
                        }
                }
            }
            .addOnFailureListener {
                binding.tvLocation.text = getString(R.string.home_weather_error)
                binding.tvTemperature.visibility = View.GONE
            }
    }

    private fun bindWeatherLocation(location: Location) {
        val resolvedLocation = resolveWeatherLocation(location)
        vm.loadWeather(
            resolvedLocation.latitude,
            resolvedLocation.longitude,
            resolvedLocation.cityName
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 101 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            loadWeatherByLocation()
        } else {
            binding.tvLocation.text = getString(R.string.home_weather_permission)
            binding.tvTemperature.visibility = View.GONE
        }
    }

    private fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale("iw", "IL"))
            val list = geocoder.getFromLocation(lat, lon, 1)
            resolveBestCityName(list?.firstOrNull())
        } catch (e: Exception) {
            "המיקום שלך"
        }
    }

    private fun resolveBestCityName(address: Address?): String {
        return address?.locality
            ?.takeIf { it.isNotBlank() }
            ?: address?.subAdminArea?.takeIf { it.isNotBlank() }
            ?: address?.adminArea?.takeIf { it.isNotBlank() }
            ?: address?.countryName?.takeIf { it.isNotBlank() }
            ?: "המיקום שלך"
    }

    private fun resolveWeatherLocation(location: Location): ResolvedWeatherLocation {
        val cityName = getCityName(location.latitude, location.longitude)
        val shouldUseTelAvivFallback = isLikelyEmulator() && (
            cityName.contains("Mountain View", ignoreCase = true) || location.isMock
        )

        return if (shouldUseTelAvivFallback) {
            ResolvedWeatherLocation(
                latitude = 32.0853,
                longitude = 34.7818,
                cityName = "תל אביב"
            )
        } else {
            ResolvedWeatherLocation(
                latitude = location.latitude,
                longitude = location.longitude,
                cityName = cityName
            )
        }
    }

    private fun isLikelyEmulator(): Boolean {
        return Build.FINGERPRINT.startsWith("generic") ||
            Build.FINGERPRINT.contains("emulator", ignoreCase = true) ||
            Build.MODEL.contains("Emulator", ignoreCase = true) ||
            Build.MODEL.contains("Android SDK built for", ignoreCase = true) ||
            Build.MANUFACTURER.contains("Genymotion", ignoreCase = true) ||
            Build.PRODUCT.contains("sdk", ignoreCase = true)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
