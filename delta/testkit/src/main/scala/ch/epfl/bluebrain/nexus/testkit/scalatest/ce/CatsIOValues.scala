package ch.epfl.bluebrain.nexus.testkit.scalatest.ce

import cats.effect.IO
import org.scalactic.source
import org.scalatest.{Assertion, Suite}

import scala.concurrent.duration.DurationInt
import scala.reflect.ClassTag

trait CatsIOValues {

  self: Suite =>

  implicit final class CatsIOValuesOps[A](private val io: IO[A]) {
    def accepted: A =
      io.unsafeRunTimed(45.seconds).getOrElse(fail("IO timed out during .accepted call"))

    def rejected(implicit pos: source.Position): Throwable = rejectedWith[Throwable]

    def assertRejectedEquals[E](expected: E)(implicit pos: source.Position, EE: ClassTag[E]): Assertion =
      assertResult(expected)(rejectedWith[E])

    def rejectedWith[E](implicit pos: source.Position, EE: ClassTag[E]): E = {
      io.attempt.accepted match {
        case Left(EE(value)) => value
        case Left(value)     =>
          fail(
            s"Wrong raised error type caught, expected: '${EE.runtimeClass.getName}', actual: '${value.getClass.getName}'"
          )
        case Right(value)    =>
          fail(
            s"Expected raising error, but returned successful response with type '${value.getClass.getName}'"
          )
      }
    }
  }
}
