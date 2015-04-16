package simulations.workshop

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import io.gatling.core.scenario.Simulation
import scala.concurrent.duration._
import simulations.Common._

class WorkshopSimulation extends Simulation with AlbumsRequests {

  val singleUserScenario = scenario("Single user")
    .forever {
      pause(1)
      .exec(http("discovery").options("/").headers(commonHeaders))
  }

  
  setUp(
    createAlbumScenario.inject(
      rampUsers(maxUsers()) over (rampDurationSec() seconds)
    )
  ).protocols(httpConf)
    .maxDuration(testDurationSec() seconds)

}
