package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import simulations.Common._

import scala.concurrent.duration._

class WorkshopSimulation extends Simulation {

  val country = "Poland"
  val name = "Sony"
  val updatedName = "Sony2"

  val label =
    s"""
       |{
       |"country": "$country",
                              |"name": "$name"
                                               |}
    """.stripMargin

  val putLabel =
    s"""
       |{
       |"country": "$country",
                              |"name": "$updatedName"
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

  val updateLabel = exec(http("Update label")
    .put("/labels/${id}")
    .body(StringBody(putLabel)).asJSON
    .check(status.is(201))
    .check(jsonPath("$.country").is(country))
    .check(jsonPath("$.name").is(updatedName))
    .check(jsonPath("$.id").is("${id}")
    .saveAs("id")
    )
  )


  val singleUserScenario = scenario("Single user")
    .forever {

    pause(1)
      .exec(createLabel)
      .exec(getLabel)
      .exec(updateLabel)

  }


  setUp(
    singleUserScenario.inject(
      rampUsers(maxUsers()) over (rampDurationSec() seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
