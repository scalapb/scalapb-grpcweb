package io.grpc

final class Metadata {
  def get[T](key: Metadata.Key[T]): T = ???

  def put[T](key: Metadata.Key[T], value: T): Unit = ???

  def remove[T](key: Metadata.Key[T], value: T): Boolean = ???
}

object Metadata {
  final class Key[T]
}
