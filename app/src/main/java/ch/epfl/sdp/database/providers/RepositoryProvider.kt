package ch.epfl.sdp.database.providers

abstract class RepositoryProvider<T> {
    abstract var provide: () -> T
}