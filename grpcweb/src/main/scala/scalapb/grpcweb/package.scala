package scalapb

import scala.scalajs.js.Dictionary

package object grpcweb {

  type Metadata = Dictionary[String]

  object Metadata {
    def apply(properties: (String, String)*): Metadata =
      Dictionary.apply[String](properties: _*)
    def empty: Metadata = Dictionary.empty[String]
  }
}
