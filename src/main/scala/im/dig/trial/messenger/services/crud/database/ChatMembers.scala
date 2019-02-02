package im.dig.trial.messenger.services.crud.database

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future


object ChatMembers {

  def create(chatId: HashId, userId: HashId)(implicit db: Database): Future[Int] =
    db.run(ChatMemberRepository.create(chatId, userId))

  def delete(chatId: HashId, userId: HashId)(implicit db: Database): Future[Int] =
    db.run(ChatMemberRepository.delete(chatId, userId))

  def getChatMembers(chatId: HashId)(implicit db: Database): Future[Seq[ChatMember]] =
    db.run(ChatMemberRepository.getChatMembers(chatId))

  def getUserChats(userId: UserId)(implicit db: Database): Future[Seq[ChatId]] =
    db.run(ChatMemberRepository.getUserChats(userId))

}


object ChatMemberRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "chat_member"

  val chatMembers: TableQuery[ChatMemberTable] = TableQuery[ChatMemberTable]

  override protected val prepareTable: DBIO[Unit] =
    chatMembers.schema.create

  def create(chatId: HashId, userId: HashId): DBIO[Int] =
    chatMembers += ChatMember(chatId, userId)

  def delete(chatId: HashId, userId: HashId): DBIO[Int] =
    chatMembers.filter(cm => cm.chatId === chatId && cm.userId === userId).delete

  def getChatMembers(chatId: HashId): DBIO[Seq[ChatMember]] =
    chatMembers.filter(_.chatId === chatId).result

  def getUserChats(userId: UserId): DBIO[Seq[ChatId]] =
    chatMembers.filter { c => c.userId === userId }.map { _.chatId }.result


  final class ChatMemberTable(tag: Tag) extends Table[ChatMember](tag, schemaName, tableName) {
    import ChatRepository.chats
    import UserRepository.users

    def chatId = column[HashId]("chat_id")
    def userId = column[HashId]("user_id")

    def pk = primaryKey(s"${tableName}_pkey", (chatId, userId))
    def chatIdFK = foreignKey(s"${tableName}_chat_id_fkey", chatId, chats)(_.chatId)
    def userIdFK = foreignKey(s"${tableName}_user_id_fkey", userId, users)(_.userId)

    def userIdIndex = index(s"${tableName}_user_id_idx", userId, unique = false)

    override def * = (chatId, userId).mapTo[ChatMember]

  }

}
