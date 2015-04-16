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
    HttpHeaderNames.Accept -> HttpHeaderValues.ApplicationJson)

  val data = csv("data.csv").circular

  def website(): String = {
    """{"url":"${url}"}"""
  }

  val createWebsite = exec(
    http("Create Website")
      .post("/websites")
      .body(StringBody(website()))
      .asJSON
      .check(status.is(201)))

  val getTracks = exec(
    http("Get Tracks").get("/tracks").check(status.is(200)))

  val createTrack = exec(
    http("Create Track")
      .post("/tracks")
      .body(StringBody("{\"name\":\"asdas\",\"number\":3,\"album_id\":5}"))
      .asJSON
      .check(status.is(201)).check(jsonPath("$.id").saveAs("id")))

  def deleteTrack(id: String) = {
    println("--" + id)
    exec(
      http("Delete Track").delete(s"/tracks/${id}").check(status.is(200))
        .check(jsonPath("$").saveAs("removedId")))
  }

  val singleUserScenario = scenario("Single user")
    .feed(data)
    .forever {
      feed(data)
        .pause(1)
        .exec(http("discovery").options("/").headers(commonHeaders))
        .exec(createWebsite)
        .exec(getTracks)
        .exec(createTrack).exec(session => {
          session.get("id").asOption[String] map deleteTrack
          session
        })
        .exec(session => {
          session.get("removedId").asOption[String] map println
          session
        })
    }

  setUp(
    singleUserScenario.inject(
      rampUsers(1) over (2 seconds))).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}