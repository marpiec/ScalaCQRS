package pl.mpieciukiewicz.scalacqrs

import java.util.concurrent.{Executors, ThreadPoolExecutor}

import org.fest.assertions.api.Assertions.assertThat
import org.postgresql.ds.jdbc4.AbstractJdbc4PoolingDataSource

import org.scalatest.{FeatureSpec, GivenWhenThen}
import pl.mpieciukiewicz.jdbs.ConnectionPoolFactory
import pl.mpieciukiewicz.scalacqrs.postgresimpl.PostgresUidGenerator

import scala.collection.parallel.{ExecutionContextTaskSupport, ThreadPoolTaskSupport, ForkJoinTaskSupport}
import scala.collection.parallel.immutable.{ParRange}
import scala.concurrent.ExecutionContext

class MemorySequentialUIDGeneratorSpec extends FeatureSpec with GivenWhenThen {


  feature("Generation of sequential unique identifiers") {

    scenario("Getting few numbers from generator") {

      Given("MemorySequentialGenerator instance")

      val dataSource = ConnectionPoolFactory.createEventStoreConnectionPool


      val generator = new PostgresUidGenerator(dataSource)
      val generations = 100000

      When("Menu UIDs are generated")

      val parRange = Range(0, generations).par
      parRange.tasksupport = new ForkJoinTaskSupport(new scala.concurrent.forkjoin.ForkJoinPool(10))
      val generatedUIDs = parRange.map(el => generator.nextAggregateId).toSet

      Then("Number of unique generated UID is equal to number of generations")

      assertThat(generatedUIDs.size).isEqualTo(generations)

    }


  }

  def runInParallel(count: Int)(block: () => Set[AggregateId]): Unit = {


  }
}
