package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonRootNode
import io.github.daviddenton.fintrospect.DescriptionRenderer
import io.github.daviddenton.fintrospect.util.ArgoUtil._
import io.github.daviddenton.fintrospect.util.JsonResponseBuilder

import scala.language.implicitConversions

class ArgoJsonResponseBuilder(descriptionRenderer: DescriptionRenderer[JsonRootNode]) extends TypedResponseBuilder[JsonRootNode](
  () => new JsonResponseBuilder,
  descriptionRenderer,
  badParameters => {
    val messages = badParameters.map(p => obj(
      "name" -> string(p.name),
      "type" -> string(p.where),
      "datatype" -> string(p.paramType.name),
      "required" -> boolean(p.requirement.required)
    ))

    obj("message" -> string("Missing/invalid parameters"), "params" -> array(messages))
  })