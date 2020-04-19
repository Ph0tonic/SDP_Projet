package ch.epfl.sdp.searcharea

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.random.Random

@RunWith(AndroidJUnit4::class)
class PolygonAreaTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun canAddHundredAngles() {
        val size = 100
        val area = PolygonArea()
        for (i in 1..size) {
            area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.getNbAngles(), equalTo(size))
    }

    @Test
    fun isCompleteWhenAtLeastThreeAngle() {
        val area = PolygonArea()
        assertThat(area.isComplete(), equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(true))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(true))
    }

    @Test
    fun resetIsPerformed() {
        val area = PolygonArea()

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.getNbAngles(), equalTo(1))
        area.reset()
        assertThat(area.getNbAngles(), equalTo(0))
    }

    @Test
    fun getPropsShouldBeEmpty() {
        val area = PolygonArea()
        assertThat(area.getAdditionalProps().value, equalTo(mutableMapOf()))
    }
}