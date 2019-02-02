package im.dig.trial.messenger.services.crud.database

import java.time.LocalDateTime

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future


object PersonalFiles {

  def create(receiverId: HashId, senderId: UserId, fileId: HashId, sentOn: LocalDateTime)(implicit db: Database): Future[Int] =
    db.run(PersonalFileRepository.create(receiverId, senderId, fileId, sentOn))

  def getUpdates(userId: UserId, from: LocalDateTime)(implicit db: Database): Future[Seq[PersonalFile]] =
    db.run(PersonalFileRepository.getUpdates(userId, from))

  def getHistory(user1: UserId, user2: UserId, to: LocalDateTime, limit: Int)(implicit db: Database): Future[Seq[PersonalFile]] =
    db.run(PersonalFileRepository.getHistory(user1, user2, to, limit))

}

object PersonalFileRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "personal_file"

  val personalFiles: TableQuery[PersonalFileTable] = TableQuery[PersonalFileTable]

  override protected def prepareTable: DBIO[Unit] =
    personalFiles.schema.create

  def create(receiverId: HashId, senderId: UserId, fileId: HashId, sentOn: LocalDateTime): DBIO[Int] =
    personalFiles += PersonalFile(receiverId, senderId, fileId, sentOn)

  def getUpdates(userId: UserId, from: LocalDateTime): DBIO[Seq[PersonalFile]] =
    personalFiles.filter { pf =>
      (pf.receiverId === userId || pf.senderId === userId) && pf.sentOn >= from
    }.result

  def getHistory(user1: UserId, user2: UserId, to: LocalDateTime, limit: Int): DBIO[Seq[PersonalFile]] =
    personalFiles.filter { pf => (
        (pf.senderId === user1 && pf.receiverId === user2) ||
        (pf.senderId === user2 && pf.receiverId === user1)
      ) && pf.sentOn <= to
    }.take(limit)
    .result


  final class PersonalFileTable(tag: Tag) extends Table[PersonalFile](tag, schemaName, tableName) {
    import FileRepository.files
    import UserRepository.users

    def receiverId = column[HashId]("receiver_id")
    def senderId = column[UserId]("sender_id")
    def fileId = column[HashId]("file_id")
    def sentOn = column[LocalDateTime]("sent_on")

    def pk = primaryKey(s"${tableName}_pkey", (receiverId, senderId, fileId))
    def receiverIdFK = foreignKey(s"${tableName}_receiver_id_fkey", receiverId, users)(_.userId)
    def senderIdFK = foreignKey(s"${tableName}_sender_id_fkey", senderId, users)(_.userId)
    def fileIdFK = foreignKey(s"${tableName}_file_id_fkey", fileId, files)(_.fileId)

    def senderIdIndex = index(s"${tableName}_sender_id_idx", senderId, unique = false)
    def sentOnIndex = index(s"${tableName}_sent_on_idx", sentOn, unique = false)

    override def * = (receiverId, senderId, fileId, sentOn).mapTo[PersonalFile]

  }

}
