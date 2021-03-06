/*
 *    _____            __  __                       __
 *   / ___/_________ _/ /_/ /____  ______________  / /_  ___  ________
 *   \__ \/ ___/ __ `/ __/ __/ _ \/ ___/ ___/ __ \/ __ \/ _ \/ ___/ _ \
 *  ___/ / /__/ /_/ / /_/ /_/  __/ /  (__  ) /_/ / / / /  __/ /  /  __/
 * /____/\___/\__,_/\__/\__/\___/_/  /____/ .___/_/ /_/\___/_/   \___/
 *                                       /_/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.buildfactory.scattersphere.core.util

import io.buildfactory.scattersphere.core.util.execution.JobExecutor
import io.buildfactory.scattersphere.core.util.logging.SimpleLogger
import org.scalatest.{FlatSpec, Matchers}

class CancelJobTest extends FlatSpec with Matchers with SimpleLogger {

  class SleeperRunnable(time: Int) extends Runnable {
    override def run(): Unit = {
      logger.info(s"Sleeping $time second(s).")
      Thread.sleep(time * 1000)
      logger.info("Sleep complete.")
    }
  }

  "Job Executor" should "be able to cancel a list of tasks" in {
    val task1: Task = TaskBuilder("1").withTask(RunnableTask(new SleeperRunnable(1))).build()
    val task2: Task = TaskBuilder("2").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task1).build()
    val task3: Task = TaskBuilder("3").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task2).build()
    val task4: Task = TaskBuilder("4").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task3).build()
    val task5: Task = TaskBuilder("5").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task4).build()
    val task6: Task = TaskBuilder("6").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task5).build()
    val task7: Task = TaskBuilder("7").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task6).build()
    val task8: Task = TaskBuilder("8").withTask(RunnableTask(new SleeperRunnable(1))).dependsOn(task7).build()
    val job1: Job = JobBuilder("Timer Task").withTasks(task1, task2, task3, task4, task5, task6, task7, task8).build()
    val jobExec: JobExecutor = JobExecutor(job1)

    assert(job1.id > 0)
    assert(task1.id > 0)
    assert(task2.id > task1.id)
    assert(task3.id > task2.id)
    assert(task4.id > task3.id)
    assert(task5.id > task4.id)
    assert(task6.id > task5.id)
    assert(task7.id > task6.id)
    assert(task8.id > task7.id)
    jobExec.setBlocking(false).queue().run()
    Thread.sleep(2500)
    jobExec.cancel("Canceling for testing sake.")

    Thread.sleep(2000)

    job1.status match {
      case JobCanceled(x) => x shouldBe "Canceling for testing sake."
      case x => fail(s"Unexpected job status matched: $x")
    }

    var taskFinished: Int = 0
    var taskCanceled: Int = 0

    job1.tasks.foreach(task => task.status() match {
      case TaskCanceled(_) => taskCanceled += 1
      case TaskFinished => taskFinished += 1
      case _ => // Ignored
    })

    assert(taskFinished > 0)
    assert(taskCanceled > 0)
  }

}
