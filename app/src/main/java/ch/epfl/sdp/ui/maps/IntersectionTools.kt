package ch.epfl.sdp.ui.maps

import com.mapbox.mapboxsdk.geometry.LatLng
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min

//Adapted from : https://www.geeksforgeeks.org/check-if-two-given-line-segments-intersect/
object IntersectionTools {
    // Given three colinear points p, q, r, the function checks if
    // point q lies on line segment 'pr'
    private fun onSegment(p: LatLng, q: LatLng, r: LatLng): Boolean {
        return q.latitude <= max(p.latitude, r.latitude) && q.latitude >= min(p.latitude, r.latitude) && q.longitude <= max(p.longitude, r.longitude) && q.longitude >= min(p.longitude, r.longitude)
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are colinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    private fun orientation(p: LatLng, q: LatLng, r: LatLng): Int {
        // See https://www.geeksforgeeks.org/orientation-3-ordered-points/
        // for details of below formula.
        val res = (q.longitude - p.longitude) * (r.latitude - q.latitude) -
                (q.latitude - p.latitude) * (r.longitude - q.longitude)
        if (res.absoluteValue.equals(0.0)) return 0 // colinear absoluteValue is needed in case of "-0.0" value
        return if (res > 0) 1 else 2 // clock or counterclock wise
    }

    // The main function that returns true if line segment 'p1q1'
    // and 'p2q2' intersect.
    fun doIntersect(p1: LatLng, q1: LatLng, p2: LatLng, q2: LatLng): Boolean {
        // Find the four orientations needed for general and
        // special cases
        val o1 = orientation(p1, q1, p2)
        val o2 = orientation(p1, q1, q2)
        val o3 = orientation(p2, q2, p1)
        val o4 = orientation(p2, q2, q1)

        // General case
        return if (o1 != o2 && o3 != o4) true

        // Special Cases
        // p1, q1 and p2 are colinear and p2 lies on segment p1q1
        else if (o1 == 0 && onSegment(p1, p2, q1)) true

        // p1, q1 and q2 are colinear and q2 lies on segment p1q1
        else if (o2 == 0 && onSegment(p1, q2, q1)) true

        // p2, q2 and p1 are colinear and p1 lies on segment p2q2
        else if (o3 == 0 && onSegment(p2, p1, q2)) true

        // p2, q2 and q1 are colinear and q1 lies on segment p2q2
        else o4 == 0 && onSegment(p2, q1, q2)
        // Doesn't fall in any of the above cases
    }
}