package ch.epfl.bluebrain.nexus.delta.service.organizations

import ch.epfl.bluebrain.nexus.delta.sdk.Organizations
import ch.epfl.bluebrain.nexus.delta.sdk.model.Envelope
import ch.epfl.bluebrain.nexus.delta.sdk.model.organizations.OrganizationEvent
import ch.epfl.bluebrain.nexus.delta.sdk.testkit.OrganizationsBehaviors
import ch.epfl.bluebrain.nexus.delta.service.utils.EventLogUtils
import ch.epfl.bluebrain.nexus.delta.service.{AbstractDBSpec, ConfigFixtures}
import ch.epfl.bluebrain.nexus.sourcing.EventLog
import ch.epfl.bluebrain.nexus.testkit.CirceLiteral
import monix.bio.Task
import org.scalatest.{Inspectors, OptionValues}

class OrganizationsImplSpec
    extends AbstractDBSpec
    with OrganizationsBehaviors
    with OptionValues
    with Inspectors
    with CirceLiteral
    with ConfigFixtures {

  private def eventLog: Task[EventLog[Envelope[OrganizationEvent]]] =
    EventLog.postgresEventLog(EventLogUtils.toEnvelope)

  override def create: Task[Organizations] =
    eventLog.flatMap { el =>
      OrganizationsImpl(OrganizationsConfig(aggregate, keyValueStore, pagination, indexing), el)
    }
}