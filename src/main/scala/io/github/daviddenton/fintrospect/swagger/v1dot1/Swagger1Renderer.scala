package io.github.daviddenton.fintrospect.swagger.v1dot1

import argo.jdom.JsonNodeFactories._
import argo.jdom.{JsonNode, JsonRootNode}
import io.github.daviddenton.fintrospect.swagger.{SwParameter, SwPathMethod, SwResponse}
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.{ModuleRoute, Renderer}

import scala.collection.JavaConversions._

object Swagger1Renderer extends Renderer {
  override def render(p: SwParameter): JsonNode = obj(
    "name" -> string(p.name),
    "paramType" -> string(p.location.toString),
    "required" -> booleanNode(true),
    "dataType" -> string(p.paramType)
  )

  override def render(pm: SwPathMethod): (String, JsonNode) = pm.method.getName.toLowerCase -> obj(
    "httpMethod" -> string(pm.method.getName),
    "nickname" -> string(pm.summary),
    "summary" -> string(pm.summary),
    "produces" -> array(string("application/json")),
    "parameters" -> array(pm.params.map(render): _*),
    "errorResponses" -> {
      array(pm.responses.map(render).map(p => obj("code" -> number(p._1), "description" -> p._2)))
    }
  )

  override def render(r: ModuleRoute): (String, JsonNode) = ???

  override def render(r: SwResponse): (Int, JsonNode) = r.code -> string(r.description)

  override def render(mr: Seq[ModuleRoute]): JsonRootNode = {
    val api = mr
      .groupBy(_.toString)
      .map { case (path, routes) => obj("path" -> string(path), "operations" -> array(routes.map(render(_)._2): _*))}

    obj(
      "swaggerVersion" -> string("1.1"),
      "resourcePath" -> string("/"),
      "apis" -> array(asJavaIterable(api))
      //    "definitions" -> obj(
      //      "User" -> obj(
      //        "properties" -> obj(
      //          "id" -> obj(
      //            "type" -> "integer",
      //            "format" -> "int64"
      //          )
      //        )
      //      )
      //    )
    )
  }
}