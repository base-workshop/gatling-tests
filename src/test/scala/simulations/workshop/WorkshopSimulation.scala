package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import simulations.Common._

import scala.concurrent.duration._

class WorkshopSimulation extends Simulation {

  val country = "Poland"
  val name = "Sony"

  val label =
    s"""
      |{
      | "country": "$country",
      | "name": "$name"
      |}
    """.stripMargin

  val createLabel = exec(
    http("Create label")
      .post("/labels")
      .headers(commonHeaders)
      .body(StringBody(label))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.country").is(country))
      .check(jsonPath("$.name").is(name))
      .check(jsonPath("$.id").exists.saveAs("id"))
  )

  val getLabel = exec(
    http("Get label")
      .get("/labels/${id}")
      .check(status.is(200))
      .asJSON
      .check(jsonPath("$.country").is(country))
      .check(jsonPath("$.name").is(name))
      .check(jsonPath("$.id").is("${id}"))
  )

  val singleUserScenario = scenario("Single user")
    .forever {
    pause(1)
      .exec(createLabel).exec(getLabel)
  }

  setUp(
    singleUserScenario.inject(
      rampUsers(maxUsers()) over (rampDurationSec() seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
