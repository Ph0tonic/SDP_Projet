package ch.epfl.sdp.searcharea

import kotlin.reflect.KClass

class RoundArea: SearchArea {

    fun test(): List<KClass<out SearchArea>> {
        return listOf(RoundArea::class, QuadrilateralArea::class)
    }

    override fun isComplete(): Boolean {
        TODO("Not yet implemented")
    }
}