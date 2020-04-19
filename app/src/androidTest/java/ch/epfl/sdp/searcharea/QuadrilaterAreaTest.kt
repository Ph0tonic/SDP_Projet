package ch.epfl.sdp.searcharea

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import ch.epfl.sdp.ui.maps.IntersectionTools
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class QuadrilaterAreaTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun canAddFourAngles() {
        val size = 4
        val area = QuadrilateralArea()
        for (i in 1..size) {
            area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(size))
    }

    @Test
    fun canMovePreviouslyAddedAngle() {
        val area = QuadrilateralArea()
        area.addAngle(LatLng(0.0, 0.0))
        area.addAngle(LatLng(1.0, 0.0))

        area.moveAngle(LatLng(0.0, 0.0), LatLng(1.0, 1.0))

        val corners = area.getLatLng().value!!
        assertThat(corners, equalTo(listOf(LatLng(1.0, 0.0), LatLng(1.0, 1.0))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotAddMoreThanFourAngles() {
        val area = QuadrilateralArea()
        for (i in 0..4) {
            area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
    }

    @Test
    fun isCompleteWhenFourAngle() {
        val area = QuadrilateralArea()
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(true))
    }

    @Test
    fun resetIsPerformed() {
        val area = QuadrilateralArea()

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(1))
        area.reset()
        assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(0))
    }

    @Test
    fun getPropsShouldBeEmpty() {
        val area = QuadrilateralArea()
        assertThat(area.getAdditionalProps().value, equalTo(mutableMapOf()))
    }

    @Test
    fun whenAddingSomePointsShouldPathShouldNotIntersect() {
        val area = QuadrilateralArea()
        area.addAngle(LatLng(0.0, 0.0))
        area.addAngle(LatLng(1.0, 0.0))
        area.addAngle(LatLng(0.0, 1.0))
        area.addAngle(LatLng(1.0, 1.0))

        val corners = area.getLatLng().value!!
        assertThat(IntersectionTools.doIntersect(corners[0], corners[1], corners[2], corners[3]), equalTo(false))
    }
}