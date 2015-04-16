package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._
import simulations.Common._

import scala.concurrent.duration._

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

  def artistBody(name:String): String = {
    s"""{"name":"$name"}"""
  }

  def albumBody(): String = {
    """{"name":"Best album ever", "year": 2015, "artist_id":${artist-id}}"""
  }

  val createArtist = exec(
    http("Create Artist")
      .post("/artists")
      .headers(commonHeaders)
      .body(StringBody(artistBody("Tonny")))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("artist-id"))
  )

  val createAlbum = exec(
    http("Create Album")
      .post("/albums")
      .headers(commonHeaders)
      .body(StringBody(albumBody()))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("album-id"))
  )

  val singleCreateCoverScenario = scenario("Create cover scernario")
    .feed(data)
    .forever {
    feed(data)
      .pause(1)
      .exec(http("discovery").options("/").headers(commonHeaders))
      .exec(createArtist)
      .exec(createAlbum)
  }

  setUp(
    singleCreateCoverScenario.inject(
      rampUsers(1) over (2 seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
