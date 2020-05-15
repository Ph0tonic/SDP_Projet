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
class PolygonAreaBuilderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun canAddHundredAngles() {
        val size = 100
        val area = PolygonBuilder()
        repeat(size) {
            area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.vertices.size, equalTo(size))
    }

    @Test
    fun isCompleteWhenAtLeastThreeAngle() {
        val area = PolygonBuilder()
        assertThat(area.isComplete(), equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(true))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), equalTo(true))
    }

    @Test
    fun polygonAreaBuilderResetIsEffective() {
        val area = PolygonBuilder()

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.vertices.size, equalTo(1))
        area.reset()
        assertThat(area.vertices.size, equalTo(0))
    }
}