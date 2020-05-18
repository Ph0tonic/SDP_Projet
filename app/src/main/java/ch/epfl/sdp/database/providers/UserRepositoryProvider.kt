package ch.epfl.sdp.database.providers

import ch.epfl.sdp.database.repository.IUserRepository
import ch.epfl.sdp.database.repository.UserRepository

object UserRepositoryProvider : RepositoryProvider<IUserRepository>() {
    override var provide: () -> IUserRepository = { UserRepository() }
}