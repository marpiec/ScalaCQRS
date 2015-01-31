package pl.mpieciukiewicz.scalacqrs.memoryimpl

import java.time.Clock

import pl.mpieciukiewicz.scalacqrs._
import pl.mpieciukiewicz.scalacqrs.exception.{NoEventsForAggregateException, ConcurrentAggregateModificationException}
import pl.mpieciukiewicz.scalacqrs.internal.Event

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class MemoryEventStore(clock: Clock) extends EventStore {

  private val eventsByType = mutable.Map[Class[_], mutable.Map[AggregateId, ListBuffer[EventRow[_]]]]()

  override def addCreationEvent(commandId: CommandId, newAggregateId: AggregateId, event: CreationEvent[_]): Unit = {
    val eventsForType = eventsByType.getOrElseUpdate(event.aggregateType, new mutable.HashMap[AggregateId, ListBuffer[EventRow[_]]])
    val eventsForEntity = eventsForType.getOrElseUpdate(newAggregateId, new ListBuffer[EventRow[_]])
    val eventRow = EventRow(commandId, newAggregateId, 1, clock.instant(), event)
    eventsForEntity += eventRow
  }

  override def addModificationEvent(commandId: CommandId, aggregateId: AggregateId, expectedVersion: Int, event: ModificationEvent[_]): Unit = {
      val eventsForType = eventsByType.getOrElse(event.aggregateType, mutable.Map())
      val eventsForEntity = eventsForType.getOrElseUpdate(aggregateId, new ListBuffer[EventRow[_]])
      
      if (eventsForEntity.size > expectedVersion) {
        throw new ConcurrentAggregateModificationException("Expected version " + expectedVersion + " but is " + eventsForEntity.size)
      }

      eventsForEntity += EventRow(commandId, aggregateId, expectedVersion + 1, clock.instant(), event)
  }


  override def addDeletionEvent(commandId: CommandId, aggregateId: AggregateId, expectedVersion: Int, event: DeletionEvent[_]): Unit = {
    val eventsForType = eventsByType.getOrElse(event.aggregateType, mutable.Map())
    val eventsForEntity = eventsForType.getOrElseUpdate(aggregateId, new ListBuffer[EventRow[_]])

    if (eventsForEntity.size > expectedVersion) {
      throw new ConcurrentAggregateModificationException("Expected version " + expectedVersion + " but is " + eventsForEntity.size)
    }

    eventsForEntity += EventRow(commandId, aggregateId, expectedVersion + 1, clock.instant(), event)
  }

  override def getEventsForAggregate[T](aggregateClass: Class[T], uid: AggregateId): Seq[EventRow[T]] = {
    eventsByType.
      getOrElse(aggregateClass, throw new NoEventsForAggregateException("No events found for type " + aggregateClass)).
      getOrElse(uid, throw new NoEventsForAggregateException("No events found for type " + aggregateClass + " with uid " + uid)).
      toList.asInstanceOf[List[EventRow[T]]]
  }

  override def getEventsForAggregateFromVersion[T](aggregateClass: Class[T], uid: AggregateId, fromVersion: Int): Seq[EventRow[T]] = {
    getEventsForAggregate(aggregateClass, uid).drop(fromVersion)
  }

  override def getEventsForAggregateToVersion[T](aggregateClass: Class[T], uid: AggregateId, toVersion: Int): Seq[EventRow[T]] = {
    getEventsForAggregate(aggregateClass, uid).take(toVersion)
  }

  override def getAllAggregateIds[T](aggregateClass: Class[T]): Seq[AggregateId] = {
    eventsByType.get(aggregateClass).map(_.keys.toList).getOrElse(List())
  }

  override def countAllAggregates[T](aggregateClass: Class[T]): Long = {
    eventsByType.get(aggregateClass).map(_.size.toLong).getOrElse(0L)
  }
}
