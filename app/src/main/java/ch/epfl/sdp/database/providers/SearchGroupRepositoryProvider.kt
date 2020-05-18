package ch.epfl.sdp.database.providers

import ch.epfl.sdp.database.repository.ISearchGroupRepository
import ch.epfl.sdp.database.repository.SearchGroupRepository

object SearchGroupRepositoryProvider : RepositoryProvider<ISearchGroupRepository>() {
    override var provide: () -> ISearchGroupRepository = { SearchGroupRepository() }
}