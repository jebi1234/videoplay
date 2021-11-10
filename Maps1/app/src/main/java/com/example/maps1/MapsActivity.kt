package  com.example.maps1

import android.Manifest
import com.google.android.gms.common.ConnectionResult

import androidx.annotation.NonNull

import com.google.android.gms.maps.model.MarkerOptions

import com.google.android.gms.maps.model.Polyline

import com.example.maps1.R

import com.google.android.gms.maps.model.LatLng

import com.google.android.gms.maps.model.PolylineOptions

import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.CameraUpdate

import android.widget.Toast

import com.google.android.material.snackbar.Snackbar

import com.google.android.gms.maps.GoogleMap

import android.content.pm.PackageManager
import android.location.Location

import androidx.core.app.ActivityCompat

import androidx.core.content.ContextCompat

import com.google.android.gms.maps.SupportMapFragment

import android.os.Bundle
import android.view.View

import com.google.android.gms.common.api.GoogleApiClient

import com.google.android.gms.maps.OnMapReadyCallback

import androidx.fragment.app.FragmentActivity
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.GoogleMap.OnMyLocationChangeListener


class MainActivity : FragmentActivity(), OnMapReadyCallback,
    OnConnectionFailedListener, RoutingListener {
    //google map object
    private var mMap: GoogleMap? = null

    //current and destination location objects
    var myLocation: Location? = null
    var destinationLocation: Location? = null
    protected var start: LatLng? = null
    protected var end: LatLng? = null
    var locationPermission = false

    //polyline object
    private var polylines: MutableList<Polyline>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        //request location permission.
        requestPermision()

        //init google map fragment to show map.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
    }

    private fun requestPermision() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                LOCATION_REQUEST_CODE
            )
        } else {
            locationPermission = true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            LOCATION_REQUEST_CODE -> {
                if (grantResults.size > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    //if permission granted.
                    locationPermission = true
                    getMyLocation()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }
        }
    }

    //to get user location
    private fun getMyLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        mMap!!.isMyLocationEnabled = true
        mMap!!.setOnMyLocationChangeListener { location ->
            myLocation = location
            val ltlng = LatLng(location.latitude, location.longitude)
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(
                ltlng, 16f
            )
            mMap!!.animateCamera(cameraUpdate)
        }

        //get destination location when user click on map
        mMap!!.setOnMapClickListener { latLng ->
            end = latLng
            mMap!!.clear()
            start = LatLng(myLocation.getLatitude(), myLocation.getLongitude())
            //start route finding
            Findroutes(start, end)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getMyLocation()
    }

    // function to find Routes.
    fun Findroutes(Start: LatLng?, End: LatLng?) {
        if (Start == null || End == null) {
            Toast.makeText(this@MainActivity, "Unable to get location", Toast.LENGTH_LONG).show()
        } else {
            val routing: Routing = Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(Start, End)
                .key("Your Api Key") //also define your api key here.
                .build()
            routing.execute()
        }
    }

    //Routing call back functions.
    fun onRoutingFailure(e: RouteException) {
        val parentLayout: View = findViewById(R.id.content)
        val snackbar: Snackbar = Snackbar.make(parentLayout, e.toString(), Snackbar.LENGTH_LONG)
        snackbar.show()
        //    Findroutes(start,end);
    }

    fun onRoutingStart() {
        Toast.makeText(this@MainActivity, "Finding Route...", Toast.LENGTH_LONG).show()
    }

    //If Route finding success..
    fun onRoutingSuccess(route: ArrayList<Route>, shortestRouteIndex: Int) {
        val center = CameraUpdateFactory.newLatLng(start)
        val zoom = CameraUpdateFactory.zoomTo(16f)
        if (polylines != null) {
            polylines!!.clear()
        }
        val polyOptions = PolylineOptions()
        var polylineStartLatLng: LatLng? = null
        var polylineEndLatLng: LatLng? = null
        polylines = ArrayList()
        //add route(s) to the map using polyline
        for (i in 0 until route.size) {
            if (i == shortestRouteIndex) {
                polyOptions.color(resources.getColor(R.color.black))
                polyOptions.width(7f)
                polyOptions.addAll(route[shortestRouteIndex].getPoints())
                val polyline = mMap!!.addPolyline(polyOptions)
                polylineStartLatLng = polyline.points[0]
                val k = polyline.points.size
                polylineEndLatLng = polyline.points[k - 1]
                (polylines as ArrayList<Polyline>).add(polyline)
            } else {
            }
        }

        //Add Marker on route starting position
        val startMarker = MarkerOptions()
        startMarker.position(polylineStartLatLng!!)
        startMarker.title("My Location")
        mMap!!.addMarker(startMarker)

        //Add Marker on route ending position
        val endMarker = MarkerOptions()
        endMarker.position(polylineEndLatLng!!)
        endMarker.title("Destination")
        mMap!!.addMarker(endMarker)
    }

    fun onRoutingCancelled() {
        Findroutes(start, end)
    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        Findroutes(start, end)
    }

    companion object {
        //to get location permissions.
        private const val LOCATION_REQUEST_CODE = 23
    }
}