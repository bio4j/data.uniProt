package bio4j.data.uniprot

import java.time.LocalDate

case class FlatFileEntry(
  val allLines: Array[String]
)
extends AnyEntry {

  private def linesOfType(lt: LineType) =
    (allLines filter Line.isOfType(lt)).map(Line.contentOf)

  private lazy val id: lines.ID =
    lines.ID(linesOfType(ID).head)

  lazy val identification: Identification =
    Identification(
      entryName = id.id,
      status    = id.status,
      length    = id.length
    )

  private lazy val ac: lines.AC =
    lines.AC(linesOfType(AC))

  lazy val accessionNumbers: AccessionNumber = {

    lazy val accesions =
      ac.accesions

    AccessionNumber(
      primary   = accesions.head,
      secondary = accesions.tail
    )
  }

  private lazy val dt: lines.DT =
    lines.DT(linesOfType(DT))

  lazy val date: Date =
    Date(
      creation              = dt.creation,
      sequenceLastModified  = dt.sequenceLastModified,
      entryLastModified     = dt.entryLastModified
    )

  private lazy val de: lines.DE =
    lines.DE(linesOfType(DE))

  lazy val description: Description =
    Description(
      recommendedName   = de.recommendedName,
      alternativeNames  = de.alternativeNames,
      submittedNames    = de.submittedNames
    )

  private lazy val gn: lines.GN =
    lines.GN(linesOfType(GN))

  lazy val geneNames: Seq[GeneName] =
    gn.geneNames

  lazy val organismSpecies: OrganismSpecies = ???

  private lazy val og: lines.OG =
    lines.OG(linesOfType(GN))

  lazy val organelles: Seq[Organelle] =
    og.organelles

  lazy val organismClassification: OrganismClassification = ???

  private lazy val ox: lines.OX =
    lines.OX(linesOfType(OX).head)

  lazy val taxonomyCrossReference: TaxonomyCrossReference =
    ox.taxonomyCrossReference

  private lazy val oh: lines.OH =
    lines.OH(linesOfType(OH))

  lazy val organismHost: Seq[TaxonomyCrossReference] =
    oh.taxonomyCrossReferences

  private lazy val cc: lines.CC =
    lines.CC(linesOfType(CC))

  lazy val comments: Seq[Comment] =
    cc.comments

  private lazy val dr: lines.DR =
    lines.DR(linesOfType(DR))

  lazy val databaseCrossReferences: Seq[DatabaseCrossReference] =
    dr.databaseCrossReferences

  private lazy val pe: lines.PE =
    lines.PE(linesOfType(PE).head)
    
  lazy val proteinExistence: ProteinExistence =
    pe.proteinExistence

  lazy val keywords: Seq[Keyword] = ???

  lazy val features: Seq[Feature] = ???

  lazy val sequenceHeader: SequenceHeader = ???

  lazy val sequence: Sequence = ???
}

case object FlatFileEntry {

  def from(lns: Seq[String]): FlatFileEntry =
    FlatFileEntry(lns.toArray)
}
