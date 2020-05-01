package ch.epfl.sdp.searcharea

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

class CircleBuilderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun canAddTwoPoints() {
        val size = 2
        val area = CircleBuilder()
        for (i in 1..size) {
            area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.vertices.size, CoreMatchers.equalTo(size))
    }

    @Test
    fun canMovePreviouslyAddedPoint() {
        val area = CircleBuilder()

        area.addVertex(LatLng(0.0, 0.0))
        area.addVertex(LatLng(1.0, 0.0))

        area.moveVertex(LatLng(0.0, 0.0), LatLng(1.0, 1.0))

        val corners = area.vertices
        assertThat(corners, equalTo(listOf(LatLng(1.0, 1.0), LatLng(1.0, 0.0))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotAddMoreThanTwoPoints() {
        val area = CircleBuilder()
        for (i in 0..2) {
            area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
    }

    @Test
    fun isCompleteWhenTwoPoints() {
        val area = CircleBuilder()
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(true))
    }

    @Test
    fun resetIsEffective() {
        val area = CircleBuilder()

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.vertices.size, CoreMatchers.equalTo(1))
        area.reset()
        assertThat(area.vertices.size, CoreMatchers.equalTo(0))
    }
}