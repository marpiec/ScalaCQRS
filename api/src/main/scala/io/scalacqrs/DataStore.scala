package io.scalacqrs

import java.lang.reflect.Type

import event.Event
import eventhandler.EventHandler
import io.scalacqrs.data.AggregateId
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl

import collection.mutable
import scala.util.Try

import scala.reflect.runtime.universe._

abstract class DataStore[A : TypeTag](private val eventStore: EventStore) {

  eventStore.registerDataStore(this)

  protected val eventHandlers = mutable.HashMap[Class[Event[A]], EventHandler[A, _ <: Event[A]]]()

  def countAllAggregates(): Long

  def getAllAggregateIds(): Seq[AggregateId]

  def getAggregate(id: AggregateId): Try[Aggregate[A]]

  def getAggregates(ids: Seq[AggregateId]): Seq[Aggregate[A]]

  def getAggregateByVersion(id: AggregateId, version: Int): Try[Aggregate[A]]

  /* Method created for optimization. This method should use caches.
  * Caches won't be used on undo events.
  * For now only Event store has good access to events */
  private[scalacqrs] def getAggregateByVersionAndApplyEventToIt(
                            id: AggregateId, version: Int, event: Event[A]): Try[Aggregate[A]]

  def typeInfo = typeOf[A]
}
