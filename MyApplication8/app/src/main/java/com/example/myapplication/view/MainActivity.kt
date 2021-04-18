package com.example.myapplication.view

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.car.Car
import android.car.hardware.CarSensorEvent
import android.car.hardware.CarSensorManager
import android.content.ComponentName
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.animation.BounceInterpolator
import android.widget.PopupMenu
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.example.myapplication.R
import com.example.myapplication.data.db.DestinationDatabase
import com.example.myapplication.data.db.entities.DestinationEntity
import com.example.myapplication.data.repositories.DestinationRepository
import com.example.myapplication.view.destinationlist.AddDestinationSaveDialog
import com.example.myapplication.view.destinationlist.AddDialogListener
import com.example.myapplication.view.destinationlist.DestinationViewModel
import com.example.myapplication.view.destinationlist.DestinationViewModelFactory
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.metrics.MetricsImpl
import com.mapbox.api.directions.v5.DirectionsCriteria
import com.mapbox.api.directions.v5.models.BannerInstructions
import com.mapbox.api.directions.v5.models.DirectionsRoute
import com.mapbox.api.directions.v5.models.RouteOptions
import com.mapbox.api.directions.v5.models.VoiceInstructions
import com.mapbox.geojson.LineString
import com.mapbox.geojson.Point.fromJson
import com.mapbox.geojson.Point.fromLngLat
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraPosition
import com.mapbox.mapboxsdk.camera.CameraUpdate
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
/*import com.mapbox.mapboxsdk.location.LocationComponent
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode*/
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.expressions.Expression.*
import com.mapbox.mapboxsdk.style.layers.LineLayer
import com.mapbox.mapboxsdk.style.layers.Property.LINE_CAP_ROUND
import com.mapbox.mapboxsdk.style.layers.Property.LINE_JOIN_ROUND
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonOptions
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import com.mapbox.mapboxsdk.utils.BitmapUtils
import com.mapbox.navigation.base.TimeFormat.TWENTY_FOUR_HOURS
import com.mapbox.navigation.base.formatter.DistanceFormatter
import com.mapbox.navigation.base.internal.extensions.applyDefaultParams
import com.mapbox.navigation.base.internal.extensions.coordinates
import com.mapbox.navigation.base.internal.route.RouteUrl
import com.mapbox.navigation.base.trip.model.RouteLegProgress
import com.mapbox.navigation.base.trip.model.RouteProgress
import com.mapbox.navigation.base.trip.model.alert.RouteAlert
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.arrival.ArrivalObserver
import com.mapbox.navigation.core.directions.session.RoutesRequestCallback
import com.mapbox.navigation.core.fasterroute.FasterRouteObserver
import com.mapbox.navigation.core.trip.session.*
import com.mapbox.navigation.ui.NavigationViewOptions
import com.mapbox.navigation.ui.camera.NavigationCamera
import com.mapbox.navigation.ui.instruction.GuidanceViewImageProvider
import com.mapbox.navigation.ui.map.NavigationMapboxMap
import com.mapbox.navigation.ui.map.WayNameView
import com.mapbox.navigation.ui.voice.SpeechPlayer
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.internal.notify
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), OnMapReadyCallback,
    MapboxMap.OnMapLongClickListener {

    private lateinit var car: Car

    //private var color: Int? = 2131034319
    private var gearPosition = 8
    private lateinit var seriesSpeed: LineGraphSeries<DataPoint>
    private var x = 0
    private var y = 0

    /*START Fuel level sensor - progess bar*/
    private var fuelLevel: Float = 0F
    private var currentProgress: Int = 0
    /*END*/

    private var handler: Handler = Handler()
    private var runnable: Runnable = Runnable { }

    /*START mapbox navigacija*/
    private lateinit var mapView: MapView
    private lateinit var mapBox: MapboxMap
    private lateinit var locationComponent: LocationComponent
    private var mapboxNavigation: MapboxNavigation? = null
    private var navigationMapboxMap: NavigationMapboxMap? = null
    private var speechPlayer: SpeechPlayer? = null
    private val imageProvider = GuidanceViewImageProvider()
    private lateinit var destinationPoint: com.mapbox.geojson.Point
    private lateinit var originPoint: com.mapbox.geojson.Point
    /*END*/

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("ResourceType", "LongLogTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))
        setContentView(R.layout.activity_main)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val database = DestinationDatabase(this)
        val repository = DestinationRepository(database)
        val factory = DestinationViewModelFactory(repository)
        val viewModel = ViewModelProviders.of(this, factory).get(DestinationViewModel::class.java)


        val navigationOptions = MapboxNavigation
            .defaultNavigationOptionsBuilder(this, getString(R.string.mapbox_access_token))
            .locationEngine(LocationEngineProvider.getBestLocationEngine(this))
            .timeFormatType(TWENTY_FOUR_HOURS)
            .build()

        mapboxNavigation = MapboxNavigationProvider.create(navigationOptions).apply {
            registerRouteProgressObserver(routeProgressObserver)
            registerBannerInstructionsObserver(bannerInstructionsObserver)
            registerTripSessionStateObserver(tripSessionStateObserver)
            attachFasterRouteObserver(fasterRouteObserver)
            registerOffRouteObserver(offRouteObserver)
            registerArrivalObserver(arrivalObserver)
        }

        //Snackbar.make(container, R.string.msg_long_press_map_to_place_waypoint, LENGTH_SHORT).show()

        setOnButtonClick()

        retriveDestinationsFromDatabase(viewModel)
        wNVDurationRemaining.updateVisibility(false)
        wNVDistanceRemaining.updateVisibility(false)
        instructionView.isEnabled = false

        init()

        startRunnable()
    }

    @SuppressLint("MissingPermission")
    private fun setOnButtonClick() {
        buttonStartNav.setOnClickListener {
            updateCameraOnNavigationStateChange(true)
            mapboxNavigation!!.startTripSession()
            buttonStartNav.visibility = View.GONE
            buttonEndNav.visibility = View.VISIBLE

            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(originPoint.latitude(), originPoint.longitude()))
                .zoom(16.5)
                .build()
            mapBox.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        }

        buttonEndNav.setOnClickListener {
            instructionView.visibility = View.GONE
            mapboxNavigation!!.stopTripSession()
            buttonEndNav.visibility = View.GONE
            buttonStartNav.visibility = View.VISIBLE
        }
    }

    @SuppressLint("MissingPermission")
    private fun retriveDestinationsFromDatabase(viewModel: DestinationViewModel) {
        val popupMenu = PopupMenu(this, fabOpenDialog)
        viewModel.getAllDestinations().observe(this, Observer { Destinations ->
            popupMenu.menu.clear()
            for(destination in Destinations) {
                popupMenu.menu.add(Menu.NONE, destination.id!!, destination.id!!, destination.destinationName)
            }
        })

        popupMenu.setOnMenuItemClickListener {menuItem ->
            val lat = viewModel.getDestinationLat(menuItem.title.toString())
            val lng = viewModel.getDestinationLng(menuItem.title.toString())
            val latlng = LatLng(lat, lng)
            Log.d("latlng", "Book")
            onMapLongClick(latlng)
        }

        favBtn.setOnClickListener {
            popupMenu.show()
        }

        fabOpenDialog.isEnabled = false
        fabOpenDialog.setOnClickListener {
            AddDestinationSaveDialog(this, object : AddDialogListener {
                override fun onAddSaveButtonClick(destination: DestinationEntity) {
                    viewModel.upsert(destination)
                }
            }, destinationPoint).show()
        }
    }

    private fun updateCameraOnNavigationStateChange(navigationStarted: Boolean) {
        navigationMapboxMap?.apply {
            if (navigationStarted) {
                updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NORTH)
                updateLocationLayerRenderMode(RenderMode.GPS)
            } else {
                updateCameraTrackingMode(NavigationCamera.NAVIGATION_TRACKING_MODE_NONE)
                updateLocationLayerRenderMode(RenderMode.COMPASS)
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(mapboxMap: MapboxMap) {
        mapBox = mapboxMap
        mapBox.setStyle(Style.MAPBOX_STREETS) { style ->
            findCurrentLocation(style)

            style.addSource(GeoJsonSource("CLICK_SOURCE"))
            style.addSource(GeoJsonSource("ROUTE_LINE_SOURCE_ID", GeoJsonOptions().withLineMetrics(true)))
            style.addImage("ICON_ID", BitmapUtils.getBitmapFromDrawable(ContextCompat.getDrawable(this, R.drawable.mapbox_marker_icon_default))!!)

            style.addLayerBelow(
                LineLayer("ROUTE_LAYER_ID", "ROUTE_LINE_SOURCE_ID")
                    .withProperties(
                        lineCap(LINE_CAP_ROUND),
                        lineJoin(LINE_JOIN_ROUND),
                        lineWidth(6f),
                        lineGradient(
                            interpolate(
                                linear(),
                                lineProgress(),
                                stop(1.0f, color(Color.RED)),
                                stop(5.0f, color(Color.BLUE)),
                                stop(10.0f, color(Color.GREEN))
                            )
                        )
                    ),
                "mapbox-location-shadow-layer"
            )

            style.addLayerAbove(
                SymbolLayer("CLICK_LAYER", "CLICK_SOURCE")
                    .withProperties(
                        iconImage("ICON_ID"),
                        textField("Destination")
                    ),
                "ROUTE_LAYER_ID"
            )

            mapBox.addOnMapLongClickListener(this)
        }
    }

    override fun onMapLongClick(point: LatLng): Boolean {
        mapBox.getStyle {style ->
            val clickPointSource = style.getSourceAs<GeoJsonSource>("CLICK_SOURCE")
            clickPointSource?.setGeoJson(fromLngLat(point.longitude, point.latitude))
        }

        destinationPoint = fromLngLat(point.longitude, point.latitude)

        locationComponent.lastKnownLocation?.let { originLocation ->
            originPoint = fromLngLat( originLocation.longitude, originLocation.latitude)
            //val destinationPoint = fromLngLat(point.longitude, point.latitude)
            mapboxNavigation?.requestRoutes(  // requestRoutes() -> služi za requestanje ruta sa Mapbox Directions APIja
                RouteOptions.builder().applyDefaultParams()   // RouteOptions.builder() -> služi za kreiranje RouteOptions klase
                    .accessToken(getString(R.string.mapbox_access_token))
                    .coordinates(originPoint, null, destinationPoint)
                    .geometries(RouteUrl.GEOMETRY_POLYLINE6)
                    .profile(RouteUrl.PROFILE_DRIVING)
                    .alternatives(true)
                    .voiceInstructions(true)
                    .bannerInstructions(true)
                    .steps(true)
                    .voiceUnits(DirectionsCriteria.METRIC)
                    .continueStraight(true)
                    .build(),
                routesReqCallback  // RoutesRequestCallback interface object -> služi za informiranje korisnika o statusu "route request"
            )

            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(destinationPoint.latitude(), destinationPoint.longitude()))
                .zoom(16.5)
                .build()
            mapBox.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            fabOpenDialog.isEnabled = true

        }

        return true
    }


    @SuppressLint("MissingPermission")
    private fun findCurrentLocation(mapStyle: Style) {

        val customLocationComponentOptions = LocationComponentOptions.builder(this)
            .pulseEnabled(true)
            .pulseColor(Color.GREEN)
            .pulseAlpha(.4f)
            .pulseInterpolator(BounceInterpolator())
            .build()

        val locationComponentActivationOptions =
            LocationComponentActivationOptions.builder(this, mapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

        locationComponent = mapBox.locationComponent.apply {
            activateLocationComponent(locationComponentActivationOptions)
            isLocationComponentEnabled = true
            cameraMode = CameraMode.TRACKING
            renderMode = RenderMode.COMPASS
            Log.d("location1", this.lastKnownLocation?.latitude.toString())
        }
    }

    private fun init() {
        //Progress bar of speed
        pbBatteryLevel.max = 65000

        //Graph pf speed
        seriesSpeed = LineGraphSeries()

        car = Car.createCar(this, object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val carSensorManager = car.getCarManager(Car.SENSOR_SERVICE) as CarSensorManager

                CoroutineScope(Dispatchers.IO).launch {
                    carSensorManager.registerListener(
                        carSensorListener,
                        CarSensorManager.SENSOR_TYPE_CAR_SPEED,
                        CarSensorManager.SENSOR_RATE_NORMAL
                    )

                    carSensorManager.registerListener(
                        carSensorListener,
                        CarSensorManager.SENSOR_TYPE_ENV_OUTSIDE_TEMPERATURE,
                        CarSensorManager.SENSOR_RATE_NORMAL
                    )

                    carSensorManager.registerListener(
                        carSensorListener,
                        CarSensorManager.SENSOR_TYPE_PARKING_BRAKE,
                        CarSensorManager.SENSOR_RATE_NORMAL
                    )

                    carSensorManager.registerListener(
                        carSensorListener,
                        CarSensorManager.SENSOR_TYPE_IGNITION_STATE,
                        CarSensorManager.SENSOR_RATE_NORMAL
                    )

                    carSensorManager.registerListener(
                        carSensorListener,
                        CarSensorManager.SENSOR_TYPE_GEAR,
                        CarSensorManager.SENSOR_RATE_NORMAL
                    )

                    carSensorManager.registerListener(
                        carSensorListener,
                        CarSensorManager.SENSOR_TYPE_FUEL_LEVEL,
                        CarSensorManager.SENSOR_RATE_NORMAL
                    )
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                TODO("Not yet implemented")
            }
        })

        car.connect()

    }


    private val carSensorListener = object : CarSensorManager.OnSensorChangedListener {
        @SuppressLint("SetTextI18n")
        override fun onSensorChanged(carEvent: CarSensorEvent?) {
            if (carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_CAR_SPEED) {

                speed.text = (carEvent.floatValues[0] * 3.6f).toInt()
                    .toString() + " Km/h"
                saSpeed.progress = ((carEvent.floatValues[0] * 3.6f) / 2.5).toInt()
                y = (carEvent.floatValues[0] * 3.6f).toInt()
            }

            //PARKING BRAKE SENSOR
            if (carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_PARKING_BRAKE) {
                if (carEvent.intValues[0] == 1) {
                    ivParkingBrake.setColorFilter(getColor(this@MainActivity, R.color.red))
                } else {
                    ivParkingBrake.setColorFilter(getColor(this@MainActivity, R.color.white))
                }
            }

            //IGNITION STATE SENSOR
            if (carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_IGNITION_STATE) {
                if (carEvent.intValues[0] == 1) {
                    ivIgnition.setColorFilter(getColor(this@MainActivity, R.color.red))
                } else {
                    ivIgnition.setColorFilter(getColor(this@MainActivity, R.color.white))
                }
            }

            //OUTSIDE TEMPERATURE SENSOR
            if (carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_ENV_OUTSIDE_TEMPERATURE) {
                tvTemp.text = carEvent.floatValues[0].toString() + "°C"
            }

            //GEAR SENSOR
            if (carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_GEAR) {
                tvNeutral.setTextColor(Color.WHITE)
                tvReverse.setTextColor(Color.WHITE)
                tvPark.setTextColor(Color.WHITE)
                tvDrive.setTextColor(Color.WHITE)

                when (carEvent.intValues[0]) {
                    1 -> tvNeutral.setTextColor(Color.RED)
                    2 -> tvReverse.setTextColor(Color.RED)
                    4 -> tvPark.setTextColor(Color.RED)
                    8 -> tvDrive.setTextColor(Color.RED)
                }
            }

            //FUEL LEVEL SENSOR
            if (carEvent?.sensorType == CarSensorManager.SENSOR_TYPE_FUEL_LEVEL) {
                if (carEvent.floatValues[0] <= 65000) {
                    currentProgress =  carEvent.floatValues[0].toInt()

                    ObjectAnimator.ofInt(pbBatteryLevel, "progress", currentProgress)
                        .setDuration(2000)
                        .start()

                    fuelLevel = (carEvent.floatValues[0] / 65000) * 100
                    tvFuelLevel.text = "Fuel level: ${(fuelLevel).toInt()}%"
                    if (carEvent.floatValues[0] <= 4062) {
                        ivFuel.setColorFilter(getColor(this@MainActivity, R.color.red))
                    } else {
                        ivFuel.setColorFilter(getColor(this@MainActivity, R.color.white))
                    }
                } else {

                    ObjectAnimator.ofInt(pbBatteryLevel, "progress", 65000)
                        .setDuration(2000)
                        .start()
                }
            }
        }
    }

    fun startRunnable() {
        runnable = object : Runnable {
            override fun run() {
                seriesSpeed.appendData(DataPoint(x.toDouble(), y.toDouble()), false, 5)
                x += 1
                graphSpeed.addSeries(seriesSpeed)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    /*--------------------------------------------------------INERFACES--------------------------------------------*/

    private val routesReqCallback = object : RoutesRequestCallback {

        override fun onRoutesReady(routes: List<DirectionsRoute>) {
            Log.d("routes", routes[0].toString())
            if (routes.isNotEmpty()) {
                mapBox.getStyle {
                    val clickPointSource = it.getSourceAs<GeoJsonSource>("ROUTE_LINE_SOURCE_ID")
                    val routeLineString = LineString.fromPolyline(
                        routes[0].geometry()!!,
                        6
                    )
                    clickPointSource?.setGeoJson(routeLineString)
                }
            }
            route_retrieval_progress_spinner.visibility = View.INVISIBLE
            mapBox.moveCamera(CameraUpdateFactory.zoomTo(15.0))
        }

        override fun onRoutesRequestFailure(throwable: Throwable, routeOptions: RouteOptions) {

        }

        override fun onRoutesRequestCanceled(routeOptions: RouteOptions) {

        }
    }

    private val routeProgressObserver = object : RouteProgressObserver {
        override fun onRouteProgressChanged(routeProgress: RouteProgress) {
            instructionView.updateDistanceWith(routeProgress)
            wNVDurationRemaining.updateVisibility(true)
            wNVDistanceRemaining.updateVisibility(true)

            val durationRemaining: String = if(routeProgress.durationRemaining < 3600)
                (TimeUnit.SECONDS.toMinutes(routeProgress.durationRemaining.toLong()).toInt()).toString() + "min"
            else
                (TimeUnit.SECONDS.toHours(routeProgress.durationRemaining.toLong()).toInt()).toString() + "h"

            val distanceRemaining: String = if(routeProgress.distanceRemaining < 1000)
                String.format("%.2f", routeProgress.distanceRemaining) + "ft"
            else
                String.format("%.2f", routeProgress.distanceRemaining / 5280) + "mi"

            wNVDurationRemaining.updateWayNameText("Time remaining: " + durationRemaining)
            wNVDistanceRemaining.updateWayNameText("Distance remaining: " + distanceRemaining.toString())
            Log.d("progess", routeProgress.distanceRemaining.toString())
        }
    }

    private val tripSessionStateObserver = object: TripSessionStateObserver {
        override fun onSessionStateChanged(tripSessionState: TripSessionState) {
            when(tripSessionState) {
                TripSessionState.STARTED -> {
                    if (mapboxNavigation!!.getRoutes().isNotEmpty()) {
                        Log.d("dsdsdsds", "dsdsdsd")
                        instructionView.visibility = View.VISIBLE
                    }
                }
                TripSessionState.STOPPED -> {
                    instructionView.visibility = View.GONE
                    wNVDurationRemaining.updateVisibility(false)
                    wNVDistanceRemaining.updateVisibility(false)
                    updateCameraOnNavigationStateChange(false)
                }
            }
        }
    }

    private val bannerInstructionsObserver = object: BannerInstructionsObserver {
        override fun onNewBannerInstructions(bannerInstructions: BannerInstructions) {
            Log.d("banner", bannerInstructions.toString())
            instructionView.updateBannerInstructionsWith(bannerInstructions)
            instructionView.toggleGuidanceView(bannerInstructions)
            instructionView.retrieveAlertView()
            instructionView.requestFocusFromTouch()

            if (bannerInstructions != null && imageProvider != null) {
                imageProvider.renderGuidanceView(bannerInstructions, guidanceViewImageProvider)
            }
        }
    }

    private val offRouteObserver = object : OffRouteObserver {
        override fun onOffRouteStateChanged(offRoute: Boolean) {
            if(offRoute == true) {
                Toast.makeText(this@MainActivity, "You are off the route", Toast.LENGTH_LONG).show()
            }
        }
    }

    private val arrivalObserver = object : ArrivalObserver {
        override fun onFinalDestinationArrival(routeProgress: RouteProgress) {
            Log.d("routesss", routeProgress.toString())
        }

        override fun onNextRouteLegStart(routeLegProgress: RouteLegProgress) {
            TODO("Not yet implemented")
        }

    }

    private val fasterRouteObserver = object : FasterRouteObserver {
        override fun onFasterRoute(currentRoute: DirectionsRoute, alternatives: List<DirectionsRoute>, isAlternativeFaster: Boolean) {
            mapboxNavigation?.setRoutes(alternatives)
        }
    }

    private val guidanceViewImageProvider = object: GuidanceViewImageProvider.OnGuidanceImageDownload {
        override fun onGuidanceImageReady(bitmap: Bitmap) {
            //bitmapId.setImageBitmap(bitmap)
        }

        override fun onNoGuidanceImageUrl() {

        }

        override fun onFailure(message: String?) {

        }
    }

    /*-------------------------------------------------------------------------*/

    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
        mapboxNavigation?.unregisterBannerInstructionsObserver(bannerInstructionsObserver)
        mapboxNavigation?.unregisterRouteProgressObserver(routeProgressObserver)
        mapboxNavigation?.unregisterArrivalObserver(arrivalObserver)
        mapboxNavigation?.unregisterOffRouteObserver(offRouteObserver)
        mapboxNavigation?.detachFasterRouteObserver()
        mapboxNavigation?.stopTripSession()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
        if (outState != null) {
            mapView.onSaveInstanceState(outState)
        }
    }
}

