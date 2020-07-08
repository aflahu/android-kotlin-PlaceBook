package com.example.placebook.ui

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.location.Location
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.placebook.R
import com.example.placebook.adapter.BookMarkInfoWindowAdapter
import com.example.placebook.adapter.BookmarkListAdapter
import com.example.placebook.viewmodel.MapsViewModel
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.maps.model.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.net.FetchPhotoRequest
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import kotlinx.android.synthetic.main.main_view_maps.*
import kotlinx.android.synthetic.main.activity_maps.*
import kotlinx.android.synthetic.main.drawer_view_maps.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var placesClient: PlacesClient
    private val mapsViewModel by viewModels<MapsViewModel>()
    private lateinit var bookmarkListAdapter: BookmarkListAdapter
    private var markers = HashMap<Long, Marker>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        setupToolbar()
        setupLocationClient()
        setupPlacesClient()
        setupNavigationDrawer()
    }

    private fun requestLocationPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_LOCATION
        )
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestLocationPermissions()
        } else {
            mMap.isMyLocationEnabled = true
            fusedLocationClient.lastLocation.addOnCompleteListener {
                val location = it.result
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    val update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
                    mMap.moveCamera(update)
                } else {
                    Log.e(TAG, "no location found")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Log.e(TAG, "Location permission denied")
            }
        }
    }

    private fun displayPoi(pointOfInterest: PointOfInterest) {
        displayPoiGetPlaceStep(pointOfInterest)
    }

    private fun displayPoiGetPlaceStep(pointOfInterest: PointOfInterest) {
        val placeId = pointOfInterest.placeId

        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES

        )

        val request = FetchPlaceRequest.builder(placeId, placeFields).build()

        placesClient.fetchPlace(request).addOnSuccessListener { response ->
            val place = response.place
            displayPoiGetPhotoStep(place)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(
                    TAG,
                    "Place not found: " + exception.message + ", " + "statusCode: " + statusCode
                )
            }
        }
    }

    private fun displayPoiGetPhotoStep(place: Place) {
        val photoMetadata = place.photoMetadatas?.get(0)

        if (photoMetadata == null) {
            displayPoiDisplayStep(place, null)
            return
        }

        val photoRequest = FetchPhotoRequest.builder(photoMetadata)
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_width))
            .setMaxWidth(resources.getDimensionPixelSize(R.dimen.default_image_height)).build()

        placesClient.fetchPhoto(photoRequest).addOnSuccessListener { fetchPhotoResponse ->
            val bitmap = fetchPhotoResponse.bitmap
            displayPoiDisplayStep(place, bitmap)
        }.addOnFailureListener { exception ->
            if (exception is ApiException) {
                val statusCode = exception.statusCode
                Log.e(
                    TAG,
                    "Place not found: " + exception.message + ", " + "statusCode: " + statusCode
                )
            }
        }
    }

    private fun displayPoiDisplayStep(place: Place, photo: Bitmap?) {
        val marker = mMap.addMarker(
            MarkerOptions().position(place.latLng as LatLng).title(place.name)
                .snippet(place.phoneNumber)
        )
        marker?.tag = PlaceInfo(place, photo)
        marker?.showInfoWindow()
    }

    private fun setupMapListeners() {
        mMap.setInfoWindowAdapter(BookMarkInfoWindowAdapter(this))
        mMap.setOnPoiClickListener {
            displayPoi(it)
        }
        mMap.setOnInfoWindowClickListener {
            handleInfoWindowClick(it)
        }
        fab.setOnClickListener { searchAtCurrentLocation() }

        mMap.setOnMapLongClickListener { latLng -> newBookmark(latLng) }
    }

    private fun handleInfoWindowClick(marker: Marker) {
        when (marker.tag) {
            is MapsActivity.PlaceInfo -> {

                val placeInfo = (marker.tag as PlaceInfo)
                if (placeInfo.place != null) {
                    GlobalScope.launch {
                        mapsViewModel.addBookmarkFromPlace(placeInfo.place, placeInfo.image)
                    }
                }
                marker.remove()
            }
            is MapsViewModel.BookmarkView -> {
                val bookmarkMarkerView = (marker.tag as MapsViewModel.BookmarkView)
                marker.hideInfoWindow()
                bookmarkMarkerView.id?.let {
                    startBookmarkDetails(it)
                }
            }
        }
    }

    private fun createBookmarkObserver() {
        mapsViewModel.getBookmarkViews()?.observe(
            this, Observer<List<MapsViewModel.BookmarkView>> {
                mMap.clear()
                markers.clear()
                it?.let {
                    displayAllBookMarks(it)
                    bookmarkListAdapter.setBookmarkData(it)
                }
            }
        )
    }

    private fun displayAllBookMarks(
        bookmarks: List<MapsViewModel.BookmarkView>
    ) {
        for (bookmark in bookmarks) {
            addPlaceMarker(bookmark)
        }
    }

    private fun addPlaceMarker(
        bookmark: MapsViewModel.BookmarkView
    ): Marker? {
        val marker = mMap.addMarker(
            MarkerOptions()
                .position(bookmark.location)
                .title(bookmark.name)
                .snippet(bookmark.phone)
                .icon(bookmark.categoryResourceId?.let {
                    BitmapDescriptorFactory.fromResource(it)
                })
                .alpha(0.8f)
        )

        marker.tag = bookmark
        bookmark.id?.let { markers.put(it, marker) }

        return marker
    }

    fun moveToBookmark(bookmark: MapsViewModel.BookmarkView) {
        drawerLayout.closeDrawer(drawerView)

        val marker = markers[bookmark.id]
        marker?.showInfoWindow()

        val location = Location("")
        location.latitude = bookmark.location.latitude
        location.longitude = bookmark.location.longitude
        updateMapToLocation(location)
    }

    private fun newBookmark(latLng: LatLng) {
        GlobalScope.launch {
            val bookmarkId = mapsViewModel.addBookmark(latLng)
            bookmarkId?.let { startBookmarkDetails(it) }
        }
    }

    private fun updateMapToLocation(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        mMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, 16.0f)
        )
    }

    private fun searchAtCurrentLocation() {
        // 1
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.PHONE_NUMBER,
            Place.Field.PHOTO_METADATAS,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG,
            Place.Field.TYPES
        )
        // 2
        val bounds = RectangularBounds.newInstance(mMap.projection.visibleRegion.latLngBounds)
        try {
            // 3
            val intent = Autocomplete.IntentBuilder(
                AutocompleteActivityMode.OVERLAY, placeFields
            ).setLocationBias(bounds).build(this)

            startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)
        } catch (e: GooglePlayServicesRepairableException) {

        } catch (e: GooglePlayServicesNotAvailableException) {

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // 1
        when (requestCode) {
            AUTOCOMPLETE_REQUEST_CODE ->
                // 2
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // 3
                    val place = Autocomplete.getPlaceFromIntent(data)

                    // 4
                    val location = Location("")
                    location.latitude = place.latLng?.latitude ?: 0.0
                    location.longitude = place.latLng?.longitude ?: 0.0
                    updateMapToLocation(location)

                    // 5
                    displayPoiGetPhotoStep(place)
                }
        }
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setupMapListeners()
        createBookmarkObserver()
        getCurrentLocation()
    }

    private fun setupPlacesClient() {
        Places.initialize(applicationContext, getString(R.string.google_maps_key))
        placesClient = Places.createClient(this)
    }

    private fun startBookmarkDetails(bookmarkId: Long) {
        val intent = Intent(this, BookmarkDetailsActivity::class.java)
        intent.putExtra(EXTRA_BOOKMARK_ID, bookmarkId)
        startActivity(intent)
    }

    private fun setupLocationClient() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )
        toggle.syncState()
    }


    private fun setupNavigationDrawer() {
        var layoutManager = LinearLayoutManager(this)
        bookmarkRecyclerView.layoutManager = layoutManager
        bookmarkListAdapter = BookmarkListAdapter(null, this)
        bookmarkRecyclerView.adapter = bookmarkListAdapter
    }

    class PlaceInfo(val place: Place? = null, val image: Bitmap? = null)

    companion object {
        const val EXTRA_BOOKMARK_ID = "com.example.placebook.EXTRA_BOOKMARK_ID"
        private const val REQUEST_LOCATION = 1
        private const val TAG = "MapsActivity"
        private const val AUTOCOMPLETE_REQUEST_CODE = 2
    }
}