package com.derosa.progettolam.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.derosa.progettolam.R
import com.derosa.progettolam.activities.RecordActivity
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.library.BuildConfig
import org.osmdroid.views.MapView
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker

class Mappa : Fragment() {

    private lateinit var map: MapView
    private val coordinatesList = listOf(
        GeoPoint(37.7749, -122.4194), // San Francisco
        GeoPoint(34.0522, -118.2437), // Los Angeles
        GeoPoint(40.7128, -74.0060)  // New York
        // Add more coordinates here
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance().userAgentValue = BuildConfig.APPLICATION_ID
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mappa, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        map = view.findViewById(R.id.map)
        map.setMultiTouchControls(true)

        val bologna = GeoPoint(44.4949, 11.3426)
        map.controller.setZoom(15.0)
        map.controller.setCenter(bologna)

        val marker = Marker(map)
        marker.position = bologna
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = "Marker at Bologna"
        marker.icon = requireContext().resources.getDrawable(R.drawable.maps_marker)
        map.overlays.add(marker)

        marker.setOnMarkerClickListener(object : Marker.OnMarkerClickListener {
            override fun onMarkerClick(marker: Marker, mapView: MapView): Boolean {
                if (marker.position == bologna) {
                    val intent = Intent(activity, RecordActivity::class.java)
                    startActivity(intent)
                    return true
                }
                return false
            }
        })
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
