package io.fintrospect

import com.twitter.finagle.{Filter, Service}
import com.twitter.finagle.http.path.{->, /, Path}
import com.twitter.finagle.http.{Method, Request, Response}
import io.fintrospect.IncompletePath._
import io.fintrospect.ModuleSpec.ModifyPath
import io.fintrospect.parameters.{Path => Fp, PathParameter}

object IncompletePath {
  def apply(routeSpec: RouteSpec, method: Method): IncompletePath0 = new IncompletePath0(routeSpec, method, identity)

  private[fintrospect] def clientFor(ip: IncompletePath,
                                     service: Service[Request, Response],
                                     pp: PathParameter[_]*): RouteClient = {
    new RouteClient(ip.method, ip.routeSpec, Fp.fixed(ip.pathFn(Path("")).toString) +: pp, service)
  }
}

trait IncompletePath {
  type ModifyPath = Path => Path
  val routeSpec: RouteSpec
  val method: Method
  val pathFn: ModifyPath

  def bindToClient(service: Service[Request, Response]): RouteClient
}

class IncompletePath0(val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath) extends IncompletePath {
  def /(part: String) = new IncompletePath0(routeSpec, method, pathFn = pathFn.andThen(_ / part))

  def /[T](pp0: PathParameter[T]) = new IncompletePath1(routeSpec, method, pathFn, pp0)

  def bindTo[RS](fn: () => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path if matches(actualMethod, basePath, path) => {
          filtered.andThen(filter).andThen(fn())
        }
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service)
}

class IncompletePath1[A](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                         pp1: PathParameter[A]) extends IncompletePath {
  def /(part: String): IncompletePath2[A, String] = /(Fp.fixed(part))

  def /[B](pp2: PathParameter[B]): IncompletePath2[A, B] = new IncompletePath2(routeSpec, method, pathFn, pp1, pp2)

  def bindTo[RS](fn: (A) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1))
      }
    }

  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1)
}

class IncompletePath2[A, B](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                            pp1: PathParameter[A],
                            pp2: PathParameter[B]) extends IncompletePath {
  def /(part: String): IncompletePath3[A, B, String] = /(Fp.fixed(part))

  def /[C](pp3: PathParameter[C]): IncompletePath3[A, B, C] = new IncompletePath3(routeSpec, method, pathFn, pp1, pp2, pp3)

  def bindTo[RS](fn: (A, B) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1, pp2) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) / pp2(s2) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1, s2))
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2)
}

class IncompletePath3[A, B, C](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                               pp1: PathParameter[A],
                               pp2: PathParameter[B],
                               pp3: PathParameter[C]) extends IncompletePath {
  def /(part: String): IncompletePath4[A, B, C, String] = /(Fp.fixed(part))

  def /[D](pp4: PathParameter[D]): IncompletePath4[A, B, C, D] = new IncompletePath4(routeSpec, method, pathFn, pp1, pp2, pp3, pp4)

  def bindTo[RS](fn: (A, B, C) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1, pp2, pp3) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1, s2, s3))
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3)
}

class IncompletePath4[A, B, C, D](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                  pp1: PathParameter[A],
                                  pp2: PathParameter[B],
                                  pp3: PathParameter[C],
                                  pp4: PathParameter[D]
                                 ) extends IncompletePath {
  def /(part: String): IncompletePath5[A, B, C, D, String] = /(Fp.fixed(part))

  def /[E](pp5: PathParameter[E]): IncompletePath5[A, B, C, D, E] = new IncompletePath5(routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5)

  def bindTo[RS](fn: (A, B, C, D) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1, s2, s3, s4))
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4)
}

class IncompletePath5[A, B, C, D, E](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                     pp1: PathParameter[A],
                                     pp2: PathParameter[B],
                                     pp3: PathParameter[C],
                                     pp4: PathParameter[D],
                                     pp5: PathParameter[E]
                                    ) extends IncompletePath {
  def /(part: String): IncompletePath6[A, B, C, D, E, String] = /(Fp.fixed(part))

  def /[F](pp6: PathParameter[F]): IncompletePath6[A, B, C, D, E, F] = new IncompletePath6(routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6)

  def bindTo[RS](fn: (A, B, C, D, E) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1, s2, s3, s4, s5))
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4, pp5)
}

class IncompletePath6[A, B, C, D, E, F](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                        pp1: PathParameter[A],
                                        pp2: PathParameter[B],
                                        pp3: PathParameter[C],
                                        pp4: PathParameter[D],
                                        pp5: PathParameter[E],
                                        pp6: PathParameter[F]
                                       ) extends IncompletePath {
  def /(part: String): IncompletePath7[A, B, C, D, E, F, String] = /(Fp.fixed(part))

  def /[G](pp7: PathParameter[G]): IncompletePath7[A, B, C, D, E, F, G] = new IncompletePath7(routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6, pp7)

  def bindTo[RS](fn: (A, B, C, D, E, F) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) / pp6(s6) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1, s2, s3, s4, s5, s6))
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4, pp5, pp6)
}

class IncompletePath7[A, B, C, D, E, F, G](val routeSpec: RouteSpec, val method: Method, val pathFn: ModifyPath,
                                           pp1: PathParameter[A],
                                           pp2: PathParameter[B],
                                           pp3: PathParameter[C],
                                           pp4: PathParameter[D],
                                           pp5: PathParameter[E],
                                           pp6: PathParameter[F],
                                           pp7: PathParameter[G]
                                          ) extends IncompletePath {
  def bindTo[RS](fn: (A, B, C, D, E, F, G) => Service[Request, RS]): ServerRoute[RS] = new ServerRoute[RS](routeSpec, method, pathFn, pp1, pp2, pp3, pp4, pp5, pp6, pp7) {
    override def toPf(filter: Filter[Request, Response, Request, RS], basePath: Path) = {
      filtered: Filter[Request, Response, Request, Response] => {
        case actualMethod -> path / pp1(s1) / pp2(s2) / pp3(s3) / pp4(s4) / pp5(s5) / pp6(s6) / pp7(s7) if matches(actualMethod, basePath, path) => filtered.andThen(filter).andThen(fn(s1, s2, s3, s4, s5, s6, s7))
      }
    }
  }

  override def bindToClient(service: Service[Request, Response]) = clientFor(this, service, pp1, pp2, pp3, pp4, pp5, pp6, pp7)
}
