/**
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
package com.scattersphere.core.util

/**
  * RunnableTask class
  *
  * This class extends the Runnable class, of which you must override the run() method.  The [[RunnableTask]]
  * class adds an additional method that can be overridden, which will be called when the run() method completes
  * without any exceptions.
  */
abstract class RunnableTask extends Runnable {

  /**
    * This function is called after the run() method completes without any fault.
    *
    * @return Runnable containing the function to run when a task is complete.
    */
  def onFinished(): Unit = {
    println(s"Job finished.")
  }

}

/**
  * Convenience [[RunnableTask]] object
  *
  * This can be used to wrap a Runnable object without having to fully implement all of the [[RunnableTask]]
  * methods.
  */
object RunnableTask {

  def apply(runnable: Runnable): RunnableTask = new RunnableTask {
    override def run(): Unit = {
      runnable.run()
    }
  }

}