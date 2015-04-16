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

  val data = csv("data.csv").circular

  def website(): String = {
    """{"url":"${url}"}"""
  }

  val getWebsite = exec(
      http("wolak1")
      .get("/websites/${websiteId}")
      .headers(commonHeaders)
      .check(status.is(201))
      )

  val createArtist = exec(
      http("wolak4")
      .post("/artists")
      .body(StringBody("""{"name": "Wolakko", "label_id": ${labelId}}"""))
      .asJSON
      .headers(commonHeaders)
      .check(status.is(201))
      )

  val createLabel = exec(
      http("wolak3")
      .post("/labels")
      .body(StringBody("""{"country": "wolakolandia", "name": "Volaco Solutions"}"""))
      .asJSON
      .headers(commonHeaders)
      .check(jsonPath("$.id")
      .ofType[Int]
      .saveAs("labelId"))
      )

  val createWebsite = exec(
    http("Create Website")
      .post("/websites")
      .body(StringBody(website()))
      .asJSON
      .check(jsonPath("$.id")
      .ofType[Int]
      .saveAs("websiteId")))

  val singleUserScenario = scenario("Single user")
    .feed(data)
    .forever {
      feed(data)
      .pause(1)
      .exec(http("wolak").options("/").headers(commonHeaders))
      .exec(createWebsite)
      .exec(getWebsite)
      .exec(createLabel)
      .exec(createArtist)
  }

  setUp(
    singleUserScenario.inject(
      rampUsers(maxUsers()) over (rampDurationSec() seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
