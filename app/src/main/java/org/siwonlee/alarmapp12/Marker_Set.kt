package org.siwonlee.alarmapp12

import com.google.android.gms.maps.model.Marker

class Marker_Info (val lat:Double = 0.toDouble(), val lon: Double = 0.toDouble())
fun Marker_Info.isEqual(other: Marker_Info): Boolean {
    return (this.lat == other.lat && this.lon == other.lon)
}

class Marker_Set {
    var markerList: ArrayList<Marker_Info> = ArrayList()
    fun add(marker: Marker) {
        this.markerList.add(Marker_Info(marker.position.latitude, marker.position.longitude))
    }
    fun pop(marker: Marker) {
        val searchMarker = Marker_Info(marker.position.latitude, marker.position.longitude)
        for(i in 0 until markerList.size)
            if(markerList[i].isEqual(searchMarker)) {
                markerList.removeAt(i)
                break
            }
    }
}