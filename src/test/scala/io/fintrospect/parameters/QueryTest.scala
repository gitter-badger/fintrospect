package io.fintrospect.parameters

import java.time.LocalDate

import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Request
import org.scalatest._


class QueryTest extends FunSpec with ShouldMatchers {

  private val paramName = "name"

  describe("required") {
    describe("singular") {
      val param = Query.required.localDate(paramName)

      it("retrieves value from field") {
        param.validate(requestWithValueOf("2015-02-04")) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
        param <-- requestWithValueOf("2015-02-04") shouldEqual LocalDate.of(2015, 2, 4)
      }

      it("fails to retrieve invalid value") {
        param.validate(requestWithValueOf("notValid")) shouldEqual Left(param)
      }

      it("does not retrieve non existent value") {
        param.validate(requestWithValueOf()) shouldEqual Left(param)
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123")
        val bindings = Query.required.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldEqual "/?field=123"
      }
    }

    describe("multi") {

      it("retrieves value from field") {
        val param = Query.required.multi.localDate(paramName)
        param.validate(requestWithValueOf("2015-02-04", "2015-02-05")) shouldEqual Right(Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- requestWithValueOf("2015-02-04", "2015-02-05") shouldEqual Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))
      }

      it("fails to retrieve invalid value") {
        val param = Query.required.*.long(paramName)
        param.validate(requestWithValueOf("qwe","notValid")) shouldEqual Left(param)
      }

      it("does not retrieve non existent value") {
        val param = Query.required.*.zonedDateTime(paramName)
        param.validate(requestWithValueOf()) shouldEqual Left(param)
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123&field=456")
        val bindings = Query.required.*.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldEqual "/?field=456&field=123"
      }
    }
  }

  describe("optional") {
    describe("singular") {

      it("retrieves value from field") {
        val param = Query.optional.localDate(paramName)
        param.validate(requestWithValueOf("2015-02-04")) shouldEqual Right(Option(LocalDate.of(2015, 2, 4)))
        param <-- requestWithValueOf("2015-02-04") shouldEqual Option(LocalDate.of(2015, 2, 4))
      }

      it("fails to retrieve invalid value") {
        val param = Query.optional.json(paramName)
        param.validate(requestWithValueOf("notValid")) shouldEqual Left(param)
      }

      it("does not retrieve non existent value") {
        val param = Query.optional.xml(paramName)
        param.validate(requestWithValueOf()) shouldEqual Right(None)
        param <-- Request() shouldEqual None
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123&field=456")
        val bindings = Query.optional.*.int("field") rebind inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldEqual "/?field=456&field=123"
      }

      it("doesn't rebind missing value") {
        val inRequest = Request("?")
        val bindings = Query.optional.dateTime("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldEqual "/"
      }
    }
    describe("multi") {
      val param = Query.optional.multi.localDate(paramName)

      it("retrieves value from field") {
        param.validate(requestWithValueOf("2015-02-04", "2015-02-05")) shouldEqual Right(Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5))))
        param <-- requestWithValueOf("2015-02-04", "2015-02-05") shouldEqual Option(Seq(LocalDate.of(2015, 2, 4), LocalDate.of(2015, 2, 5)))
      }

      it("fails to retrieve invalid value") {
        param.validate(requestWithValueOf("2015-02-04", "notValid")) shouldEqual Left(param)
      }

      it("does not retrieve non existent value") {
        param.validate(requestWithValueOf()) shouldEqual Right(None)
        param <-- Request() shouldEqual None
      }

      it("can rebind valid value") {
        val inRequest = Request("?field=123&field=456")
        val bindings = Query.optional.multi.int("field") rebind inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldEqual "/?field=456&field=123"
      }

      it("doesn't rebind missing value") {
        val inRequest = Request("?")
        val bindings = Query.optional.int("field") <-> inRequest
        val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
        outRequest.uri shouldEqual "/"
      }
    }
  }

  private def requestWithValueOf(value: String*) = {
    Request(value.map(value => paramName -> value): _*)
  }
}
