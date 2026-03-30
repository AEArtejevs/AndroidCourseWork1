package com.example.coursework.ui.reminder

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.location.Geocoder
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.coursework.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale

class ReminderDetailFragment : Fragment() {

    private val viewModel: ReminderDetailViewModel by viewModels()
    private val args: ReminderDetailFragmentArgs by navArgs()

    private lateinit var etTitle: EditText
    private lateinit var btnDate: MaterialButton
    private lateinit var btnTime: MaterialButton
    private lateinit var btnSave: Button
    private lateinit var tvLastModified: TextView
    private lateinit var btnBack: ImageButton
    private lateinit var toggleImportant: ToggleButton
    private lateinit var switchAlert: SwitchMaterial
    private lateinit var switchMap: SwitchMaterial
    private lateinit var layoutMap: LinearLayout
    private lateinit var layoutAlertToggle: LinearLayout
    private lateinit var etLocationSearch: EditText
    private lateinit var mapView: MapView
    private lateinit var scheduler: ReminderScheduler
    private lateinit var geofenceManager: GeofenceManager

    private var technicalDate: String = ""
    private var selectedTime: String = ""
    private var selectedLatLng: LatLng? = null
    private var selectedLocationName: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduler = ReminderScheduler(requireContext())
        geofenceManager = GeofenceManager(requireContext())

        etTitle = view.findViewById(R.id.etTitle)
        btnDate = view.findViewById(R.id.btnDate)
        btnTime = view.findViewById(R.id.btnTime)
        btnSave = view.findViewById(R.id.btnSave)
        tvLastModified = view.findViewById(R.id.tvLastModified)
        toggleImportant = view.findViewById(R.id.toggleImportant)
        switchAlert = view.findViewById(R.id.switchAlert)
        switchMap = view.findViewById(R.id.switchMap)
        layoutMap = view.findViewById(R.id.layoutMap)
        layoutAlertToggle = view.findViewById(R.id.layoutAlertToggle)
        etLocationSearch = view.findViewById(R.id.etLocationSearch)
        mapView = view.findViewById(R.id.mapView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
        }

        switchMap.setOnCheckedChangeListener { _, isChecked ->
            updateUiVisibility(isChecked)
            if (isChecked) mapView.onResume() else mapView.onPause()
        }

        etLocationSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString()
                if (query.length > 2) {
                    lifecycleScope.launch {
                        val coords = getCoordinatesFromName(query)
                        if (coords != null) {
                            selectedLatLng = LatLng(coords.first, coords.second)
                            selectedLocationName = query
                            mapView.getMapAsync { googleMap ->
                                googleMap.clear()
                                selectedLatLng?.let { latLng ->
                                    googleMap.addMarker(MarkerOptions().position(latLng).title(query))
                                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                                }
                            }
                        }
                    }
                }
            }
        })

        viewModel.loadReminder(args.reminderId)

        lifecycleScope.launch {
            viewModel.reminder.collect { reminder ->
                reminder?.let {
                    etTitle.setText(it.title)
                    technicalDate = it.date
                    selectedTime = it.time
                    toggleImportant.isChecked = it.isImportant
                    switchAlert.isChecked = it.hasAlert
                    switchMap.isChecked = it.hasLocationAlert

                    if (it.hasLocationAlert) {
                        selectedLocationName = it.locationName
                        etLocationSearch.setText(it.locationName)
                        if (it.latitude != null && it.longitude != null) {
                            selectedLatLng = LatLng(it.latitude, it.longitude)
                            mapView.getMapAsync { googleMap ->
                                googleMap.clear()
                                val latLng = LatLng(it.latitude, it.longitude)
                                googleMap.addMarker(MarkerOptions().position(latLng).title(it.locationName))
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                            }
                        }
                    }

                    updateUiVisibility(it.hasLocationAlert)

                    // Display friendly date
                    try {
                        val parts = it.date.split("-")
                        if (parts.size == 3) {
                            btnDate.text = "${parts[2]}/${parts[1].toInt()}/${parts[0]}"
                        } else {
                            btnDate.text = it.date
                        }
                    } catch (e: Exception) {
                        btnDate.text = it.date
                    }

                    btnTime.text = it.time
                }
            }
        }

        val calendar = Calendar.getInstance()

        btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), { _, y, m, d ->
                    technicalDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    btnDate.text = "$d/${m + 1}/$y"
                    btnDate.error = null
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btnTime.setOnClickListener {
            TimePickerDialog(
                requireContext(), { _, h, min ->
                    selectedTime = String.format(Locale.US, "%02d:%02d", h, min)
                    btnTime.text = selectedTime
                    btnTime.error = null
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            ).show()
        }

        btnSave.setOnClickListener {
            val title = etTitle.text.toString()
            var isValid = true

            if (title.isBlank()) {
                etTitle.error = "Required"
                isValid = false
            }

            if (!switchMap.isChecked) {
                if (technicalDate.isBlank()) {
                    btnDate.error = "Required"
                    isValid = false
                }
                if (selectedTime.isBlank()) {
                    btnTime.error = "Required"
                    isValid = false
                }
            } else {
                if (selectedLatLng == null) {
                    etLocationSearch.error = "Please select a valid location"
                    isValid = false
                }
            }

            if (!isValid) return@setOnClickListener

            val currentReminder = viewModel.reminder.value
            if (currentReminder != null) {
                val updatedReminder = currentReminder.copy(
                    title = title,
                    date = if (switchMap.isChecked) "" else technicalDate,
                    time = if (switchMap.isChecked) "" else selectedTime,
                    isImportant = toggleImportant.isChecked,
                    hasAlert = if (switchMap.isChecked) false else switchAlert.isChecked,
                    hasLocationAlert = switchMap.isChecked,
                    locationName = if (switchMap.isChecked) selectedLocationName else null,
                    latitude = if (switchMap.isChecked) selectedLatLng?.latitude else null,
                    longitude = if (switchMap.isChecked) selectedLatLng?.longitude else null,
                    radius = if (switchMap.isChecked) 100f else null
                )

                viewModel.updateReminder(updatedReminder)

                // Update alarm schedule
                scheduler.cancel(updatedReminder.id)
                if (updatedReminder.hasAlert) {
                    scheduler.schedule(updatedReminder)
                }

                // Update Geofence
                geofenceManager.removeGeofence(updatedReminder.id)
                if (updatedReminder.hasLocationAlert && updatedReminder.latitude != null && updatedReminder.longitude != null) {
                    geofenceManager.addGeofence(updatedReminder)
                }

                findNavController().navigateUp()
            }
        }
    }

    private fun updateUiVisibility(isMapEnabled: Boolean) {
        layoutMap.visibility = if (isMapEnabled) View.VISIBLE else View.GONE
        btnDate.visibility = if (isMapEnabled) View.GONE else View.VISIBLE
        btnTime.visibility = if (isMapEnabled) View.GONE else View.VISIBLE
        layoutAlertToggle.visibility = if (isMapEnabled) View.GONE else View.VISIBLE
    }

    private suspend fun getCoordinatesFromName(name: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(name, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                return@withContext Pair(address.latitude, address.longitude)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}
