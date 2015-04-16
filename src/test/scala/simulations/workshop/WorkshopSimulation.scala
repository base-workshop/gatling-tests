package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import scala.concurrent.duration._
import simulations.Common._

class WorkshopSimulation extends Simulation {


  val label =
    """
      |{
      |"country": "Poland",
      |"name": "sony"
      |}
    """.stripMargin

  val createLabel = exec(
  http("Create label")
  .post("/labels")
    .headers(commonHeaders)
  .body(StringBody(label))
  .asJSON
  .check(status.is(201))
  )

  val singleUserScenario = scenario("Single user")
    .forever {
      pause(1)
      .exec(createLabel)
  }

  setUp(
    singleUserScenario.inject(
      rampUsers(maxUsers()) over (rampDurationSec() seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
