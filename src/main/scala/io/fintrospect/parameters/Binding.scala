package io.fintrospect.parameters

import org.jboss.netty.handler.codec.http.HttpRequest

import scala.language.existentials

sealed trait Binding {
  val parameter: Parameter[_]
}

case class QueryBinding(parameter: Parameter[_], entry: (String, String)) extends Binding

case class PathBinding(parameter: Parameter[_], value: String) extends Binding

case class RequestBinding(parameter: Parameter[_], into: HttpRequest => HttpRequest) extends Binding
