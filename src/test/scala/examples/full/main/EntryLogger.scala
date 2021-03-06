package examples.full.main

import java.time.Clock

import com.twitter.finagle.Http
import com.twitter.finagle.http.Method.{Get, Post}
import com.twitter.finagle.http.{Response, Status}
import com.twitter.util.Future
import examples.full.main.EntryLogger.{Entry, Exit, LogList}
import io.fintrospect.RouteSpec
import io.fintrospect.formats.json.Json4s.Native.JsonFormat._
import io.fintrospect.parameters.Body

object EntryLogger {

  object Entry {
    val entry = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(entry).at(Post) / "entry"
  }

  object Exit {
    val entry = Body(bodySpec[UserEntry]())
    val route = RouteSpec().body(entry).at(Post) / "exit"
  }

  object LogList {
    val entries = Body(bodySpec[Seq[UserEntry]]())
    val route = RouteSpec().at(Get) / "list"
  }

}

/**
  * Remote Entry Logger service, accessible over HTTP
  */
class EntryLogger(hostAuthority: String, clock: Clock) {
  private val http = Http.newService(hostAuthority)

  private def expect[T](expectedStatus: Status, b: Body[T]): Response => Future[T] = {
    r => if (r.status == expectedStatus) Future.value(b <-- r)
    else Future.exception(RemoteSystemProblem("entry logger", r.status))
  }

  private val entryClient = Entry.route bindToClient http

  def enter(username: Username): Future[UserEntry] =
    entryClient(Entry.entry --> UserEntry(username.value, goingIn = true, clock.instant().toEpochMilli))
      .flatMap(expect(Status.Created, Entry.entry))

  private val exitClient = Exit.route bindToClient http

  def exit(username: Username): Future[UserEntry] =
    exitClient(Exit.entry --> UserEntry(username.value, goingIn = false, clock.instant().toEpochMilli))
      .flatMap(expect(Status.Created, Exit.entry))

  private val listClient = LogList.route bindToClient http

  def list(): Future[Seq[UserEntry]] = listClient().flatMap(expect(Status.Ok, LogList.entries))
}
