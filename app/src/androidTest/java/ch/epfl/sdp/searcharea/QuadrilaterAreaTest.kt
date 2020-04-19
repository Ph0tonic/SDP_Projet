package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import kotlin.random.Random

class QuadrilaterAreaTest {

    @Test
    fun CanAddFourAngles() {
        val size = 4
        val area = QuadrilateralArea()
        for (i in 0..size) {
            area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(size))
    }

    @Test(expected = IllegalArgumentException::class)
    fun CannotAddMoreThanFourAngles() {
        val area = QuadrilateralArea()
        for (i in 0..4) {
            area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
    }

    @Test
    fun IsCompleteWhenFourAngle() {
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
    fun ResetIsPerformed() {
        val area = QuadrilateralArea()

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(1))
        area.reset()
        assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(0))
    }

    @Test
    fun GetPropsShouldBeEmpty() {
        val area = QuadrilateralArea()
        assertThat(area.getAdditionalProps().value, equalTo(mutableMapOf()))
    }

    @Test
    fun WhenAddingSomePointsShouldReorderThem() {
        TODO("Implement this test")
    }
}