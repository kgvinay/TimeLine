package com.test.timeline

import android.Manifest
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import com.google.android.gms.common.ConnectionResult

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.location.places.GeoDataClient
import com.google.android.gms.location.places.PlaceDetectionClient
import com.google.android.gms.location.places.Places
import com.google.android.gms.common.api.GoogleApiClient
import android.annotation.SuppressLint
import android.arch.persistence.room.Room
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationListener
import android.support.v4.content.ContextCompat
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.tasks.Task
import com.test.timeline.db.DbWorkerThread
import com.test.timeline.db.TimeLine
import com.test.timeline.db.TimeLineDatabase


class HomeActivity : AppCompatActivity(),
        OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        ActivityCompat.OnRequestPermissionsResultCallback,
        LocationListener {

    private lateinit var mMap: GoogleMap
    protected var mGeoDataClient: GeoDataClient? = null
    private var mPlaceDetectionClient: PlaceDetectionClient? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var mPermissionDenied = false
    private var locationManager: LocationManager? = null
    private val MIN_TIME: Long = 400
    private val MIN_DISTANCE = 100f
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback
    lateinit var task : Task<LocationSettingsResponse>
    private val REQUEST_CHECK_SETTINGS = 2000
    val PLACE_PICKER_REQUEST = 1000
    lateinit var  timeLineMarker : Marker
    lateinit var  timeLineDatabase : TimeLineDatabase
    private lateinit var mDbWorkerThread: DbWorkerThread
    private val mUiHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        mDbWorkerThread = DbWorkerThread("dbWorkerThread")
        mDbWorkerThread.start()

        setupRoomDatabase()
        setupMapFragment()
        createLocationRequest()
        createLocationCallBack()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            PermissionUtils().requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        } else {
            setupPlaces()
            setupLocationManager()
            fusedLocationClient.lastLocation
                    .addOnSuccessListener { location : Location? ->
                        // Got last known location. In some rare situations this can be null.
                    }
            task.addOnSuccessListener { locationSettingsResponse ->
                fusedLocationClient.requestLocationUpdates(locationRequest,
                        locationCallback, Looper.getMainLooper())
            }

            task.addOnFailureListener { exception ->
                if (exception is ResolvableApiException) {
                    exception.startResolutionForResult(this, REQUEST_CHECK_SETTINGS)
                }
            }
        }

        val dataFromDB = fetchTimeLineDataFromDb("12345")
        Toast.makeText(this,"DD : "+dataFromDB,Toast.LENGTH_SHORT).show()
        if(dataFromDB.isNotEmpty()){
            for( timeLine in dataFromDB) {
                val currentLocation = LatLng(timeLine.lat, timeLine.lon)
                mMap.addMarker(MarkerOptions().position(currentLocation).title(timeLine.name))
            }
        }


    }

    private fun setupRoomDatabase() {
        timeLineDatabase = Room.databaseBuilder(
                this,
                TimeLineDatabase::class.java, "timeline"
        ).build()

    }

    fun createLocationRequest() {
        locationRequest = LocationRequest().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        task = client.checkLocationSettings(builder.build())

    }

    fun createLocationCallBack() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                //Do what you want with the position here
            }
        }
    }



    @SuppressLint("MissingPermission")
    private fun setupLocationManager() {
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager!!.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                MIN_TIME, MIN_DISTANCE, this)
    }

    private fun setupPlaces() {
        mGeoDataClient = Places.getGeoDataClient(this)
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this)

        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build()
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)
        enableLocationPermissions()
    }

    private fun enableLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            PermissionUtils().requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true)
        } else if (mMap != null) {
            mMap.isMyLocationEnabled = true
        }
    }

    override fun onConnectionFailed(p0: ConnectionResult) {

    }

    override fun onMyLocationClick(location: Location) {

        val currentLocation = LatLng(location.latitude, location.longitude)
        mMap.addMarker(MarkerOptions().position(currentLocation).title("Your Location"))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((currentLocation), 15.0f))
    }

    override fun onMyLocationButtonClick(): Boolean {
         return false
    }


    override fun onLocationChanged(location: Location?) {

        val newLocation = LatLng(location!!.latitude, location.longitude)
        timeLineMarker = mMap.addMarker(MarkerOptions().position(newLocation))
        timeLineMarker.tag =
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((newLocation), 15.0f))
        locationManager!!.removeUpdates(this);
    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {

    }

    override fun onProviderEnabled(p0: String?) {

    }

    override fun onProviderDisabled(p0: String?) {

    }


    override fun onResumeFragments() {
        super.onResumeFragments()
        if (mPermissionDenied) {
            showMissingPermissionError()
            mPermissionDenied = false
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return
        }

        if (PermissionUtils().isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_FINE_LOCATION) &&
                PermissionUtils().isPermissionGranted(permissions, grantResults,
                        Manifest.permission.ACCESS_COARSE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableLocationPermissions()
            setupLocationManager()
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true
        }
    }

    /**
     * Displays a dialog with error message explaining that the location permission is missing.
     */
    private fun showMissingPermissionError() {
        PermissionDeniedDialog()
                .newInstance(true).show(supportFragmentManager, "dialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val place = PlacePicker.getPlace(this, data)
                val selectedLatLong = place.latLng
                val selectedPlaceName = place.name

                val timeLine = TimeLine("12345",
                        selectedPlaceName.toString(),
                        selectedLatLong.latitude,
                        selectedLatLong.longitude,
                        "0930",
                        "1830")

                val task = Runnable { timeLineDatabase?.timeLineDao()?.saveTimeLine(timeLine) }
                mDbWorkerThread.postTask(task)

                mMap.addMarker(MarkerOptions().position(selectedLatLong).title(selectedPlaceName.toString()))
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom((selectedLatLong), 15.0f))
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_home, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menu_add -> {

                 val builder = PlacePicker.IntentBuilder();
                startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);

                return true
            }

            R.id.menu_display -> {
                fetchTimeLineDataFromDb("12345")
//                 val timeLineList = timeLineDatabase.timeLineDao().fetchTimeLineByDate("12345")
//                 Toast.makeText(this,timeLineList.get(0).name,Toast.LENGTH_SHORT).show()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun fetchTimeLineDataFromDb(dateString : String) : List<TimeLine> {
        var timeLineData : List<TimeLine> = emptyList()
        val task = Runnable {
            timeLineData =
                    timeLineDatabase?.timeLineDao()?.getTimeLineForDate(dateString)
//            mUiHandler.post({
//                if (timeLineData == null || timeLineData?.size == 0) {
//                    Toast.makeText(this,"No data in cache..!!", Toast.LENGTH_SHORT).show()
//                } else {
//
//                    Toast.makeText(this,"DDHGYRE : "+timeLineData.get(0).name, Toast.LENGTH_SHORT).show()
//                }
//            })
        }
        mDbWorkerThread.postTask(task)
        return timeLineData
    }

}
