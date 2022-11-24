package com.example.androidgpx

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.tomtom.sdk.common.location.GeoPoint
import com.tomtom.sdk.common.vehicle.Vehicle
import com.tomtom.sdk.map.display.MapOptions
import com.tomtom.sdk.map.display.TomTomMap
import com.tomtom.sdk.map.display.route.RouteOptions
import com.tomtom.sdk.map.display.ui.MapFragment
import com.tomtom.sdk.map.display.ui.OnMapReadyCallback
import com.tomtom.sdk.route.Route
import com.tomtom.sdk.routing.*
import com.tomtom.sdk.routing.common.RoutingError
import com.tomtom.sdk.routing.common.options.*
import com.tomtom.sdk.routing.online.OnlineRoutePlanner
import io.ticofab.androidgpxparser.parser.GPXParser
import io.ticofab.androidgpxparser.parser.domain.Gpx
import io.ticofab.androidgpxparser.parser.domain.Track
import java.io.InputStream

class MainActivity : AppCompatActivity() {
    private lateinit var tomTomMap: TomTomMap
    private lateinit var routePlanner: RoutePlanner

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
        routePlanner = OnlineRoutePlanner.create(
            context = this,
            apiKey = resources.getString(R.string.API_KEY)
        )
    }

    private val mapReadyCallback = OnMapReadyCallback { map ->
        tomTomMap = map

        val gpx: Gpx? = readGpx()
        if (gpx != null) {
            val track = gpx.tracks.elementAt(0)
            val geoPoints = trackToGeoPoints(track)
            reconstructRoute(geoPoints)
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

    private fun trackToGeoPoints(track: Track): List<GeoPoint> {
        val geoPoints = mutableListOf<GeoPoint>()
        for (segment in track.trackSegments) {
            for (trackPoint in segment.trackPoints) {
                geoPoints.add(GeoPoint(trackPoint.latitude, trackPoint.longitude))
            }
        }
        return geoPoints
    }

    private fun reconstructRoute(coords: List<GeoPoint>) {
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

        routePlanner.planRoute(planRouteOptions, routePlanningCallback)
    }

    private val routePlanningCallback = object : RoutePlanningCallback {
        override fun onSuccess(result: RoutePlanningResult) {
            val route = result.routes.first()
            drawRoute(route!!)
        }

        override fun onError(error: RoutingError) {
            Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_SHORT).show()
        }

        override fun onRoutePlanned(route: Route) = Unit
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