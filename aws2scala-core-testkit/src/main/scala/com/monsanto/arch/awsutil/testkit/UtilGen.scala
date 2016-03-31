package com.monsanto.arch.awsutil.testkit

import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen

object UtilGen {
  /** Generates a ’word character‘. */
  val wordChar: Gen[Char] = Gen.oneOf(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') :+ '_')

  /** Generates a character matching `[a-zA-Z0-9_+,.-]`. */
  val extendedWordChar: Gen[Char] = Gen.oneOf(('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9') ++ "_+=,.-".toSeq)

  /** Generates a printable ASCII char. */
  val asciiChar: Gen[Char] = Gen.oneOf((0x20 to 0x7e).map(_.toChar))

  /** Generates a lower-case hexadecimal char. */
  val lowerHexChar: Gen[Char] = Gen.oneOf(('0' to '9') ++ ('a' to 'f'))

  /** Generates an upper-case hexadecimal char. */
  val upperHexChar: Gen[Char] = Gen.oneOf(('0' to '9') ++ ('A' to 'F'))

  val base36Char: Gen[Char] = Gen.oneOf(('0' to '9') ++ ('A' to 'Z'))

  def listOfSqrtN[T](g: Gen[T]): Gen[List[T]] =
    Gen.sized { size ⇒
      val maxN = Math.sqrt(size).toInt
      for {
        n ← Gen.choose(0, maxN)
        ts ← Gen.listOfN(n, g)
      } yield ts
    }

  def nonEmptyListOfSqrtN[T](g: Gen[T]): Gen[List[T]] =
    Gen.sized { size ⇒
      val maxN = Math.sqrt(size).toInt
      for {
        n ← Gen.choose(1, maxN)
        ts ← Gen.listOfN(n, g)
      } yield ts
    }

  /** Generates a string of between `minSize` and `maxSize` characters generated by the given generated. */
  def stringOf(charGen: Gen[Char], minSize: Int, maxSize: Int): Gen[String] =
    Sizer(minSize, maxSize).sized { size ⇒
      for {
        n ← Gen.choose(minSize, size)
        str ← Gen.listOfN(n, charGen).map(_.mkString)
      } yield str
    }.suchThat(_.length >= minSize)

  val nonEmptyString: Gen[String] = Gen.nonEmptyListOf(arbitrary[Char]).map(_.mkString).suchThat(_.nonEmpty)

  case class Sizer(minSize: Int, maxSize: Int, minScale: Double = 0.05) {
    private val k = Math.log(1.0 / minScale) / 99.0
    private val range = maxSize - minScale

    def apply(n: Int): Int = {
      val scale = Math.exp((n - 1) * k) * minScale
      val rawSizedMax = (range * scale + minSize).toInt
      rawSizedMax.min(maxSize).max(minSize)
    }

    def sized[T](f: Int ⇒ Gen[T]): Gen[T] = Gen.sized(n ⇒ f(apply(n)))
  }
}
