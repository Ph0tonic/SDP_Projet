package ch.epfl.sdp.searcharea

import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
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
        MatcherAssert.assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(size))
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
        MatcherAssert.assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        MatcherAssert.assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        MatcherAssert.assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        MatcherAssert.assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        MatcherAssert.assertThat(area.isComplete(), CoreMatchers.equalTo(true))
    }

    @Test
    fun ResetIsPerformed() {
        val area = QuadrilateralArea()

        area.addAngle(LatLng(Random.nextDouble(), Random.nextDouble()))
        MatcherAssert.assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(1))
        area.reset()
        MatcherAssert.assertThat(area.getLatLng().value?.size, CoreMatchers.equalTo(0))
    }

    @Test
    fun GetPropsShouldBeEmpty() {
        val area = QuadrilateralArea()
        MatcherAssert.assertThat(area.getAdditionalProps(), CoreMatchers.equalTo(mutableMapOf<String, Double>()))
    }
}