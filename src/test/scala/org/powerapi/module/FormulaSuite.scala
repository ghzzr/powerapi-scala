/**
 * This software is licensed under the GNU Affero General Public License, quoted below.
 *
 * This file is a part of PowerAPI.
 *
 * Copyright (C) 2011-2014 Inria, University of Lille 1.
 *
 * PowerAPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * PowerAPI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with PowerAPI.

 * If not, please consult http://www.gnu.org/licenses/agpl-3.0.html.
 */
package org.powerapi.module

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.testkit.{TestActorRef, TestKit}
import org.powerapi.UnitTest
import org.powerapi.core.{Channel, MessageBus}

object SensorMockChannel extends SensorChannel {
  private val topic = "test"

  case class SensorMockReport(topic: String, muid: UUID, power: Double) extends SensorReport

  def subscribeMockMessage: MessageBus => ActorRef => Unit = {
    subscribe(topic)
  }

  def publishSensorMockReport(muid: UUID, power: Double): MessageBus => Unit = {
    publish(SensorMockReport(topic, muid, power))
  }
}

class FormulaMock(eventBus: MessageBus, actorRef: ActorRef, coeff: Double) extends FormulaComponent(eventBus) {
  import org.powerapi.module.SensorMockChannel.{SensorMockReport, subscribeMockMessage}

  type SR = SensorMockReport

  def subscribeSensorReport(): Unit = {
    subscribeMockMessage(eventBus)(self)
  }

  def compute(sensorReport: SensorMockReport): Unit = {
    actorRef ! sensorReport.power * coeff
  }
}

class FormulaSuite(system: ActorSystem) extends UnitTest(system) {

  def this() = this(ActorSystem("FormulaSuite"))

  override def afterAll() = {
    TestKit.shutdownActorSystem(system)
  }

  trait Bus {
    val eventBus = new MessageBus
  }

  "A Formula" should "process SensorReport messages" in new Bus {
    import org.powerapi.module.SensorMockChannel.publishSensorMockReport

    val coeff = 10d
    val formulaMock = TestActorRef(Props(classOf[FormulaMock], eventBus, testActor, coeff))(system)

    val muid = UUID.randomUUID()
    val power = 2.2d

    publishSensorMockReport(muid, power)(eventBus)
    expectMsgClass(classOf[Double]) should equal(power * coeff)
  }
}
