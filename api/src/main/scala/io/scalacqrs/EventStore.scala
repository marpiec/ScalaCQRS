package io.scalacqrs

import io.scalacqrs.data.{UserId, AggregateId}
import io.scalacqrs.event.{EventRow, Event}

import scala.collection.mutable

trait EventStore {

  private val eventListeners = mutable.Map[Class[_], mutable.ListBuffer[AggregateUpdated[_] => Unit]]()

  def countAllAggregates[T](aggregateClass: Class[T]): Long

  def getAllAggregateIds[T](aggregateClass: Class[T]): Seq[AggregateId]

  def addFirstEvent(commandId: CommandId, userId: UserId, newAggregateId: AggregateId, event: Event[_])

  def addEvent(commandId: CommandId, userId: UserId, aggregateId: AggregateId, expectedVersion: Int, event: Event[_])

  def getEventsForAggregate[T](aggregateClass: Class[T], uid: AggregateId): Seq[EventRow[T]]

  def getEventsForAggregateFromVersion[T](aggregateClass: Class[T], uid: AggregateId, fromVersion: Int): Seq[EventRow[T]]

  def getEventsForAggregateToVersion[T](aggregateClass: Class[T], uid: AggregateId, toVersion: Int): Seq[EventRow[T]]

  def addEventListener[T](aggregateClass: Class[T], eventListener: AggregateUpdated[T] => Unit): Unit = {
    val eventListenersForType = eventListeners.getOrElseUpdate(aggregateClass, mutable.ListBuffer())
    eventListenersForType += eventListener.asInstanceOf[AggregateUpdated[_] => Unit]
  }

  protected def callEventListeners[T](aggregateId: AggregateId, version: Int, event: Event[T]): Unit = {
    val eventListenersForType = eventListeners.getOrElse(event.aggregateType, mutable.ListBuffer())
    eventListenersForType.asInstanceOf[mutable.ListBuffer[AggregateUpdated[T] => Unit]]
      .foreach(_.apply(AggregateUpdated(aggregateId, version, event)))
  }
}
