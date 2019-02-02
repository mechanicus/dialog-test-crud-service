package im.dig.trial.messenger.services.crud.database

import java.time.LocalDateTime

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future

object ChatMessages {

  def create(chatId: HashId, senderId: UserId, messageId: HashId, sentOn: LocalDateTime)(implicit db: Database): Future[Int] =
    db.run(ChatMessageRepository.create(chatId, senderId, messageId, sentOn))

  def delete(chatId: HashId, messageId: HashId)(implicit db: Database): Future[Int] =
    db.run(ChatMessageRepository.delete(chatId, messageId))

  def getUpdates(chatIds: Set[ChatId], from: LocalDateTime)(implicit db: Database): Future[Seq[ChatMessage]] =
    db.run(ChatMessageRepository.getUpdates(chatIds, from))

  def getHistory(chatId: ChatId, to: LocalDateTime, limit: Int)(implicit db: Database): Future[Seq[ChatMessage]] =
    db.run(ChatMessageRepository.getHistory(chatId, to, limit))

}

object ChatMessageRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "chat_message"

  val chatMessages: TableQuery[ChatMessageTable] = TableQuery[ChatMessageTable]

  override protected val prepareTable: DBIO[Unit] =
    chatMessages.schema.create

  def create(chatId: HashId, senderId: UserId, messageId: HashId, sentOn: LocalDateTime): DBIO[Int] =
    chatMessages += ChatMessage(chatId, senderId, messageId, sentOn)

  def delete(chatId: HashId, messageId: HashId): DBIO[Int] =
    chatMessages.filter(cm => cm.chatId === chatId && cm.messageId === messageId).delete

  def getUpdates(chatIds: Set[ChatId], from: LocalDateTime): DBIO[Seq[ChatMessage]] =
    chatMessages.filter { cm => (cm.chatId inSet chatIds) && cm.sentOn >= from }.result

  def getHistory(chatId: ChatId, to: LocalDateTime, limit: Int): DBIO[Seq[ChatMessage]] =
    chatMessages.filter { cm => cm.chatId === chatId && cm.sentOn <= to }.take(limit).result


  final class ChatMessageTable(tag: Tag) extends Table[ChatMessage](tag, schemaName, tableName) {
    import ChatRepository.chats
    import UserRepository.users
    import MessageRepository.messages

    def chatId = column[HashId]("chat_id")
    def senderId = column[UserId]("sender_id")
    def messageId = column[HashId]("message_id")
    def sentOn = column[LocalDateTime]("sent_on")

    def pk = primaryKey(s"${tableName}_pkey", (chatId, senderId, messageId))
    def chatIdFK = foreignKey(s"${tableName}_chat_id_fkey", chatId, chats)(_.chatId)
    def senderIdFK = foreignKey(s"${tableName}_sender_id_fkey", senderId, users)(_.userId)
    def messageIdFK = foreignKey(s"${tableName}_message_id_fkey", messageId, messages)(_.messageId)

    def sentOnIndex = index(s"${tableName}_sent_on_idx", sentOn, unique = false)

    override def * = (chatId, senderId, messageId, sentOn).mapTo[ChatMessage]

  }

}
