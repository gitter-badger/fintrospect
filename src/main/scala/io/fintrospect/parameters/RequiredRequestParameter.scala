package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.util.Try

abstract class RequiredRequestParameter[T](location: Location, parse: (String => Try[T]))
  extends RequestParameter[T](Requirement.Mandatory, location, parse) {
  def from(request: HttpRequest): T = parseFrom(request).flatMap(_.toOption).get
}

object RequiredRequestParameter {
  def builderFor(location: Location) = new ParameterBuilder[RequiredRequestParameter]() {
    override def apply[T](aName: String, aDescription: Option[String], aParamType: ParamType, parse: (String => Try[T])) =
      new RequiredRequestParameter[T](location, parse) {
        override val name = aName
        override val description = aDescription
        override val paramType = aParamType
      }
  }
}