package im.dig.trial.messenger.services.crud.database

import java.time.LocalDateTime

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future

object ChatFiles {

  def create(chatId: ChatId, senderId: UserId, fileId: FileId, sentOn: LocalDateTime)(implicit db: Database): Future[Int] =
    db.run(ChatFileRepository.create(chatId, senderId, fileId, sentOn))

  def delete(chatId: ChatId, fileId: FileId)(implicit db: Database): Future[Int] =
    db.run(ChatFileRepository.delete(chatId, fileId))

  // получить новые файлы для чатов из `chatIds`, начиная с момента времени `from`
  def getUpdates(chatIds: Set[ChatId], from: LocalDateTime)(implicit db: Database): Future[Seq[ChatFile]] =
    db.run(ChatFileRepository.getUpdates(chatIds, from))

  // получить `limit` файлов чата `chatId` до момента времени `to`
  def getHistory(chatId: ChatId, to: LocalDateTime, limit: Int)(implicit db: Database): Future[Seq[ChatFile]] =
    db.run(ChatFileRepository.getHistory(chatId, to, limit))

}

object ChatFileRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "chat_file"

  val chatFiles: TableQuery[ChatFileTable] = TableQuery[ChatFileTable]

  override protected val prepareTable: DBIO[Unit] =
    chatFiles.schema.create

  def create(chatId: ChatId, senderId: UserId, fileId: FileId, sentOn: LocalDateTime): DBIO[Int] =
    chatFiles += ChatFile(chatId, senderId, fileId, sentOn)

  def delete(chatId: ChatId, fileId: FileId): DBIO[Int] =
    chatFiles.filter(cf => cf.chatId === chatId && cf.fileId === fileId).delete

  def getUpdates(chatIds: Set[ChatId], from: LocalDateTime): DBIO[Seq[ChatFile]] =
    chatFiles.filter { cf => (cf.chatId inSet chatIds) && cf.sentOn >= from }.result

  def getHistory(chatId: ChatId, to: LocalDateTime, limit: Int): DBIO[Seq[ChatFile]] =
    chatFiles.filter { cf => cf.chatId === chatId && cf.sentOn <= to }.take(limit).result


  final class ChatFileTable(tag: Tag) extends Table[ChatFile](tag, schemaName, tableName) {
    import ChatRepository.chats
    import UserRepository.users
    import FileRepository.files

    def chatId = column[ChatId]("chat_id")
    def senderId = column[UserId]("sender_id")
    def fileId = column[FileId]("file_id")
    def sentOn = column[LocalDateTime]("sent_on")

    def pk = primaryKey(s"${tableName}_pkey", (chatId, fileId))
    def chatIdFK = foreignKey(s"${tableName}_chat_id_fkey", chatId, chats)(_.chatId)
    def senderIdFK = foreignKey(s"${tableName}_sender_id_fkey", senderId, users)(_.userId)
    def fileIdFK = foreignKey(s"${tableName}_file_id_fkey", fileId, files)(_.fileId)

    // для быстрого поиска обновлений и истории
    def sentOnIndex = index(s"${tableName}_sent_on_idx", sentOn, unique = false)

    override def * = (chatId, senderId, fileId, sentOn).mapTo[ChatFile]

  }

}
