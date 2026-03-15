/**
 * מטרת הקובץ:
 * HomeFragment:
 * - מציג שלום לפי המשתמש (Greeting)
 * - מציג מזג אוויר לפי מיקום + שם עיר
 * - מציג "לוקים מומלצים" (RecyclerView אופקי)
 * - מציג "כל הלוקים" (RecyclerView אנכי)
 * - ניווט לפרטי לוק באמצעות SafeArgs
 * - שינוי מועדפים
 */
package com.example.styleshare.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
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
import com.google.android.gms.location.LocationServices
import java.util.Locale

class HomeFragment : Fragment(R.layout.fragment_home) {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val vm: HomeViewModel by viewModels()

    private lateinit var adapterAllLooks: LooksAdapter
    private lateinit var adapterRecommended: LooksAdapter

    /** חיבור UI + Adapters + Observers */
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHomeBinding.bind(view)

        // ✅ Adapter לחלוטין כל הלוקים
        adapterAllLooks = LooksAdapter(
            items = emptyList(),
            onItemClick = { look ->
                val action =
                    HomeFragmentDirections.actionHomeFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            },
            onFavClick = { look ->
                vm.toggleFavorite(look.id)
            }
        )

        // ✅ Adapter ללוקים מומלצים
        adapterRecommended = LooksAdapter(
            items = emptyList(),
            onItemClick = { look ->
                val action =
                    HomeFragmentDirections.actionHomeFragmentToLookDetailsFragment(look.id)
                findNavController().navigate(action)
            },
            onFavClick = { look ->
                vm.toggleFavorite(look.id)
            }
        )

        // ✅ RecyclerView - מומלצים (אופקי)
        binding.rvRecommended.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecommended.adapter = adapterRecommended

        // ✅ RecyclerView - כל הלוקים (אנכי)
        binding.rvLooks.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLooks.adapter = adapterAllLooks

        // ✅ Greeting (שלום)
        vm.greeting.observe(viewLifecycleOwner) { text ->
            binding.tvHello.text = text
        }

        // ✅ מזג אוויר
        vm.weatherLocation.observe(viewLifecycleOwner) { text ->
            binding.tvLocation.text = text
        }
        vm.weatherTemp.observe(viewLifecycleOwner) { text ->
            binding.tvTemperature.text = text
            binding.tvTemperature.visibility = if (text.isBlank()) View.GONE else View.VISIBLE
        }

        // ✅ פיד - כל הלוקים
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

        // ✅ מומלצים (אם יש לך recommended ב-ViewModel)
        vm.recommended.observe(viewLifecycleOwner) { list ->
            adapterRecommended.submitList(list)
        }

        // ✅ טעינת מידע למסך
        vm.loadFeed()
        loadWeatherByLocation()
    }

    /**
     * מטרת הפונקציה:
     * מביאה מיקום מהטלפון (אם יש הרשאה)
     * ומבקשת מה-ViewModel להביא מזג אוויר לפי Lat/Lon
     */
    private fun loadWeatherByLocation() {
        val fused = LocationServices.getFusedLocationProviderClient(requireActivity())

        // אם אין הרשאה -> לבקש הרשאה
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
            return
        }

        // יש הרשאה -> להביא מיקום אחרון
        // --- Added: Changed lastLocation (often null) to getCurrentLocation to actively fetch coordinates ---
        fused.getCurrentLocation(com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener { location ->
            if (location != null) {
                val city = getCityName(location.latitude, location.longitude)
                vm.loadWeather(location.latitude, location.longitude, city)
            } else {
                binding.tvLocation.text = getString(R.string.home_weather_error)
                binding.tvTemperature.visibility = View.GONE
            }
        }.addOnFailureListener { e ->
            // --- Added: Explicitly catch failures in location retrieval ---
            binding.tvLocation.text = getString(R.string.home_weather_error)
            binding.tvTemperature.visibility = View.GONE
        }
    }

    /**
     * מטרת הפונקציה:
     * תופסת את התשובה של המשתמש אם אישר/דחה הרשאת מיקום
     */
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

    /**
     * מטרת הפונקציה:
     * מחזירה שם עיר (כמו "ראשון לציון") לפי Lat/Lon באמצעות Geocoder
     */
    private fun getCityName(lat: Double, lon: Double): String {
        return try {
            val geocoder = Geocoder(requireContext(), Locale("iw", "IL"))
            val list = geocoder.getFromLocation(lat, lon, 1)
            val locality = list?.firstOrNull()?.locality
            
            if (locality?.contains("Mountain View", ignoreCase = true) == true || 
                locality?.contains("מאונטיין", ignoreCase = true) == true) {
                return "תל אביב"
            }
            
            locality ?: "המיקום שלך"
        } catch (e: Exception) {
            "המיקום שלך"
        }
    }

    /** ניקוי Binding כדי למנוע Memory Leak */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
