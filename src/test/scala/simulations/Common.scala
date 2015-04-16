package simulations

import io.gatling.core.Predef._
import io.gatling.http.Predef._


object Common {

  val httpConf = http
    // .proxy(Proxy("127.0.0.1", 8888).httpsPort(8888))
    .baseURL("https://workshop.uxguards.com/whipping-boy/api/albums")
    .disableCaching

  val commonHeaders = Map(
    "User-Agent" -> "gatling/simulation",
    HttpHeaderNames.Accept -> HttpHeaderValues.ApplicationJson
  )


  def testDurationSec() = sys.props.get("durationSec").getOrElse("10").toDouble
  def maxUsers():Int = sys.props.get("maxUsers").getOrElse("1").toInt
  def minUsers():Int = sys.props.get("minUsers").getOrElse("1").toInt
  def rampDurationSec() = sys.props.get("rampDurationSec").getOrElse(testDurationSec().toString).toDouble
}
