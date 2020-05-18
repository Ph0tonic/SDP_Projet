package ch.epfl.sdp.database.providers

import ch.epfl.sdp.database.repository.IMarkerRepository
import ch.epfl.sdp.database.repository.MarkerRepository

object MarkerRepositoryProvider : RepositoryProvider<IMarkerRepository>() {
    override var provide: () -> IMarkerRepository = { MarkerRepository() }
}