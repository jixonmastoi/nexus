package ch.epfl.bluebrain.nexus.delta.plugins.elasticsearch.routes

import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.epfl.bluebrain.nexus.delta.plugins.elasticsearch.IdResolution
import ch.epfl.bluebrain.nexus.delta.rdf.jsonld.context.RemoteContextResolution
import ch.epfl.bluebrain.nexus.delta.rdf.syntax.uriSyntax
import ch.epfl.bluebrain.nexus.delta.rdf.utils.JsonKeyOrdering
import ch.epfl.bluebrain.nexus.delta.sdk.acls.AclCheck
import ch.epfl.bluebrain.nexus.delta.sdk.directives.AuthDirectives
import ch.epfl.bluebrain.nexus.delta.sdk.directives.DeltaDirectives._
import ch.epfl.bluebrain.nexus.delta.sdk.fusion.FusionConfig
import ch.epfl.bluebrain.nexus.delta.sdk.identities.Identities
import ch.epfl.bluebrain.nexus.delta.sdk.model.BaseUri
import monix.execution.Scheduler

class IdResolutionRoutes(
    identities: Identities,
    aclCheck: AclCheck,
    idResolution: IdResolution
)(implicit
    baseUri: BaseUri,
    s: Scheduler,
    jko: JsonKeyOrdering,
    rcr: RemoteContextResolution,
    fusionConfig: FusionConfig
) extends AuthDirectives(identities, aclCheck) {

  def routes: Route = concat(resolutionRoute, proxyRoute)

  private def resolutionRoute: Route =
    pathPrefix("resolve") {
      extractCaller { implicit caller =>
        (get & iriSegment & pathEndOrSingleSlash) { iri =>
          val resolved = idResolution.resolve(iri)
          emit(resolved)
        }
      }
    }

  private def proxyRoute: Route =
    pathPrefix("resolve-proxy-pass") {
      extractUnmatchedPath { path =>
        get {
          val resourceId = fusionConfig.resolveBase / path
          emitOrFusionRedirect(
            fusionResolveUri(resourceId),
            redirect(deltaResolveEndpoint(resourceId), StatusCodes.SeeOther)
          )
        }
      }
    }

  private def deltaResolveEndpoint(id: Uri): Uri =
    baseUri.endpoint / "resolve" / id.toString

}
