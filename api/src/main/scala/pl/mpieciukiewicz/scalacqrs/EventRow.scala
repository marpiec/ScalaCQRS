package pl.mpieciukiewicz.scalacqrs

import java.time.Instant

import pl.mpieciukiewicz.scalacqrs.internal.Event

/**
 * ...
 * @author Marcin Pieciukiewicz
 */

case class EventRow[T](userId: UID, aggregateId: UID, version:Int, creationTimestamp: Instant, event: Event[T])
