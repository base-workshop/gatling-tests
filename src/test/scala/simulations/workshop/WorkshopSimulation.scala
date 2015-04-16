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
    HttpHeaderNames.Accept -> HttpHeaderValues.ApplicationJson,
    HttpHeaderNames.ContentType -> HttpHeaderValues.ApplicationJson
  )

  val data = csv("data.csv").circular

  def artist(): String = {
    """{"name":"${name}"}"""
  }

  def newArtist(): String = {
    """{"name":"${name}-xxx"}"""
  }

  val createArtist = exec(
    http("Create Website")
      .post("/artists")
      .body(StringBody(artist()))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("artist_id"))
  )

  val updateArtist = exec(
    http("Update artist")
      .put("/artists/${artist_id}")
      .body(StringBody(newArtist()))
      .asJSON
      .check(status.is(201))
  )

  var listArtists = exec(
    http("List artists")
      .get("/artists")
      .asJSON
      .check(status.is(200))
      .check(jsonPath("$..id").ofType[Int].findAll.saveAs("ids"))
  )


  val deleteAllArtists = scenario("Delete all artists")
  .forever {
    exec(listArtists)
    .foreach("${ids}", "id") {
      exec(
        http("Delete artist")
        .delete("/artists/${id}")
        .check(status.is(200))
      )
      .pause(2)
    }
  }

  val singleUserScenario = scenario("Single user")
    .feed(data)
    .forever {
      feed(data)
      .pause(1)
      //.exec(http("discovery").options("/").headers(commonHeaders))
      .exec(createArtist)
      .exec(updateArtist)
  }

  setUp(
    singleUserScenario.inject(
      rampUsers(1) over (2 seconds)
    )
//    deleteAllArtists.inject(
//      rampUsers(1) over (2 seconds)
//    )
  ).protocols(httpConf)
    .maxDuration(600 seconds)

}
