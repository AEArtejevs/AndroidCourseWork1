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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.coursework.R
import com.example.coursework.database.NoteDatabase
import com.example.coursework.database.reminder.ReminderEntity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Locale


class ReminderFragment : Fragment() {
    private val viewModel: ReminderViewModel by viewModels()
    private lateinit var reminderAdapter: ReminderAdapter
    private lateinit var searchAdapter: ReminderAdapter
    private lateinit var bottomBar: LinearLayout
    private lateinit var deleteBar: LinearLayout
    private lateinit var scheduler: ReminderScheduler
    private lateinit var geofenceManager: GeofenceManager

    private var currentMapView: MapView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        scheduler = ReminderScheduler(requireContext())
        geofenceManager = GeofenceManager(requireContext())
        
        bottomBar = view.findViewById(R.id.bottomBar)
        deleteBar = view.findViewById(R.id.deleteBar)

        val mainContentScroll = view.findViewById<NestedScrollView>(R.id.mainContentScroll)
        val recyclerSearchResults = view.findViewById<RecyclerView>(R.id.recyclerSearchResults)

        val summaryRecycler = view.findViewById<RecyclerView>(R.id.recyclerSummary)
        val summaryAdapter = SummaryAdapter(emptyList()) { category ->
            val action = ReminderFragmentDirections.actionNavRemindersToReminderListFragment(category)
            findNavController().navigate(action)
        }
        summaryRecycler.adapter = summaryAdapter

        val remindersRecycler = view.findViewById<RecyclerView>(R.id.recyclerReminders)
        
        val onReminderClick = { reminder: ReminderEntity ->
            val action = ReminderFragmentDirections.actionNavRemindersToReminderDetailFragment(reminder.id)
            findNavController().navigate(action)
        }

        val onSelectionModeChanged = { isSelectionMode: Boolean ->
            if (isSelectionMode) {
                deleteBar.visibility = View.VISIBLE
                bottomBar.visibility = View.GONE
            } else {
                deleteBar.visibility = View.GONE
                bottomBar.visibility = View.VISIBLE
            }
        }

        reminderAdapter = ReminderAdapter(
            items = emptyList(),
            onItemClick = onReminderClick,
            onSelectionModeChanged = onSelectionModeChanged
        )
        remindersRecycler.adapter = reminderAdapter
        remindersRecycler.layoutManager = LinearLayoutManager(requireContext())

        searchAdapter = ReminderAdapter(
            items = emptyList(),
            onItemClick = onReminderClick,
            onSelectionModeChanged = onSelectionModeChanged
        )
        recyclerSearchResults.adapter = searchAdapter
        recyclerSearchResults.layoutManager = LinearLayoutManager(requireContext())

        summaryRecycler.layoutManager = GridLayoutManager(requireContext(), 2)

        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.updateSearchQuery(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        val btnAdd = view.findViewById<ImageButton>(R.id.btnAddReminder)
        btnAdd.setOnClickListener {
            showAddReminderDialog()
        }

        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            deleteSelectedReminders()
        }

        view.findViewById<MaterialButton>(R.id.btnCancelDelete).setOnClickListener {
            reminderAdapter.clearSelection()
            searchAdapter.clearSelection()
        }

        lifecycleScope.launch {
            viewModel.searchQuery.collect { query ->
                if (query.isBlank()) {
                    mainContentScroll.visibility = View.VISIBLE
                    recyclerSearchResults.visibility = View.GONE
                } else {
                    mainContentScroll.visibility = View.GONE
                    recyclerSearchResults.visibility = View.VISIBLE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.searchResults.collect { results ->
                searchAdapter.updateData(results)
            }
        }

        lifecycleScope.launch {
            viewModel.pastReminders.collect { reminders ->
                reminderAdapter.updateData(reminders)
            }
        }

        lifecycleScope.launch {
            viewModel.summaryFlow.collect { summary ->
                summaryAdapter.updateData(summary)
            }
        }
    }

    private fun deleteSelectedReminders() {
        val selectedIds = if (searchAdapter.getSelectedIds().isNotEmpty()) {
            searchAdapter.getSelectedIds()
        } else {
            reminderAdapter.getSelectedIds()
        }

        if (selectedIds.isNotEmpty()) {
            val db = NoteDatabase.getDatabase(requireContext())
            lifecycleScope.launch {
                selectedIds.forEach { 
                    scheduler.cancel(it)
                    geofenceManager.removeGeofence(it)
                }
                db.reminderDao().deleteRemindersByIds(selectedIds)
                reminderAdapter.clearSelection()
                searchAdapter.clearSelection()
            }
        }
    }

    private fun showAddReminderDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_reminder, null)

        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val btnDate = dialogView.findViewById<MaterialButton>(R.id.tvDate)
        val btnTime = dialogView.findViewById<MaterialButton>(R.id.tvTime)
        val layoutAlertToggle = dialogView.findViewById<LinearLayout>(R.id.layoutAlertToggle)
        val toggleImportant = dialogView.findViewById<ToggleButton>(R.id.toggleImportant)
        val switchAlert = dialogView.findViewById<SwitchMaterial>(R.id.switchAlert)
        
        val switchMap = dialogView.findViewById<SwitchMaterial>(R.id.switchMap)
        val layoutMap = dialogView.findViewById<LinearLayout>(R.id.layoutMap)
        val etLocationSearch = dialogView.findViewById<EditText>(R.id.etLocationSearch)
        val mapView = dialogView.findViewById<MapView>(R.id.mapView)
        currentMapView = mapView

        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        var selectedLatLng: LatLng? = null
        var selectedLocationName: String? = null

        mapView.onCreate(null)
        mapView.getMapAsync { googleMap ->
            googleMap.uiSettings.isZoomControlsEnabled = true
        }

        switchMap.setOnCheckedChangeListener { _, isChecked ->
            layoutMap.visibility = if (isChecked) View.VISIBLE else View.GONE
            
            // Toggle visibility of Date/Time selection and Alert toggle
            btnDate.visibility = if (isChecked) View.GONE else View.VISIBLE
            btnTime.visibility = if (isChecked) View.GONE else View.VISIBLE
            layoutAlertToggle?.visibility = if (isChecked) View.GONE else View.VISIBLE
            
            if (isChecked) {
                mapView.onResume()
            } else {
                mapView.onPause()
            }
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

        val calendar = Calendar.getInstance()
        var technicalDate: String? = null
        var technicalTime: String? = null

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()

        btnDate.setOnClickListener {
            DatePickerDialog(
                requireContext(), { _, y, m, d ->
                    technicalDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
                    btnDate.text = String.format(Locale.US, "%d/%d/%d", d, m + 1, y)
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
                    technicalTime = String.format(Locale.US, "%02d:%02d", h, min)
                    btnTime.text = technicalTime
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
                if (technicalDate == null) {
                    btnDate.error = "Required"
                    isValid = false
                }
                if (technicalTime == null) {
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

            lifecycleScope.launch {
                saveReminder(
                    title = title, 
                    date = technicalDate ?: "",
                    time = technicalTime ?: "",
                    isImportant = toggleImportant.isChecked, 
                    hasAlert = if (switchMap.isChecked) false else switchAlert.isChecked,
                    hasLocationAlert = switchMap.isChecked,
                    locationName = selectedLocationName,
                    lat = selectedLatLng?.latitude,
                    lon = selectedLatLng?.longitude
                )
                withContext(Dispatchers.Main) {
                    dialog.dismiss()
                    currentMapView = null
                }
            }
        }

        dialog.show()
    }

    override fun onResume() {
        super.onResume()
        currentMapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        currentMapView?.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentMapView?.onDestroy()
        currentMapView = null
    }

    override fun onLowMemory() {
        super.onLowMemory()
        currentMapView?.onLowMemory()
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

    private suspend fun saveReminder(
        title: String, 
        date: String, 
        time: String, 
        isImportant: Boolean, 
        hasAlert: Boolean,
        hasLocationAlert: Boolean = false,
        locationName: String? = null,
        lat: Double? = null,
        lon: Double? = null
    ) {
        val db = NoteDatabase.getDatabase(requireContext())
        val dao = db.reminderDao()

        val id = dao.insertReminder(
            ReminderEntity(
                title = title,
                date = date,
                time = time,
                isImportant = isImportant,
                hasAlert = hasAlert,
                isCompleted = false,
                hasLocationAlert = hasLocationAlert,
                locationName = locationName,
                latitude = lat,
                longitude = lon,
                radius = 100f
            )
        )
        
        val newReminder = ReminderEntity(
            id = id.toInt(), 
            title = title, 
            date = date, 
            time = time, 
            isImportant = isImportant, 
            hasAlert = hasAlert, 
            isCompleted = false,
            hasLocationAlert = hasLocationAlert, 
            locationName = locationName, 
            latitude = lat, 
            longitude = lon, 
            radius = 100f
        )
        
        if (hasAlert) {
            scheduler.schedule(newReminder)
        }
        
        if (hasLocationAlert && lat != null && lon != null) {
            geofenceManager.addGeofence(newReminder)
        }
    }
}
