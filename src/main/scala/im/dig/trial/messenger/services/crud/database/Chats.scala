package im.dig.trial.messenger.services.crud.database

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future


object Chats {

  def create(chatId: HashId, title: NonEmptyString)(implicit db: Database): Future[Int] =
    db.run(ChatRepository.create(chatId, title))

  def read(chatId: HashId)(implicit db: Database): Future[Option[Chat]] =
    db.run(ChatRepository.read(chatId))

  def update(chatId: HashId, title: NonEmptyString)(implicit db: Database): Future[Int] =
    db.run(ChatRepository.update(chatId, title))

  def delete(chatId: HashId)(implicit db: Database): Future[Int] =
    db.run(ChatRepository.delete(chatId))

}


object ChatRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "chat"

  val chats: TableQuery[ChatTable] = TableQuery[ChatTable]

  override protected val prepareTable: DBIO[Unit] =
    chats.schema.create

  def create(chatId: HashId, title: NonEmptyString): DBIO[Int] =
    chats += Chat(chatId, title)

  def read(chatId: HashId): DBIO[Option[Chat]] =
    chats.filter(_.chatId === chatId).result.headOption

  def update(chatId: HashId, title: NonEmptyString): DBIO[Int] =
    chats.filter(_.chatId === chatId).map(_.title).update(title)

  def delete(chatId: HashId): DBIO[Int] =
    chats.filter(_.chatId === chatId).delete


  final class ChatTable(tag: Tag) extends Table[Chat](tag, schemaName, tableName) {
    def chatId = column[HashId]("chat_id", O.PrimaryKey)
    def title = column[NonEmptyString]("title", O.SqlType("text"))
    override def * = (chatId, title).mapTo[Chat]
  }

}
