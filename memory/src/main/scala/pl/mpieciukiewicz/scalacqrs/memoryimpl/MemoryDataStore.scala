package pl.mpieciukiewicz.scalacqrs.memoryimpl

import org.slf4j.LoggerFactory
import pl.mpieciukiewicz.scalacqrs._
import pl.mpieciukiewicz.scalacqrs.exception.AggregateWasAlreadyDeletedException

class MemoryDataStore(val eventStore: EventStore) extends DataStore {

  private val Log = LoggerFactory.getLogger(classOf[MemoryDataStore])

  override def getAggregateByVersion[T](aggregateClass: Class[T], uid: UID, version: Int): Aggregate[T] = getAggregateWithOptionalVersion(aggregateClass, uid, Some(version))

  override def getAggregate[T](aggregateClass: Class[T], uid: UID): Aggregate[T] = getAggregateWithOptionalVersion(aggregateClass, uid, None)


  private def getAggregateWithOptionalVersion[T](aggregateRootClass: Class[T], uid: UID, version: Option[Int]): Aggregate[T] = {
    val eventRows = if (version.isDefined) {
      if (version.get < 1) {
        throw new IllegalArgumentException("Cannot get aggregates for versions lower than 1");
      } else {
        eventStore.getEventsForAggregateToVersion(aggregateRootClass, uid, version.get)
      }
    } else {
      eventStore.getEventsForAggregate(aggregateRootClass, uid)
    }

    if (eventRows.isEmpty) {
      throw new IllegalStateException("Aggregate of type " + aggregateRootClass + " does not exist.")
    }


    val creatorEventRow: EventRow[T] = eventRows.head
    if (creatorEventRow.expectedAggregateVersion != 0) {
      throw new IllegalStateException("CreatorEvent need to expect version 0 of an aggregate, as it not exists before that event.")
    }
    val aggregateRoot = creatorEventRow.event.asInstanceOf[CreationEvent[T]].apply()

    var aggregate = Aggregate(uid, 1, Some(aggregateRoot))

    eventRows.tail.foreach((eventRow) => {
      if (eventRow.expectedAggregateVersion == aggregate.version && aggregate.aggregateRoot.isDefined) {
        aggregate = eventRow.event match {
          case event: ModificationEvent[T] => Aggregate(aggregate.uid, aggregate.version + 1, Some(event.apply(aggregateRoot)))
          case event: DeletionEvent[T] => Aggregate(aggregate.uid, aggregate.version + 1, None)
        }
      } else if (aggregate.aggregateRoot.isEmpty) {
        throw new AggregateWasAlreadyDeletedException("Unexpected modification of already deleted aggregate")
      } else {
        throw new IllegalStateException("Unexpected version for aggregate when applying eventRow. " +
          "[aggregateType:" + aggregateRootClass.getName + ", aggregateId:" + uid + ", aggregateVersion:" +
          aggregate.version + "eventType:" + eventRow.getClass.getName + ", expectedVersion:" + eventRow.expectedAggregateVersion + "]")
      }
    })

    if (Log.isDebugEnabled) {
      Log.debug(eventRows.size + " eventRows applied for aggregate [type:" + aggregateRootClass.getName + ", uid:" + uid + "]")
    }
    aggregate
  }


}
