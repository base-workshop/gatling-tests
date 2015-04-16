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

  def artistBody(name: String): String = {
    s"""{"name":"$name"}"""
  }

  def albumBody(name: String, year: Int): String = {
    s"""{"name":"$name", "year": $year, "artist_id":$${artist-id}}"""
  }

  def coverBody(url: String): String = {
    s"""{"url":"$url", "album_id":$${album-id}}"""
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
      .body(StringBody(albumBody("Best album ever", 2012)))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("album-id"))
  )

  val createCover = exec(
    http("Create Cover")
      .post("/covers")
      .headers(commonHeaders)
      .body(StringBody(coverBody("http://LOL")))
      .asJSON
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("cover-id"))
  )

  val getCovers = exec(
    http("Get covers")
      .get("/covers")
      .headers(commonHeaders)
      .check(status.is(200))
  )

  val putCover = exec(
    http("put cover")
      .put("/covers/${cover-id}")
      .headers(commonHeaders)
      .body(StringBody(coverBody("http://LOL2")))
      .asJSON
      .check(status.is(201))
  )

  val deleteCover = exec(
    http("Delete cover")
      .delete("/covers/${cover-id}")
      .headers(commonHeaders)
      .check(status.is(200))
  )


  val getByIdCover = exec(
    http("Get by id cover")
      .get("/covers/${cover-id}")
      .headers(commonHeaders)
      .check(status.is(200))
  )

  val singleCreateCoverScenario = scenario("Create cover scenario")
    .feed(data)
    .forever {
    feed(data)
      .pause(1)
      .exec(createArtist)
      .exec(createAlbum)
      .exec(createCover)
  }

  val singleGetCoversScenario = scenario("Get covers scenario")
    .feed(data)
    .forever {
    feed(data)
      .pause(1)
      .exec(getCovers)
  }

  val singlePutCoversScenario = scenario("Put cover scenario")
    .feed(data)
    .forever {
    feed(data)
      .pause(1)
      .exec(createArtist)
      .exec(createAlbum)
      .exec(createCover)
      .exec(putCover)
  }

  val singleDeleteCoversScenario = scenario("Delete cover scenario")
    .feed(data)
    .forever {
    feed(data)
      .pause(1)
      .exec(createArtist)
      .exec(createAlbum)
      .exec(createCover)
      .exec(deleteCover)
  }

  val singleGetByIdScenario = scenario("Get by id scenario")
    .feed(data)
    .forever {
    feed(data)
      .pause(1)
      .exec(createArtist)
      .exec(createAlbum)
      .exec(createCover)
      .exec(getByIdCover)
  }


  setUp(
    singleCreateCoverScenario.inject(
      rampUsers(1) over (2 seconds)
    ),
    singleGetCoversScenario.inject(
      rampUsers(1) over (2 seconds)
    ),
    singlePutCoversScenario.inject(
      rampUsers(1) over (2 seconds)
    ),
    singleDeleteCoversScenario.inject(
      rampUsers(1) over (2 seconds)
    ),
    singleGetByIdScenario.inject(
      rampUsers(1) over (2 seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
