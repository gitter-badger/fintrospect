package io.fintrospect.formats.json

import io.fintrospect.formats.json.Json4s.Json4sFormat
import org.json4s.MappingException

case class Json4sStreetAddress(address: String)

case class Json4sLetter(to: Json4sStreetAddress, from: Json4sStreetAddress, message: String)

abstract class RoundtripEncodeDecodeSpec[T](format: Json4sFormat[T]) extends JsonFormatSpec(format) {

  describe(format.getClass.getSimpleName) {
    val aLetter = Json4sLetter(Json4sStreetAddress("my house"), Json4sStreetAddress("your house"), "hi there")
    it("roundtrips to JSON and back") {
      format.decode[Json4sLetter](format.encode(aLetter)) shouldEqual aLetter
    }

    it("invalid extracted JSON throws up") {
      intercept[MappingException](format.decode[Json4sLetter](format.obj()))
    }
  }
}

class Json4sNativeEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.Native.JsonFormat)
class Json4sNativeJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.Native)

class Json4sNativeDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.NativeDoubleMode.JsonFormat)
class Json4sNativeDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.NativeDoubleMode)

class Json4sJacksonEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.Jackson.JsonFormat)
class Json4sJacksonJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.Jackson)

class Json4sJacksonDoubleEncodeDecodeTest extends RoundtripEncodeDecodeSpec(Json4s.JacksonDoubleMode.JsonFormat)
class Json4sJacksonDoubleJsonResponseBuilderTest extends JsonResponseBuilderSpec(Json4s.JacksonDoubleMode)

