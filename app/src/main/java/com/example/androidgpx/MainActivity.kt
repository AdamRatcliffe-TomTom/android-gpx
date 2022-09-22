package com.example.androidgpx

import android.os.*
import android.widget.*
import androidx.appcompat.app.*
import com.tomtom.sdk.common.location.*
import com.tomtom.sdk.common.route.Route
import com.tomtom.sdk.maps.display.*
import com.tomtom.sdk.maps.display.route.*
import com.tomtom.sdk.maps.display.ui.*
import com.tomtom.sdk.routing.api.*
import com.tomtom.sdk.routing.common.*
import com.tomtom.sdk.routing.common.options.*
import com.tomtom.sdk.routing.common.options.vehicle.*
import com.tomtom.sdk.routing.online.*
import io.ticofab.androidgpxparser.parser.*
import io.ticofab.androidgpxparser.parser.domain.*
import java.io.*

class MainActivity : AppCompatActivity() {
    private lateinit var tomTomMap: TomTomMap
    private lateinit var routingApi: RoutingApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initMap()
        initRouting()
    }

    private fun initMap() {
        val mapOptions = MapOptions(mapKey = resources.getString(R.string.API_KEY))
        val mapFragment = MapFragment.newInstance(mapOptions)
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment)
            .commit()

        mapFragment.getMapAsync(mapReadyCallback)
    }

    private fun initRouting() {
        routingApi = OnlineRoutingApi.create(
            context = this,
            apiKey = resources.getString(R.string.API_KEY)
        )
    }

    private val mapReadyCallback = OnMapReadyCallback { map ->
        tomTomMap = map

        val gpx: Gpx? = readGpx()
        if (gpx != null) {
            val track = gpx.tracks.elementAt(0)
            val geoCoordinates = trackToGeoCoordinates(track)
            reconstructRoute(geoCoordinates)
        }
    }

    private fun readGpx(): Gpx? {
        val parser = GPXParser()
        var parsedGpx: Gpx? = null

        try {
            val input: InputStream = assets.open(GPX_FILE)
            parsedGpx = parser.parse(input)
        } catch (error: Exception) {
            Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
        }

        return parsedGpx
    }

    private fun trackToGeoCoordinates(track: Track): List<GeoCoordinate> {
        val geoCoordinates = mutableListOf<GeoCoordinate>()
        for (segment in track.trackSegments) {
            for (trackPoint in segment.trackPoints) {
                geoCoordinates.add(GeoCoordinate(trackPoint.latitude, trackPoint.longitude))
            }
        }
        return geoCoordinates
    }

    private fun reconstructRoute(coords: List<GeoCoordinate>) {
        val itinerary = Itinerary(
            origin = coords.first(),
            destination = coords.last(),
        )
        val routeLegOptions = RouteLegOptions(supportingPoints = coords)
        val planRouteOptions = RoutePlanningOptions(
            itinerary = itinerary,
            routeLegOptions = listOf(routeLegOptions),
            vehicle = Vehicle.Car()
        )

        routingApi.planRoute(planRouteOptions, routePlanningCallback)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResult) {
            val route = result.routes.first()
            drawRoute(route!!)
        }

        override fun onError(error: RoutingError) {
            Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawRoute(route: Route) {
        val geometry = route.legs.flatMap { it.points }
        val routeOptions = RouteOptions(geometry = geometry)
        tomTomMap.addRoute(routeOptions)
        tomTomMap.zoomToRoutes(ZOOM_TO_ROUTE_PADDING)
    }

    companion object {
        private const val GPX_FILE = "Lodz.gpx"
        private const val ZOOM_TO_ROUTE_PADDING = 100
    }
}