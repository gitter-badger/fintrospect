package io.fintrospect.parameters

import com.twitter.finagle.http.Message
import io.fintrospect.ContentType
import io.fintrospect.formats.json.{Argo, JsonFormat}

import scala.xml.Elem

abstract class Body[T](spec: BodySpec[T]) extends Iterable[BodyParameter] with Retrieval[T, Message] {
  val contentType: ContentType = spec.contentType

  def validate(request: Message): Seq[Either[Parameter, Option[_]]]
}

/**
 * Factory methods for various supported HTTP body types.
 */
object Body {

  /**
   * Create a custom body type for the request. Encapsulates the means to insert/retrieve into the request
   */
  def apply[T](bodySpec: BodySpec[T], example: T = null, paramType: ParamType = StringParamType): UniBody[T] = new UniBody[T](bodySpec, paramType, Option(example))

  /**
   * JSON format HTTP message body. Defaults to Argo JSON format, but this can be overridden by passing an alternative JsonFormat
   */
  def json[T](description: Option[String], example: T = null, jsonFormat: JsonFormat[T, _] = Argo.JsonFormat): UniBody[T] = Body(BodySpec.json(description, jsonFormat), example, ObjectParamType)

  /**
   * Native Scala XML format HTTP message body.
   */
  def xml(description: Option[String], example: Elem = null): UniBody[Elem] = Body(BodySpec.xml(description), example, StringParamType)

  /**
   * HTML encoded form HTTP message body.
   */
  def form(fields: FormField[_] with Retrieval[_, Form]*): FormBody = new FormBody(fields)
}
