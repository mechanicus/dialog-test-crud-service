package im.dig.trial.messenger.services.crud.database

import java.time.LocalDateTime

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future


object PersonalMessages {

  def create(receiverId: UserId, senderId: UserId, messageId: MessageId, sentOn: LocalDateTime)(implicit db: Database): Future[Int] =
    db.run(PersonalMessageRepository.create(receiverId, senderId, messageId, sentOn))

  def getUpdates(userId: UserId, from: LocalDateTime)(implicit db: Database): Future[Seq[PersonalMessage]] =
    db.run(PersonalMessageRepository.getUpdates(userId, from))

  def getHistory(user1: UserId, user2: UserId, to: LocalDateTime, limit: Int)(implicit db: Database): Future[Seq[PersonalMessage]] =
    db.run(PersonalMessageRepository.getHistory(user1, user2, to, limit))

}

object PersonalMessageRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "personal_message"

  val personalMessages: TableQuery[PersonalMessageTable] = TableQuery[PersonalMessageTable]

  override protected def prepareTable: DBIO[Unit] =
    personalMessages.schema.create

  def create(receiverId: UserId, senderId: UserId, messageId: MessageId, sentOn: LocalDateTime): DBIO[Int] =
    personalMessages += PersonalMessage(receiverId, senderId, messageId, sentOn)

  def getUpdates(userId: UserId, from: LocalDateTime): DBIO[Seq[PersonalMessage]] =
    personalMessages
      .filter { pm =>
        (pm.receiverId === userId || pm.senderId === userId) && pm.sentOn >= from
      }.result

  def getHistory(user1: UserId, user2: UserId, to: LocalDateTime, limit: Int): DBIO[Seq[PersonalMessage]] =
    personalMessages
      .filter { pm => (
          (pm.senderId === user1 && pm.receiverId === user2) ||
          (pm.senderId === user2 && pm.receiverId === user1)
        ) && pm.sentOn <= to
      }.take(limit)
      .result

  final class PersonalMessageTable(tag: Tag) extends Table[PersonalMessage](tag, schemaName, tableName) {
    import MessageRepository.messages
    import UserRepository.users

    def receiverId = column[UserId]("receiver_id")
    def senderId = column[UserId]("sender_id")
    def messageId = column[MessageId]("message_id")
    def sentOn = column[LocalDateTime]("sent_on")

    def pk = primaryKey(s"${tableName}_pkey", (receiverId, senderId, messageId))
    def receiverIdFK = foreignKey(s"${tableName}_receiver_id_fkey", receiverId, users)(_.userId)
    def senderIdFK = foreignKey(s"${tableName}_sender_id_fkey", senderId, users)(_.userId)
    def messageIdFK = foreignKey(s"${tableName}_message_id_fkey", messageId, messages)(_.messageId)

    def senderIdIndex = index(s"${tableName}_sender_id_idx", senderId, unique = false)
    def sentOnIndex = index(s"${tableName}_sent_on_idx", sentOn, unique = false)

    override def * = (receiverId, senderId, messageId, sentOn).mapTo[PersonalMessage]

  }

}
