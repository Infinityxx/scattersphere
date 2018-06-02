package com.scattersphere.tasks.spark

import com.scattersphere.core.util.execution.JobExecutor
import com.scattersphere.core.util.{JobBuilder, RunnableTask, TaskBuilder}
import com.scattersphere.core.util.spark.SparkCache
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.SparkConf
import org.scalatest.{FlatSpec, Matchers}

import scala.math.random

class SparkTaskPiTest extends FlatSpec with Matchers with LazyLogging {

  SparkCache.save("sparkPiTestCache", new SparkConf()
    .setMaster("local[*]")
    .setAppName("local pi test")
    .set("spark.ui.enabled", "false"))

  "Spark task pi test" should "calculate Pi in the form of a task" in {
    val piTask: SparkTask = new SparkTask("sparkPiTestCache") {
      override def run(): Unit = {
        val slices = 2
        val n = math.min(100000L * slices, Int.MaxValue).toInt
        val count = getContext().parallelize(1 until n, slices).map { _ =>
          val x = random * 2 - 1
          val y = random * 2 - 1

          if (x * x + y * y <= 1) 1 else 0
        }.reduce(_ + _)

        val pi = 4.0 * count / (n - 1)

        assert(pi >= 3.0 && pi <= 3.2)

        println(s"Pi calculated to approximately ${pi}")
      }
    }

    var sTask = TaskBuilder()
      .withTask(piTask)
      .withName("Pi Task")
      .build()
    var sJob = JobBuilder()
      .withTasks(sTask)
      .build()
    var jExec: JobExecutor = JobExecutor(sJob)

    jExec.queue().run()
  }

}
