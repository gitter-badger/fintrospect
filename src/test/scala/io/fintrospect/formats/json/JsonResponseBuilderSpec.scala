package io.fintrospect.formats.json

import io.fintrospect.formats.AbstractResponseBuilderSpec

abstract class JsonResponseBuilderSpec[X, Y](jsonLibrary: JsonLibrary[X, Y]) extends AbstractResponseBuilderSpec[X](jsonLibrary.ResponseBuilder) {
  override val expectedContent = message
  override val customError = jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message))
  override val expectedErrorContent = jsonLibrary.JsonFormat.pretty(jsonLibrary.JsonFormat.obj("message" -> jsonLibrary.JsonFormat.string(message)))
  override val customType = jsonLibrary.JsonFormat.obj("okThing" -> jsonLibrary.JsonFormat.string("theMessage"))
  override val customTypeSerialised: String = jsonLibrary.JsonFormat.pretty(customType)
}
