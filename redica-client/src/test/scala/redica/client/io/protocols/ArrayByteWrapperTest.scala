package redica.client.io.protocols

import org.scalatest.FunSuite

class ArrayByteWrapperTest extends FunSuite {

  test("testRead") {
    val data = Array(1, 2, 3).map(_.toByte)
    val sut = new ArrayByteWrapper(data)
    data.foreach { i =>
      assert(sut.read() === i)
    }
    assert(sut.read() === -1)
    assert(sut.read() === -1)
  }

  test("testAppend") {
    val data = Array(1).map(_.toByte)
    val sut = new ArrayByteWrapper(data)
    sut.append(Array(2.toByte))
    assert(sut.read() === 1)
    assert(sut.read() === 2)
    assert(sut.read() === -1)

    sut.append(Array(3).map(_.toByte))
    assert(sut.read() === 3)
    assert(sut.read() === -1)
  }

  test("testPrepend") {
    val data = Array(2).map(_.toByte)
    val sut = new ArrayByteWrapper(data)
    sut.prepend(Array(1.toByte))
    assert(sut.read() === 1)
    assert(sut.read() === 2)
    assert(sut.read() === -1)

    sut.prepend(Array(3.toByte))
    assert(sut.read() === 3)
    assert(sut.read() === -1)
  }

}
