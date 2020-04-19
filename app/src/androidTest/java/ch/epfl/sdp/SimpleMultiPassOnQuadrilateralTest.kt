package ch.epfl.sdp

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone.SimpleMultiPassOnQuadrilateral
import ch.epfl.sdp.searcharea.QuadrilateralArea
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SimpleMultiPassOnQuadrilateralTest {

    @Test(expected = IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptNegativeMaxDistance() {
        SimpleMultiPassOnQuadrilateral(-10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptZeroMaxDistance() {
        SimpleMultiPassOnQuadrilateral(0.0)
    }

    @Test(expected = java.lang.IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptIncompleteSearchArea() {
        val searchArea = QuadrilateralArea()
        searchArea.addAngle(LatLng(0.0, 0.0))
        SimpleMultiPassOnQuadrilateral(-10.0)
                .createFlightPath(LatLng(0.0, 0.0), searchArea)
    }

    @Test
    fun simpleMultiPassCreatesGoodNumberOfPointsForSmallArea() {
        val searchArea = QuadrilateralArea()
        searchArea.addAngle(LatLng(0.0, 0.0))
        searchArea.addAngle(LatLng(1.0, 0.0))
        searchArea.addAngle(LatLng(1.0, 1.0))
        searchArea.addAngle(LatLng(0.0, 1.0))
        val strategy = SimpleMultiPassOnQuadrilateral(1000000000000.0)
        val path = strategy.createFlightPath(LatLng(0.0, 0.0), searchArea)
        val pathSize = path.size
        assertThat(pathSize, equalTo(4))
    }
}