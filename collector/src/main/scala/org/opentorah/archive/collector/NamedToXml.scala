package org.opentorah.archive.collector

import org.opentorah.archive.collector.selectors.{CollectionSelector, NameSelector, NamesSelector}
import org.opentorah.metadata.Language
import org.opentorah.reference.{Name, Named, NamesList, Reference}
import org.opentorah.util.Collections

import scala.xml.Elem

object NamedToXml {

  // TODO collapse into the underlying - but provide a way to make id undefined...
  def toXml(value: Named): Elem =
    <named role={value.role.orNull}>
      {for (name <- value.names) yield Name.toXml(name)}
      {value.content}
    </named>
      .copy(label = value.entity.element)

  def toXml(
    value: NamesList,
    namedUrl: String => String
  ): Elem =
    <list xml:id={value.id} role={value.role.orNull}>
      <head>{value.head}</head>
      {for (named <- value.nameds) yield toListXml(named, namedUrl)}
    </list>
      .copy(label = value.entity.listElement)

  // TODO unfold
  def toListXml(
    value: Named,
    namedUrl: String => String
  ): Elem = {
    val url: String = namedUrl(value.path.last.selectedName(Language.English.toSpec)) //namedUrl(id)
    <l><ref target={url} role="namesViewer">{Name.toXml(value.names.head)}</ref></l>
  }

  def toXml(
    value: Named,
    references: Seq[Reference],
    namedUrl: String => String,
    namedInTheListUrl: String => String,
    documentUrl: (String, String) => String
  ): Elem = {
    def sources(viewer: String, references: Seq[Reference]): Seq[Elem] =
      for (source <- Collections.removeConsecutiveDuplicates(references.map(_.source))) yield {
        val name = source.last.selectedName(Language.Russian.toSpec)
        val collectionLike = source.init.last

        val url =
          if (collectionLike.getSelector == NamesSelector)
            namedUrl(source.last.selectedName(Language.English.toSpec))
          else if (collectionLike.getSelector == CollectionSelector)
            documentUrl(
              collectionLike.selectedName(Language.English.toSpec),
              source.last.selectedName(Language.English.toSpec))
          else throw new IllegalArgumentException(s"Wrong selector: $collectionLike")

        <ref target={url} role={viewer}>{name}</ref>
      }

    //    // TODO move into Reference
    //    def name(reference: Reference): String = reference.name
    //      .map(_.text.replace("\n", "").trim
    //        .replace("  ", " ")
    //        .replace("  ", " ")
    //        .replace("  ", " ")
    //        .replace("  ", " ")
    //      )
    //      .filterNot(_.isEmpty).mkString(" ")

    def isFromNames(reference: Reference): Boolean =
      reference.source.last.getSelector == NameSelector

    val usedBy: Seq[Reference] = references.filter(_.ref.contains(value.id.get))
    val fromNames: Seq[Reference] = usedBy.filter(isFromNames)
    val bySource: Seq[(String, Seq[Reference])] = usedBy.filterNot(isFromNames)
      .groupBy(_.source.init.reference(Language.Russian.toSpec)).toSeq.sortBy(_._1)

    //    val numbers: Seq[(String, Int)] =
    //      usedBy.groupBy(name).mapValues(_.length).toSeq.sortBy(_._2).reverse
    //    <p rendition="usage">
    //      {for ((name, number) <- numbers) yield <l>{s"$name ($number)"}</l>}
    //    </p>

    <named xml:id={value.id.get} role={value.role.orNull}>
      {for (name <- value.names) yield Name.toXml(name)}
      {value.content}
      <p rendition="mentions">
        <ref target={namedInTheListUrl(value.id.get)} role="namesViewer">[...]</ref>
        {if (fromNames.isEmpty) Seq.empty else
        <l>
          <emph>{fromNames.head.source.init.reference(Language.Russian.toSpec)}:</emph>
          {
          val result = sources("namesViewer", fromNames)
          result.init.map(elem => <span>{elem},</span>) :+ result.last
          }
        </l>}
        {for ((source, references) <- bySource) yield <l><emph>{source}:</emph>{sources("documentViewer", references)}</l>}
      </p>
    </named>
      .copy(label = value.entity.element)
  }
}
