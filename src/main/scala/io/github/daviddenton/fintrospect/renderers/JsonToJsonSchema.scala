package io.github.daviddenton.fintrospect.renderers

import argo.jdom.JsonNode
import argo.jdom.JsonNodeFactories.string
import argo.jdom.JsonNodeType._
import io.github.daviddenton.fintrospect.renderers.JsonToJsonSchema.IllegalSchemaException
import io.github.daviddenton.fintrospect.util.ArgoUtil._

import scala.collection.JavaConversions._

object JsonToJsonSchema {
  class IllegalSchemaException(message: String) extends Exception(message)
}

case class Schema(node: JsonNode, modelDefinitions: List[Field])

class JsonToJsonSchema(idGen: () => String) {

  private def toSchema(input: Schema): Schema = {
    input.node.getType match {
      case NULL => throw new IllegalSchemaException("Cannot use a null value in a schema!")
      case STRING => Schema(obj("type" -> string("string")), input.modelDefinitions)
      case TRUE => Schema(obj("type" -> string("boolean")), input.modelDefinitions)
      case FALSE => Schema(obj("type" -> string("boolean")), input.modelDefinitions)
      case NUMBER => Schema(obj("type" -> string("number")), input.modelDefinitions)
      case ARRAY => arraySchema(input)
      case OBJECT => objectSchema(input)
    }
  }

  private def arraySchema(input: Schema): Schema = {
    val Schema(node, modelDefinitions) = input.node.getElements.to[Seq].headOption.map(n => toSchema(Schema(n, input.modelDefinitions))).getOrElse(throw new IllegalSchemaException("Cannot use an empty list for a schema!"))
    Schema(obj("type" -> string("array"), "items" -> node), modelDefinitions)
  }

  private def objectSchema(input: Schema): Schema = {
    val definitionId = idGen()

    val (nodeFields, subDefinitions) = input.node.getFieldList.foldLeft((List[Field](), input.modelDefinitions)) {
      case ((memoFields, memoModels), nextField) =>
        val next = toSchema(Schema(nextField.getValue, memoModels))
        (nextField.getName.getText -> next.node :: memoFields, next.modelDefinitions)
    }

    val allDefinitions = definitionId -> obj("type" -> string("object"), "properties" -> obj(nodeFields: _*)) :: subDefinitions
    Schema(obj("$ref" -> string(s"#/definitions/$definitionId")), allDefinitions)
  }

  def toSchema(input: JsonNode): Schema = toSchema(Schema(input, Nil))
}