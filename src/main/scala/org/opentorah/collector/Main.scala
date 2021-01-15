package org.opentorah.collector

import org.slf4j.{Logger, LoggerFactory}
import java.io.File
import java.net.URL

object Main {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private def info(message: String): Unit = logger.info(message)

  def main(args: Array[String]): Unit =
    if (args.isEmpty) Service.main(Array.empty) else {
      val siteRootPath: String = args(1)

      args(0) match {
        case "serve"    => Service.main(Array(siteRootPath))
        case "verify"   => generateSite(siteRootPath, doPrettyPrint = true , doWrite = false)
        case "generate" => generateSite(siteRootPath, doPrettyPrint = false, doWrite = true )
        case "build"    => generateSite(siteRootPath, doPrettyPrint = true , doWrite = true )
        case "upload"   =>
          GoogleCloudStorageSynchronizer.sync(
            serviceAccountKey = args(2),
            Service.bucketName,
            bucketPrefix = "",
            directoryPath = siteRootPath + "/",
            dryRun = (args.length > 3) && (args(3) == "dryRun")
          )

        case command => throw new IllegalArgumentException(s"Unrecognized command [$command]")
      }
    }

  private def generateSite(
    siteRootPath: String,
    doPrettyPrint: Boolean,
    doWrite: Boolean
  ): Unit = {
    val baseUrl: URL = new File(siteRootPath).toURI.toURL

    info("Reading store.")
    val site: Site = new Site(baseUrl)

    info("Reading New Generation Site.")
    val siteNg: org.opentorah.collectorng.Site = org.opentorah.collectorng.Site.read(baseUrl)

    info("Writing New Generation Site.")
    siteNg.writeLists()
    siteNg.verify()

    if (doPrettyPrint) {
      info("Pretty-printing store.")
      site.prettyPrintStore()
    }

    info(if (doWrite) "Verifying and writing site." else "Verifying site.")
    site.write(doWrite)
    if (doWrite) siteNg.writeStaticFiles()
  }
}
