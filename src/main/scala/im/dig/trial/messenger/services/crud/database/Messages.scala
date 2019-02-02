package im.dig.trial.messenger.services.crud.database

import java.time.LocalDateTime

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future

object Messages {

  def create(messageId: HashId, ownerId: HashId, content: NonEmptyString, createdOn: LocalDateTime)(implicit db: Database): Future[Int] =
    db.run(MessageRepository.create(messageId, ownerId, content, createdOn))

  def read(messageId: HashId)(implicit db: Database): Future[Option[Message]] =
    db.run(MessageRepository.read(messageId))

  def read(messageIds: Set[HashId])(implicit db: Database): Future[Seq[Message]] =
    db.run(MessageRepository.read(messageIds))

  def update(messageId: HashId)(content: NonEmptyString)(implicit db: Database): Future[Int] =
    db.run(MessageRepository.update(messageId)(content))

  def delete(messageId: HashId)(implicit db: Database): Future[Int] =
    db.run(MessageRepository.delete(messageId))

}

object MessageRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "message"

  val messages: TableQuery[MessageTable] = TableQuery[MessageTable]

  override protected def prepareTable: DBIO[Unit] =
    messages.schema.create

  def create(messageId: HashId, ownerId: HashId, content: NonEmptyString, createdOn: LocalDateTime): DBIO[Int] =
    messages += Message(messageId, ownerId, content, createdOn)

  def read(messageId: HashId): DBIO[Option[Message]] =
    messages.filter(_.messageId === messageId).result.headOption

  def read(messageIds: Set[HashId]): DBIO[Seq[Message]] =
    messages.filter(_.messageId inSet messageIds).result

  def update(messageId: HashId)(content: NonEmptyString): DBIO[Int] =
    messages.filter(_.messageId === messageId).map(_.content).update(content)

  def delete(messageId: HashId): DBIO[Int] =
    messages.filter(_.messageId === messageId).delete


  final class MessageTable(tag: Tag) extends Table[Message](tag, schemaName, tableName) {

    def messageId = column[HashId]("message_id", O.PrimaryKey)
    def ownerId = column[HashId]("owner_id")
    def content = column[NonEmptyString]("content", O.SqlType("text"))
    def createdOn = column[LocalDateTime]("created_on")

    def ownerIdForeignKey = foreignKey(s"${tableName}_owner_id_fkey", ownerId, UserRepository.users)(_.userId)

    override def * = (messageId, ownerId, content, createdOn).mapTo[Message]

  }

}
