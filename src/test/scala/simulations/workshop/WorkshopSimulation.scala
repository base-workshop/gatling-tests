package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import scala.concurrent.duration._
import simulations.Common._

class WorkshopSimulation extends Simulation {

  val httpConf = http
    .proxy(Proxy("127.0.0.1", 8888).httpsPort(8888))
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

  def artist(): String = {
    """{ "name": "Sebasitons", "label_id": ${labelId} }"""
  }

  def label(): String = {
    """{ "name": "Wojas Music Poland", "country": "Nibylandia" }"""
  }

  val createWebsite = exec(
    http("Create Website")
      .post("/websites")
      .body(StringBody(website()))
      .asJSON
      .check(jsonPath("$.id")
      .ofType[Int]
      .saveAs("websiteId"))
  )
  val getWebsite = exec(
    http("Get Website")
      .get("/websites/${websiteId}")
      .headers(commonHeaders)
      .check(status.is(200))
  )

  val createLabel = exec(
    http("Create awsome label")
      .post("/labels")
      .body(StringBody(label()))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id")
      .ofType[Int]
      .saveAs("labelId"))
  )

  val getLabel = exec(
    http("Get Labelosy")
      .get("/labels/${labelId}")
      .headers(commonHeaders)
      .check(status.is(200))
  )

  val createArtists = exec(
    http("Create awsome artist")
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
      .exec(http("misiaczki").options("/").headers(commonHeaders))
      .exec(createWebsite).exec(getWebsite)
      .exec(createLabel)
      .exec(getLabel)
      .exec(createArtists)
  }

  setUp(
    singleUserScenario.inject(
      rampUsers(2) over (2 seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
