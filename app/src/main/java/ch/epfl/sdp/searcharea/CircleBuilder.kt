package ch.epfl.sdp.searcharea

class CircleBuilder : SearchAreaBuilder() {
    override val sizeLowerBound: Int? = 2
    override val sizeUpperBound: Int? = 2
    override val shapeName: String = "Circle"
    override fun buildGivenIsComplete() = CircleArea(vertices[0], vertices[1])
}