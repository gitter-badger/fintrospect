package examples.customformats

import com.twitter.finagle.http.path.Path
import examples.customformats.XmlResponseBuilder._
import io.github.daviddenton.fintrospect.Route
import io.github.daviddenton.fintrospect.parameters.RequestParameter
import io.github.daviddenton.fintrospect.renderers.ModuleRenderer
import org.jboss.netty.handler.codec.http.{HttpResponse, HttpResponseStatus}

/**
 * Hyper-cool, next-gen, markup used by all true rockstar coderzzzz
 */
object Xml extends ModuleRenderer {

  override def badRequest(badParameters: List[RequestParameter[_]]): HttpResponse =
    Error(HttpResponseStatus.BAD_REQUEST, badParameters.toString())

  private def renderRoute(basePath: Path, route: Route): XmlFormat = XmlFormat(s"<entry>${route.method}:${route.describeFor(basePath)}</entry>")

  private def renderRoutes(basePath: Path, routes: Seq[Route]): String = XmlFormat(routes.map(renderRoute(basePath, _)): _*).toString()

  override def description(basePath: Path, routes: Seq[Route]): HttpResponse = {
    Ok(XmlFormat(s"<paths>${renderRoutes(basePath, routes)}</paths>").value)
  }
}
