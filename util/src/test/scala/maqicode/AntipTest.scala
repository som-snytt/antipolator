package maqicode

import org.junit.Test

import maqicode.testing.JUnitTest
import maqicode.testing.speculum.inspect

class AntipTest extends JUnitTest {

  @Test def sample(): Unit = {
    val one = 1
    inspect {
      1 == one
    }
  }
}
