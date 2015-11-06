package io.fintrospect.formats.json

import java.math.BigInteger

import argonaut.Argonaut._
import argonaut.{DecodeJson, EncodeJson, Json}
import io.fintrospect.formats.json.JsonFormat.{InvalidJson, InvalidJsonForDecoding}

/**
 * Argonaut JSON support.
 */
object Argonaut extends JsonLibrary[Json, Json] {

  object JsonFormat extends JsonFormat[Json, Json] {

    override def parse(in: String): Json = in.parseOption.getOrElse(throw new InvalidJson)

    override def pretty(node: Json): String = node.spaces2

    override def compact(node: Json): String = node.nospaces

    override def obj(fields: Iterable[Field]): Json = Json.obj(fields.map(f => (f._1, f._2)).toSeq:_*)

    override def obj(fields: Field*): Json = Json.obj(fields.map(f => (f._1, f._2)):_*)

    override def array(elements: Iterable[Json]) = Json.array(elements.toSeq: _*)

    override def array(elements: Json*) = array(elements)

    override def string(value: String) = jString(value)

    override def number(value: Int) = jNumber(value)

    override def number(value: BigDecimal) = jNumber(value.doubleValue())

    override def number(value: Long) = jNumber(value)

    override def number(value: BigInteger) = jNumber(value.intValue())

    override def boolean(value: Boolean) = jBool(value)

    override def nullNode() = jNull

    def encode[T](in: T)(implicit codec: EncodeJson[T]) = codec.encode(in)

    def decode[T](in: Json)(implicit codec: DecodeJson[T]) = codec.decodeJson(in).getOr(throw new InvalidJsonForDecoding)
  }

}