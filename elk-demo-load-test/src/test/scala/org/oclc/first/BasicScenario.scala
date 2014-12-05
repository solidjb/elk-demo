package org.oclc.first 

import io.gatling.core.Predef._
import io.gatling.core.structure.ScenarioBuilder
import io.gatling.http.config.HttpProtocolBuilder
import scala.concurrent.duration._


import io.gatling.http.Predef._

class BasicScenario extends Simulation {

  private def createHttpConf(port:Int) : HttpProtocolBuilder = {
      http 
        .baseURL("http://localhost:" + port) 
        .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") 
        .doNotTrackHeader("1")
        .acceptLanguageHeader("en-US,en;q=0.5")
        .acceptEncodingHeader("gzip, deflate")
        .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")
        .header("X-DEMO-ClientId", "Gatling_Load_Tests")
  }

  object Health {

    val health = exec(http("health_check")
      .get("/health"))
  }

  private def createScenario(suffix : String) : ScenarioBuilder = {
    scenario("BasicScenario" + suffix).exec(Health.health)
  }

  val httpConfA = createHttpConf(8081)
  val httpConfB = createHttpConf(8082)
  val httpConfC = createHttpConf(8083)
  val httpConfD = createHttpConf(8084)
  val httpConfE = createHttpConf(8085)

  val scnA = createScenario("A")
  val scnB = createScenario("B")
  val scnC = createScenario("C")
  val scnD = createScenario("D")
  val scnE = createScenario("E")

  setUp( 
    scnA.inject(rampUsers(100) over (15 minutes)).protocols(httpConfA),
    scnB.inject(rampUsers(110) over (15 minutes)).protocols(httpConfB),
    scnC.inject(rampUsers(90) over (15 minutes)).protocols(httpConfC),
    scnD.inject(rampUsers(105) over (15 minutes)).protocols(httpConfD),
    scnE.inject(rampUsers(95) over (15 minutes)).protocols(httpConfE)
  )
}