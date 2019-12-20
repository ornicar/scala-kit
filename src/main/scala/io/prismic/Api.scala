package io.prismic

import org.joda.time._

import play.api.libs.functional.syntax._
import play.api.libs.json._
import play.api.libs.json.JodaReads._
import play.api.libs.ws._

import scala.util.control.Exception._
import scala.concurrent._
import scala.language.postfixOps

/**
  * High-level entry point for communications with prismic.io API
  */
final class Api(data: ApiData) {

  def refs: Map[String, Ref] =
    data.refs.groupBy(_.label).view.mapValues(_.head).toMap
  def bookmarks: Map[String, String] = data.bookmarks
  def forms: Map[String, SearchForm] =
    data.forms.view
      .mapValues(form => SearchForm(this, form, form.defaultData))
      .toMap
  def master: Ref =
    refs.values
      .collectFirst { case ref if ref.isMasterRef => ref }
      .getOrElse(sys.error("no master reference found"))

  /**
    * Return the URL to display a given preview
    * @param token as received from Prismic server to identify the content to preview
    * @param linkResolver the link resolver to build URL for your site
    * @param defaultUrl the URL to default to return if the preview doesn't correspond to a document
    *                (usually the home page of your site)
    * @return a Future corresponding to the URL you should redirect the user to preview the requested change
    */
  def previewSession(
      token: String,
      linkResolver: DocumentLinkResolver,
      defaultUrl: String
  )(implicit ws: WSClient, ec: ExecutionContext): Future[String] = {
    try {
      (for {
        tokenJson <- ws
          .url(token)
          .withHttpHeaders("Accept" -> "application/json")
          .get()
          .map(_.json)
        mainDocumentId = (tokenJson \ "mainDocument").as[String]
        results <- forms("everything")
          .query(Predicate.at("document.id", mainDocumentId))
          .ref(token)
          .submit()
        document = results.results.head
      } yield {
        linkResolver(document.asDocumentLink)
      }).recoverWith {
        case _ => Future.successful(defaultUrl)
      }
    } catch {
      case _: Exception => Future.successful(defaultUrl)
    }
  }

  def oauthInitiateEndpoint = data.oauthEndpoints._1
  def oauthTokenEndpoint = data.oauthEndpoints._2
}

/**
  * Instanciate an Api instance from a prismic.io API URL
  */
object Api {

  /**
    * Instantiate an Api instance from a prismic.io API URL
    */
  def get(
      endpoint: String
  )(implicit ws: WSClient, ec: ExecutionContext): Future[Api] =
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
            .getOrElse(sys.error(s"Error while parsing API document: $json"))
        )
      }
}

/**
  * Represent a prismic.io reference, a fixed point in time.
  *
  * The references must be provided when accessing to any prismic.io resource
  * (except /api) and allow to assert that the URL you use will always
  * returns the same results.
  */
case class Ref(
    id: String,
    ref: String,
    label: String,
    isMasterRef: Boolean = false,
    scheduledAt: Option[DateTime] = None
)

private[prismic] object Ref {

  implicit val reader = (
    (__ \ "id").read[String] and
      (__ \ "ref").read[String] and
      (__ \ "label").read[String] and
      ((__ \ "isMasterRef").read[Boolean] orElse Reads.pure(false)) and
      (__ \ "scheduledAt").readNullable[DateTime]
  )(Ref.apply _)

}

/**
  * A prismic.io document field metadata
  */
case class Field(`type`: String, multiple: Boolean, default: Option[String])

private[prismic] object Field {
  implicit val reader = (
    (__ \ "type").read[String] and
      (__ \ "multiple").readNullable[Boolean].map(_.getOrElse(false)) and
      (__ \ "default").readNullable[String]
  )(Field.apply _)
}

case class Form(
    name: Option[String],
    method: String,
    rel: Option[String],
    enctype: String,
    action: String,
    fields: Map[String, Field]
) {

  def defaultData: Map[String, Seq[String]] = {
    fields.view
      .mapValues(_.default)
      .collect {
        case (key, Some(value)) => (key, Seq(value))
      }
      .toMap
  }

}

private[prismic] object Form {
  implicit val reader = Json.reads[Form]
}

case class ApiData(
    refs: Seq[Ref],
    bookmarks: Map[String, String],
    types: Map[String, String],
    tags: Seq[String],
    forms: Map[String, Form],
    oauthEndpoints: (String, String)
)

object ApiData {

  implicit val reader = (
    (__ \ "refs").read[Seq[Ref]] and
      (__ \ "bookmarks").read[Map[String, String]] and
      (__ \ "types").read[Map[String, String]] and
      (__ \ "tags").read[Seq[String]] and
      (__ \ "forms").read[Map[String, Form]] and
      (
        (__ \ "oauth_initiate").read[String] and
          (__ \ "oauth_token").read[String] tupled
      )
  )(ApiData.apply _)

}
