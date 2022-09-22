## GPX example for Android ##

This example demonstrates how to reconstruct a route from a GPX track. The [Routing API](https://developer.tomtom.com/assets/downloads/tomtom-sdks/android/api-reference/0.3.344/routing/routing-client-online/com.tomtom.sdk.routing.online/-online-routing-api/index.html) from the Maps SDK uses the `supportingPoints` parameter to reconstruct a route from a sequence of points when planning a route. In the example a GPX file is read from the app `assets` directory, and the track points from the file passed as supporting points when the route is calcuated. The route path returned by the Routing API is drawn on the map. For more information on using supporting points see [Custom routes](https://developer.tomtom.com/android/maps/documentation/guides/routing/waypoints-and-custom-routes#custom-routes) in the SDK documentation.

To simplify parsing the GPX file the example uses the [Android GPX Parser](https://github.com/ticofab/android-gpx-parser) library.

### Running the example ###

1. To run the example you'll need an API key with the **Map Display** and **Routing with Extended Guidance** APIs enabled.

2. Open the project in Android Studio, the file `local.properties` will be generated in your project level directory, and add the following code to local.properties, replacing `YOUR_API_KEY` with your API key.

<code>API\_KEY=*YOUR\_API\_KEY*</code>

3. Save the file and run the app.