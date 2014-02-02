package maqicode

import org.junit.Test

import maqicode.testing.JUnitTest
import maqicode.testing.speculum.inspect

class AntipTest extends JUnitTest {

  import maqicode.antipolator._

  @Test def sample(): Unit = {
    val one = 1
    val two = 2
    val tre = 3L
    val cat: Short = 4
    val snk: Char = 'c'
    val set = true
    val wit = 3.0f
    val nuf = 5.0d
    val dis: Byte = 7
    val ess = "s"
    inspect {
      "1" == a"one"
      "12" == a"one${""}two"
      "1()2" == a"one${}two"
      "1s2" == a"one${ess}two"
      "112" == a"one${one}two"
      "132" == a"one${tre}two"
      "142" == a"one${cat}two"
      "1c2" == a"one${snk}two"
      "1true2" == a"one${set}two"
      "13.02" == a"one${wit}two"
      "15.02" == a"one${nuf}two"
      "172" == a"one${dis}two"
      "12" == a"one + two"
      "123" == a"one + two + tre"
      "11.6666666666666667" == a"one + (nuf / wit)"
      "11" == a"1 + 1"
      "1 and 2" == a"""one +" and "+ two"""
      1 == one
    }
  }
}
