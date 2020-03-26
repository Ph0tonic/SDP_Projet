package ch.epfl.sdp

import android.location.Location
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class LocationUnitTest {

    private var location1: Location = Location("")

    @Before
    fun before(){
        location1 = Location("")
    }


    @Test
    fun addition_isCorrect() {
        Assert.assertEquals(4, 2 + 2.toLong())
    }

    @Test
    fun locationSubscriberDoesNotGetNotifiedJustBySubscribing() {
        val locationSubscriber = LocationSubscriberTester()
        CentralLocationListener.subscribe(locationSubscriber)
        Assert.assertEquals(locationSubscriber.locationChanged,false)
    }

    @Test
    fun locationSubscriberGetsNotifiedIfSubscribed() {
        val locationSubscriber = LocationSubscriberTester()
        CentralLocationListener.subscribe(locationSubscriber)
        CentralLocationListener.onLocationChanged(location1)
        Assert.assertEquals(locationSubscriber.locationChanged,true)
    }

    @Test
    fun locationSubscriberDoesNotGetNotifiedIfUnsubscribed() {
        val locationSubscriber = LocationSubscriberTester()
        CentralLocationListener.subscribe(locationSubscriber)
        CentralLocationListener.unsubscribe(locationSubscriber)
        CentralLocationListener.onLocationChanged(location1)
        Assert.assertEquals(locationSubscriber.locationChanged,false)
    }

    @Test
    fun locationSubscriberReceivesCorrectLocation() {
        val locationSubscriber = LocationSubscriberTester()
        CentralLocationListener.subscribe(locationSubscriber)
        CentralLocationListener.onLocationChanged(location1)
        Assert.assertTrue(locationSubscriber.location == location1)
    }


}


private class LocationSubscriberTester : LocationSubscriber{
    internal var location: Location = Location("")
    internal var locationChanged = false

    override fun onLocationChanged(location: Location) {
        locationChanged = true
        this.location = location
    }
}