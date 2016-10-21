package bio4j.data.uniprot

import java.time.LocalDate


case class FlatFileEntry(
  val ID_line         : Line,
  val AC_lines        : Seq[Line], // nonEmpty
  val DT_lines        : Seq[Line], // 3 lines
  val DE_lines        : Seq[Line], // ?
  val GN_lines        : Seq[Line], // ?
  val OS_lines        : Seq[Line], //
  val OG_lines        : Seq[Line],
  val OX_lines        : Seq[Line],
  val OH_lines        : Seq[Line],
  val CC_lines        : Seq[Line],
  val DR_lines        : Seq[Line],
  val PE_lines        : Seq[Line],
  val KW_lines        : Seq[Line],
  val FT_lines        : Seq[Line],
  val SQ_lines        : Seq[Line],
  val sequence_lines  : Seq[String]
)
extends AnyEntry {

  lazy val identification: Identification = {

    val id =
      ID_line.content.takeWhile(_ != ' ')

    val statusRep =
      ID_line.content
        .drop(24) // magic number!
        .takeWhile(_ != ';')

    val _status: Status =
      if(statusRep == Reviewed.asString) Reviewed else Unreviewed

    val _length =
      ID_line.content
        .reverse
        .drop(4) // remove ".AA "
        .takeWhile(_ != ' ')
        .reverse
        .toInt

    Identification(
      entryName = id,
      status    = _status,
      length    = _length
    )
  }

  lazy val accessionNumbers: AccessionNumber = {

    val allIDs =
      AC_lines
        .map(_.content).mkString("")  // join all lines
        .split(';').map(_.trim)       // split and trim values

      AccessionNumber(
        primary   = allIDs.head,
        secondary = allIDs.tail
      )
  }

  lazy val date: Date = {

    val dates =
      DT_lines map { l =>
        parsers.localDateFrom( l.content.takeWhile(_ != ',') )
      }

    val linesWithVersions: Seq[Line] = (DT_lines drop 1)

    val versions: Seq[Int] =
      linesWithVersions.map { line =>
        line.content
          .reverse
          .drop(1)
          .takeWhile(_ != ' ')
          .reverse
          .toInt
      }

    Date(
      dates(0),
      sequenceLastModified  = VersionedDate(dates(1), versions(0)),
      entryLastModified     = VersionedDate(dates(2), versions(1))
    )
  }

  lazy val description: Description = {

    // if there's a recommended name, the content starts with 'RecName:'; otherwhise there's none
    // note that there's *always* a DE line, we just don't know what it is
    // both values here cannot be empty
    val (recNameLines, rest) = DE_lines.span(_.content.startsWith("RecName:"))
    val recNameLine = recNameLines.headOption

    val fullNameOpt =
      recNameLine map { l =>

        val z =
          l.content
            .trim
            .stripPrefix("RecName: Full=")
            .stripSuffix(";")

        // they sometimes have some funny '{ECO:0000255|HAMAP-Rule:MF_01588}' stuff at the end
        if(z.endsWith("}"))
          z.reverse.dropWhile(_ != '{').stripPrefix("{").reverse.trim
        else
          z
      }

    val (shortNames: Seq[String], ecNames: Seq[String]) = recNameLine.fold[(Seq[String], Seq[String])](( Seq(),Seq() ))(l => {

      val restRecNameLinesTrimmed: Seq[String] =
        rest
          .map(_.content)
          .takeWhile(_.startsWith("        "))
          .map(_.trim)

      (
        restRecNameLinesTrimmed.filter(_.trim.startsWith("Short="))
          .map(_.stripPrefix("Short=").stripSuffix(";"))
        ,
        restRecNameLinesTrimmed.filter(_.trim.startsWith("EC="))
          .map(_.stripPrefix("EC=").stripSuffix(";"))
      )
    }
    )

    val ret1 = fullNameOpt.map { fn =>
      RecommendedName(
        full  = fn,
        short = shortNames,
        ec    = ecNames
      )
    }

    Description(
      recommendedName = ret1,
      alternativeNames = Seq(),
      submittedNames = Seq()
    )
  }

  lazy val geneNames: Seq[GeneName] = ???

  lazy val organismSpecies: OrganismSpecies = ???

  lazy val organelle: Option[Organelle] = ???

  lazy val organismClassification: OrganismClassification = ???

  lazy val taxonomyCrossReference: TaxonomyCrossReference = ???

  lazy val organismHost: Seq[TaxonomyCrossReference] = ???

  lazy val comments: Seq[Comment] = ???

  lazy val databaseCrossReferences: Seq[DatabaseCrossReference] = ???

  lazy val proteinExistence: ProteinExistence = ???

  lazy val keywords: Seq[Keyword] = ???

  lazy val features: Seq[Feature] = ???

  lazy val sequenceHeader: SequenceHeader = ???

  lazy val sequence: Sequence = ???
}


sealed trait LineType { lazy val asString: String = toString }
  case object ID extends LineType
  case object AC extends LineType
  case object DT extends LineType
  case object DE extends LineType
  case object GN extends LineType
  case object OS extends LineType
  case object OG extends LineType
  case object OX extends LineType
  // reference lines begin
  case object RN extends LineType
  case object RP extends LineType
  case object RC extends LineType
  case object RX extends LineType
  case object RG extends LineType
  case object RA extends LineType
  case object RT extends LineType
  case object RL extends LineType
  // reference lines end
  case object OH extends LineType
  case object CC extends LineType
  case object DR extends LineType
  case object PE extends LineType
  case object KW extends LineType
  case object FT extends LineType
  case object SQ extends LineType

case object LineType {

  def fromString(rep: String): Option[LineType] =
    rep match {
      case ID.asString  => Some(ID)
      case AC.asString  => Some(AC)
      case DT.asString  => Some(DT)
      case DE.asString  => Some(DE)
      case GN.asString  => Some(GN)
      case OS.asString  => Some(OS)
      case OG.asString  => Some(OG)
      case OX.asString  => Some(OX)
      case OH.asString  => Some(OH)
      case CC.asString  => Some(CC)
      case DR.asString  => Some(DR)
      case PE.asString  => Some(PE)
      case KW.asString  => Some(KW)
      case FT.asString  => Some(FT)
      case SQ.asString  => Some(SQ)
      case _            => None
    }
}

case class Line(
  val lineType: LineType,
  val content: String
) {

  def ofType(some: LineType): Option[Line] =
    if(lineType == some) Some(this) else None

  def isOfType(some: LineType): Boolean =
    lineType == some
}

case object Line {

  def from(line: String): Option[Line] = {

    val lineTypeRep = line take 2

    (LineType fromString lineTypeRep) map { lineType =>

      val lineContent = line drop 5

      Line(lineType, lineContent)
    }
  }

  def isOfType(lt: LineType)(line: String): Boolean =
    (line take 2) == lt.asString

  def isReferenceLine(line: String): Boolean =
    isOfType(RN)(line) ||
    isOfType(RP)(line) ||
    isOfType(RC)(line) ||
    isOfType(RX)(line) ||
    isOfType(RG)(line) ||
    isOfType(RA)(line) ||
    isOfType(RT)(line) ||
    isOfType(RL)(line)


  def contentOf(line: String): String =
    line drop 5
}

case object parsers {

  def entries(lines: Iterator[String]) = new Iterator[Seq[String]] {

    private val rest: BufferedIterator[String] = lines.buffered

    def hasNext: Boolean =
      rest.hasNext

    def next(): Seq[String] =
      entry

    @annotation.tailrec
    private def entry_rec(acc: Array[String]): Array[String] =
      if (rest.hasNext) {
        if( rest.head.startsWith("//") ) {

          val drop = rest.next()
          acc
        }
        else entry_rec(acc :+ rest.next())
      }
      else acc

    private def entry: Seq[String] = entry_rec(Array())
  }

  import Line._

  // see http://stackoverflow.com/a/33521793/614394
  lazy val localDateFormatter =
    new java.time.format.DateTimeFormatterBuilder()
      .parseCaseInsensitive()
      .appendPattern("dd-MMM-yyyy")
      .toFormatter(java.util.Locale.ENGLISH)

  def localDateFrom(rep: String): LocalDate =
    LocalDate.parse(rep, localDateFormatter)

  // super ugly, but I don't see any simpler way
  def flatFileEntryFrom(allLines: Seq[String]): FlatFileEntry = {

    val (id_lines, rest0) = allLines.span(isOfType(ID))
    val (ac_lines, rest1) = rest0.span(isOfType(AC))
    val (dt_lines, rest2) = rest1.span(isOfType(DT))
    val (de_lines, rest3) = rest2.span(isOfType(DE))
    val (gn_lines, rest4) = rest3.span(isOfType(GN))
    val (os_lines, rest5) = rest4.span(isOfType(OS))
    val (og_lines, rest6) = rest5.span(isOfType(OG))
    val (ox_lines, rest7) = rest6.span(isOfType(OX))
    val (oh_lines, rest8) = rest7.span(isOfType(OH))
    // drop ref lines
    val (cc_lines, rest9) = rest8.dropWhile(isReferenceLine).span(isOfType(CC))
    val (dr_lines, rest10) = rest9.span(isOfType(DR))
    val (pe_lines, rest11) = rest10.span(isOfType(PE))
    val (kw_lines, rest12) = rest11.span(isOfType(KW))
    val (ft_lines, rest13) = rest12.span(isOfType(FT))
    val (sq_lines, rest14) = rest13.span(isOfType(SQ))
    val seqLines = rest14

    FlatFileEntry(
      ID_line  = id_lines.map(l => Line( ID, contentOf(l) )).head,
      AC_lines = ac_lines.map(l => Line( AC, contentOf(l) )),
      DT_lines = dt_lines.map(l => Line( DT, contentOf(l) )),
      DE_lines = de_lines.map(l => Line( DE, contentOf(l) )),
      GN_lines = gn_lines.map(l => Line( GN, contentOf(l) )),
      OS_lines = os_lines.map(l => Line( OS, contentOf(l) )),
      OG_lines = og_lines.map(l => Line( OG, contentOf(l) )),
      OX_lines = ox_lines.map(l => Line( OX, contentOf(l) )),
      OH_lines = oh_lines.map(l => Line( OH, contentOf(l) )),
      CC_lines = cc_lines.map(l => Line( CC, contentOf(l) )),
      DR_lines = dr_lines.map(l => Line( DR, contentOf(l) )),
      PE_lines = pe_lines.map(l => Line( PE, contentOf(l) )),
      KW_lines = kw_lines.map(l => Line( KW, contentOf(l) )),
      FT_lines = ft_lines.map(l => Line( FT, contentOf(l) )),
      SQ_lines = sq_lines.map(l => Line( SQ, contentOf(l) )),
      sequence_lines = seqLines.map(l => contentOf(l))
    )
  }
}
