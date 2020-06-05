package ch.epfl.sdp.utils

import ch.epfl.sdp.utils.IntersectionUtils
import com.mapbox.mapboxsdk.geometry.LatLng
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class IntersectionUtilsTest {

    @Test
    fun intersectShouldDetectWhenItIntersect() {
        val p1 = LatLng(0.0, 0.0)
        val p2 = LatLng(1.0, 0.0)
        val p3 = LatLng(0.0, 1.0)
        val p4 = LatLng(1.0, 1.0)

        assertThat(IntersectionUtils.doIntersect(p1, p4, p2, p3), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p4, p1, p2, p3), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p1, p4, p3, p2), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p4, p1, p3, p2), equalTo(true))

        assertThat(IntersectionUtils.doIntersect(p2, p3, p1, p4), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p3, p2, p1, p4), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p2, p3, p4, p1), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p3, p2, p4, p1), equalTo(true))

        assertThat(IntersectionUtils.doIntersect(p1, p2, p4, p3), equalTo(false))
        assertThat(IntersectionUtils.doIntersect(p4, p2, p1, p3), equalTo(false))
        assertThat(IntersectionUtils.doIntersect(p1, p3, p4, p2), equalTo(false))
        assertThat(IntersectionUtils.doIntersect(p4, p3, p1, p2), equalTo(false))

        assertThat(IntersectionUtils.doIntersect(p2, p1, p3, p4), equalTo(false))
        assertThat(IntersectionUtils.doIntersect(p3, p1, p2, p4), equalTo(false))
        assertThat(IntersectionUtils.doIntersect(p2, p4, p3, p1), equalTo(false))
        assertThat(IntersectionUtils.doIntersect(p3, p4, p2, p1), equalTo(false))
    }

    @Test
    fun intersectShouldDetectWhenSameStartingPoint() {
        val p1 = LatLng(0.0, 0.0)
        val p2 = LatLng(1.0, 0.0)
        val p3 = LatLng(0.0, 1.0)

        assertThat(IntersectionUtils.doIntersect(p1, p2, p1, p3), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p2, p1, p2, p3), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p1, p2, p3, p1), equalTo(true))
        assertThat(IntersectionUtils.doIntersect(p2, p1, p3, p2), equalTo(true))
    }
}