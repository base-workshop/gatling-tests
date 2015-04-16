package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._

trait AlbumsRequests {
  self: Simulation =>
  val allAlbums = exec(http("allAlbums").get("/"))

  val allAlbumsScenario = scenario("get all albums").exec(allAlbums)
}
