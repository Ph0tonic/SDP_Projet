package ch.epfl.sdp

import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.epfl.sdp.drone.SimpleMultiPassOnQuadrangle
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OverflightStrategyTest {

    @Test(expected = IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptNegativeMaxDistance(){
        SimpleMultiPassOnQuadrangle(-10.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptZeroMaxDistance(){
        SimpleMultiPassOnQuadrangle(0.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptCreatingPathWithLessThanFourPositions(){
        SimpleMultiPassOnQuadrangle(-10.0)
                .createFlightPath(listOf(LatLng(0.0,0.0)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun simpleMultiPassOnQuadrangleDoesNotAcceptCreatingPathWithMoreThanFourPositions(){
        SimpleMultiPassOnQuadrangle(-10.0)
                .createFlightPath(listOf(
                        LatLng(0.0,0.0),
                        LatLng(0.0,0.0),
                        LatLng(0.0,0.0),
                        LatLng(0.0,0.0),
                        LatLng(0.0,0.0)))
    }

    @Test
    fun simpleMultiPassCreatesGoodNumberOfPointsForSmallArea(){
        val strategy = SimpleMultiPassOnQuadrangle(10000000000000000.0)
        val path = strategy.createFlightPath(listOf(
                        LatLng(0.0,0.0),
                        LatLng(1.0,0.0),
                        LatLng(1.0,1.0),
                        LatLng(0.0,1.0)))
        val pathSize =  path.size
        assertThat(pathSize,equalTo(4))
    }
}