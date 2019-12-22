package io.prismic

import com.github.blemale.scaffeine.{AsyncCache, AsyncLoadingCache, Scaffeine}
import play.api.libs.ws._
import scala.concurrent._
import scala.concurrent.duration._
import scala.language.postfixOps

final class Prismic(implicit ws: WSClient, ec: ExecutionContext) {

  def get(endpoint: String): Future[Api] = apiCache get endpoint

  private[prismic] val apiCache: AsyncLoadingCache[String, Api] = Scaffeine()
    .expireAfterWrite(6 seconds)
    .buildAsyncFuture[String, Api](fetch)

  private[prismic] val formCache: AsyncCache[String, Response] = Scaffeine()
    .expireAfterWrite(1 hour)
    .buildAsync[String, Response]

  private def fetch(endpoint: String): Future[Api] =
    ws.url(endpoint)
      .withHttpHeaders("Accept" -> "application/json")
      .get()
      .map { resp =>
        resp.status match {
          case 200 => resp.json
          case 401 =>
            throw UnexpectedError(
              "Authorization error, but not URL was provided"
            )
          case err =>
            throw UnexpectedError(
              s"Got an HTTP error $err (${resp.statusText})"
            )
        }
      }
      .map { json =>
        new Api(
          ApiData.reader
            .reads(json)
            .getOrElse(sys.error(s"Error while parsing API document: $json")),
          formCache
        )
      }
}

object Prismic {

  /**
    * Name of the cookie that prismic will use to store information about the
    * content currently in preview (only for writers)
    */
  val previewCookie = "io.prismic.preview"

}
