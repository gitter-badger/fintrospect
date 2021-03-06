package examples.full.main

import com.twitter.finagle.Service
import com.twitter.finagle.http.Method.Post
import com.twitter.finagle.http.Status._
import com.twitter.finagle.http.{Request, Response}
import examples.full.main.SecuritySystemAuth._
import io.fintrospect.formats.PlainText.ResponseBuilder._
import io.fintrospect.parameters.{ParameterSpec, Query, StringParamType}
import io.fintrospect.{RouteSpec, ServerRoutes}

import scala.language.reflectiveCalls


class KnockKnock(inhabitants: Inhabitants, userDirectory: UserDirectory, entryLogger: EntryLogger) extends ServerRoutes[Response] {
  private val username = Query.required(ParameterSpec[Username]("username", None, StringParamType, s => Username(s), _.value.toString))

  private def userEntry() = Service.mk[Request, Response] {
    request =>
      userDirectory.lookup(username <-- request)
        .flatMap {
          case Some(user) =>
            if (inhabitants.add(user.name))
              entryLogger
                .enter(user.name)
                .map(ue => Accepted())
            else BadRequest()
          case None => NotFound()
        }
  }

  add(RouteSpec("User enters the building")
    .taking(apiKey) // see SecuritySystemAuth for why this is here
    .taking(username)
    .returning(Ok -> "Access granted")
    .returning(NotFound -> "Unknown user")
    .returning(BadRequest -> "User is already inside building")
    .returning(Unauthorized -> "Incorrect key")
    .at(Post) / "knock" bindTo userEntry)
}
