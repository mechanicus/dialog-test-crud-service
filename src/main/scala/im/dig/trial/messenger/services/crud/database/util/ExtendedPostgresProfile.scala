package im.dig.trial.messenger.services.crud.database.util

import cats.Show
import cats.syntax.show._
import com.github.tminglei.slickpg._
import im.dig.trial.messenger.services.model.{Read, SHA256}
import im.dig.trial.messenger.services.model.ReadSyntax._

import scala.reflect.ClassTag

trait CustomColumnTypes {
  import slick.jdbc.PostgresProfile.api._

  // SHA-256 хэши хранятся в БД как массивы байтов
  implicit val sha256ColumnType: ColumnType[SHA256] =
    MappedColumnType.base[SHA256, Array[Byte]](_.bytes, SHA256.apply)

  // автоматический маппинг типов изоморфных строке
  implicit def stringColumnMapping[A : Read : Show : ClassTag]: ColumnType[A] =
    MappedColumnType.base[A, String](a => a.show, s => s.read[A])

}

trait ExtendedPostgresProfile
  extends ExPostgresProfile
     with PgDate2Support
{
  override val api: ExtendedApi.type = ExtendedApi
  object ExtendedApi
    extends API
       with DateTimeImplicits
       with CustomColumnTypes
}

object ExtendedPostgresProfile extends ExtendedPostgresProfile
