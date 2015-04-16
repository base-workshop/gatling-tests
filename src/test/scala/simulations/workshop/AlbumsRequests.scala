package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._

trait AlbumsRequests {
  self: Simulation =>
  val allAlbums = exec(http("allAlbums").get("/"))

  val allAlbumsScenario = scenario("get all albums").exec(allAlbums)
  
  val createAlbum = exec(
    http("addArtist")
      .post("/artists").body(StringBody("""{ "name": "test-artits2" }"""))
      .asJSON
      .check(jsonPath("$.id").saveAs("id"), jsonPath("$.name").saveAs("name")))
      .exec(http("addAlbum")
    .post("/albums").body(ELFileBody("album-template.json")).asJSON)
  .exec(http("getAlbums").get("/albums"))
  
  val createAlbumScenario = scenario("create album").exec(createAlbum)
}
