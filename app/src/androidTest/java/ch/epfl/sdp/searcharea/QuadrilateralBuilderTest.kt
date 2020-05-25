package ch.epfl.sdp.searcharea

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.internal.runner.junit4.statement.UiThreadStatement.runOnUiThread
import ch.epfl.sdp.utils.IntersectionUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import kotlin.random.Random

class QuadrilateralBuilderTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Test
    fun canAddFourAngles() {
        val size = 4
        val area = QuadrilateralBuilder()
        repeat(size) {
            area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        assertThat(area.vertices.size, CoreMatchers.equalTo(size))
    }

    @Test
    fun canMovePreviouslyAddedAngle() {
        val area = QuadrilateralBuilder()
        area.addVertex(LatLng(0.0, 0.0))
        area.addVertex(LatLng(1.0, 0.0))

        area.moveVertex(LatLng(0.0, 0.0), LatLng(1.0, 1.0))

        val corners = area.vertices
        assertThat(corners, equalTo(listOf(LatLng(1.0, 1.0), LatLng(1.0, 0.0))))
    }

    @Test(expected = IllegalArgumentException::class)
    fun cannotAddMoreThanFourAngles() {
        val area = QuadrilateralBuilder()
        repeat(4) {
            area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        }
        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
    }

    @Test
    fun isCompleteWhenFourAngle() {
        val area = QuadrilateralBuilder()
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(false))

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.isComplete(), CoreMatchers.equalTo(true))
    }

    @Test
    fun quadrilateralBuilderResetIsEffective() {
        val area = QuadrilateralBuilder()

        area.addVertex(LatLng(Random.nextDouble(), Random.nextDouble()))
        assertThat(area.vertices.size, CoreMatchers.equalTo(1))
        area.reset()
        assertThat(area.vertices.size, CoreMatchers.equalTo(0))
    }

    @Test
    fun whenAddingSomePointsShouldPathShouldNotIntersect() {
        val area = QuadrilateralBuilder()
        area.addVertex(LatLng(0.0, 0.0))
        area.addVertex(LatLng(1.0, 0.0))
        area.addVertex(LatLng(0.0, 1.0))
        area.addVertex(LatLng(1.0, 1.0))

        val corners = area.vertices
        assertThat(IntersectionUtils.doIntersect(corners[0], corners[1], corners[2], corners[3]), equalTo(false))
    }
}