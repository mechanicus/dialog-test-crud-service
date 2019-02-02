package im.dig.trial.messenger.services.crud.database

import java.time.LocalDateTime

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future

object Files {

  def create(fileId: FileId, ownerId: UserId, originalName: Filename, uploadedOn: LocalDateTime)(implicit db: Database): Future[Int] =
    db.run(FileRepository.create(fileId, ownerId, originalName, uploadedOn))

  def read(fileId: FileId)(implicit db: Database): Future[Option[File]] =
    db.run(FileRepository.read(fileId))

  def read(fileIds: Set[FileId])(implicit db: Database): Future[Seq[File]] =
    db.run(FileRepository.read(fileIds))

  def delete(fileId: FileId)(implicit db: Database): Future[Int] =
    db.run(FileRepository.delete(fileId))

}

object FileRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "file"

  val files: TableQuery[FileTable] = TableQuery[FileTable]

  override protected def prepareTable: DBIO[Unit] =
    files.schema.create

  def create(fileId: FileId, ownerId: UserId, originalName: Filename, uploadedOn: LocalDateTime): DBIO[Int] =
    files += File(fileId, ownerId, originalName, uploadedOn)

  def read(fileId: FileId): DBIO[Option[File]] =
    files.filter(_.fileId === fileId).result.headOption

  def read(fileIds: Set[FileId]): DBIO[Seq[File]] =
    files.filter(_.fileId inSet fileIds).result

  def delete(fileId: FileId): DBIO[Int] =
    files.filter(_.fileId === fileId).delete


  final class FileTable(tag: Tag) extends Table[File](tag, schemaName, tableName) {

    def fileId = column[FileId]("file_id", O.PrimaryKey)
    def ownerId = column[UserId]("owner_id")
    def originalName = column[Filename]("original_name", O.SqlType("text"))
    def uploadedOn = column[LocalDateTime]("uploaded_on")

    def ownerIdForeignKey = foreignKey("file_owner_id_fkey", ownerId, UserRepository.users)(_.userId)

    override def * = (fileId, ownerId, originalName, uploadedOn).mapTo[File]

  }

}
