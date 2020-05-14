package ch.epfl.sdp.database.providers

abstract class RepositoryProvider<T> {
    abstract val defaultProvider: () -> T
    var provide: () -> T = defaultProvider
}