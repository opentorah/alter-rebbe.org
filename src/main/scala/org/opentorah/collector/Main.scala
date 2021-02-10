package org.opentorah.collector

import org.slf4j.{Logger, LoggerFactory}
import java.io.File
import java.net.URL

object Main {

  private val logger: Logger = LoggerFactory.getLogger(this.getClass)
  private def info(message: String): Unit = logger.info(message)

  def main(args: Array[String]): Unit = if (args.isEmpty) Service.main(Array.empty) else {
    val siteRootPath: String = args(1)

    args(0) match {
      case "serve" =>
        Service.main(Array(siteRootPath))

      case "build" =>
        val baseUrl: URL = new File(siteRootPath).toURI.toURL

        info("Reading site.")
        val site: Site = Site.read(baseUrl)

        info("Writing site lists.")
        site.writeLists()

        info("Pretty-printing site.")
        site.prettyPrint()

        info("Verifying site.")
        site.verify()

      case "upload" =>
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
}
