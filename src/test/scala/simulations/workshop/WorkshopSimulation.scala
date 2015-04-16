package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import scala.concurrent.duration._
import simulations.Common._

class WorkshopSimulation extends Simulation {

  val httpConf = http
//    .proxy(Proxy("127.0.0.1", 8888).httpsPort(8888))
    .baseURL("https://workshop.uxguards.com/whipping-boy/api")
    .disableCaching

  val commonHeaders = Map(
    "User-Agent" -> "gatling/simulation",
    HttpHeaderNames.Accept -> HttpHeaderValues.ApplicationJson
  )

  val data = csv("labels.csv").circular

  def label(): String = {
    """{"country": "${country}",
        "name":"${name}"}"""
  }

  def artist(): String = {
    """{"name": "Krizoooo",
        "label_Id": "${labelID}"}"""
  }

  val createLabel = exec(
    http("Create Label")
      .post("/labels")
      .body(StringBody(label()))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id")
        .saveAs("labelID"))
  )

  val getLabel = exec(
    http("Get label")
      .get("/labels/${labelID}")
      .headers(commonHeaders)
      .check(status.is(200))
    )

  val createArtist = exec(
    http("Create Artist")
      .post("/artists")
      .body(StringBody(artist()))
      .asJSON
      .check(status.is(201))
  )

  val singleUserScenario = scenario("Single user")
    .feed(data)
    .forever {
      feed(data)
      .pause(1)
      .exec(http("kriz").options("/").headers(commonHeaders))
      .exec(createLabel)
      .exec(getLabel)
      .exec(createArtist)
  }

  setUp(
    singleUserScenario.inject(
      rampUsers(1) over (2 seconds)
    )
  ).protocols(httpConf)
    .maxDuration(15 seconds)

}
