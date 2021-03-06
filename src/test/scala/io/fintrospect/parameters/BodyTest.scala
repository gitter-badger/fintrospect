package io.fintrospect.parameters

import java.time.LocalDate

import argo.jdom.JsonRootNode
import com.twitter.finagle.http.Method._
import com.twitter.finagle.http.Request
import io.fintrospect.ContentTypes
import io.fintrospect.formats.json.Argo
import io.fintrospect.formats.json.Argo.JsonFormat._
import io.fintrospect.util.HttpRequestResponseUtil.contentFrom
import org.jboss.netty.handler.codec.http.HttpHeaders.Names
import org.scalatest.{FunSpec, ShouldMatchers}

import scala.xml.XML

class BodyTest extends FunSpec with ShouldMatchers {

  describe("body") {

    it("should retrieve the body value from the request") {
      val bodyJson = obj("field" -> string("value"))
      val request = Request("/")
      request.write(pretty(bodyJson))
      Body.json(Option("description"), obj("field" -> string("value"))) <-- request shouldEqual bodyJson
    }
  }

  describe("form") {
    it("should serialize and deserialize into the request") {
      val date = FormField.required.localDate("date")
      val formBody = Body.form(date)
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      contentFrom(request) shouldEqual "date=1976-08-31"
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
    }

    it("should serialize strings correctly into the request") {
      val aString = FormField.required.string("na&\"<>me")
      val formBody = Body.form(aString)
      val inputForm = Form(aString --> "&\"<>")
      val bindings = formBody --> inputForm
      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_FORM_URLENCODED.value
      val deserializedForm = formBody from request
      deserializedForm shouldEqual inputForm
    }

    it("can rebind valid value") {
      val date = FormField.required.localDate("date")
      val inputForm = Form(date --> LocalDate.of(1976, 8, 31))
      val formBody = Body.form(date)
      val bindings = formBody --> inputForm
      val inRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val rebindings = formBody <-> inRequest
      val outRequest = rebindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      val deserializedForm = formBody from outRequest
      deserializedForm shouldEqual inputForm
    }
  }

  describe("json") {
    it("should serialize and deserialize into the request") {

      val jsonBody = Body.json[JsonRootNode](None)
      val inputJson = obj("bob" -> string("builder"))
      val bindings = jsonBody --> inputJson

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      contentFrom(request) shouldEqual "{\"bob\":\"builder\"}"
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_JSON.value
      val deserializedJson = jsonBody <-- request
      deserializedJson shouldEqual inputJson
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputJson = obj("bob" -> string("builder"))
      inRequest.setContentString(pretty(inputJson))

      val bindings = Body.json(None) <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      Argo.JsonFormat.parse(contentFrom(outRequest)) shouldEqual inputJson
    }
  }

  describe("xml") {
    it("should serialize and deserialize into the request") {

      val xmlBody = Body.xml(None)
      val inputXml = <field>value</field>
      val bindings = xmlBody --> inputXml

      val request = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()

      contentFrom(request) shouldEqual "<field>value</field>"
      request.headerMap(Names.CONTENT_TYPE) shouldEqual ContentTypes.APPLICATION_XML.value
      val deserializedXml = xmlBody <-- request
      deserializedXml shouldEqual inputXml
    }

    it("can rebind valid value") {
      val inRequest = Request()
      val inputXml = <field>value</field>
      inRequest.setContentString(inputXml.toString())
      val bindings = Body.xml(None) <-> inRequest
      val outRequest = bindings.foldLeft(RequestBuilder(Get)) { (requestBuild, next) => next(requestBuild) }.build()
      XML.loadString(contentFrom(outRequest)) shouldEqual inputXml
    }
  }
}
