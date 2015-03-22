package io.github.daviddenton.fintrospect.parameters

import com.twitter.finagle.http.Request

class RequiredQueryTest extends ParametersTest[RequiredRequestParameter](Query.required) {
  override def from[X](param: RequiredRequestParameter[X], value: String): Option[X] = {
    scala.util.Try(param.from(Request(paramName -> value))).toOption
  }
}