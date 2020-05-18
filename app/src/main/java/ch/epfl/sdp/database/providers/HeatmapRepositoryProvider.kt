package ch.epfl.sdp.database.providers

import ch.epfl.sdp.database.repository.HeatmapRepository
import ch.epfl.sdp.database.repository.IHeatmapRepository

object HeatmapRepositoryProvider : RepositoryProvider<IHeatmapRepository>() {
    override var provide: () -> IHeatmapRepository = { HeatmapRepository() }
}