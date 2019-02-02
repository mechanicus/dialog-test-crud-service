package im.dig.trial.messenger.services.crud.database

import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model
import im.dig.trial.messenger.services.model._

import scala.concurrent.Future


object Sessions {

  def create(sessionId: HashId, userId: HashId)(implicit db: Database): Future[Int] =
    db.run(SessionRepository.create(sessionId, userId))

  def read(sessionId: HashId)(implicit db: Database): Future[Option[model.Session]] =
    db.run(SessionRepository.read(sessionId))

  def delete(sessionId: HashId)(implicit db: Database): Future[Int] =
    db.run(SessionRepository.delete(sessionId))

}


object SessionRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "session"

  val sessions: TableQuery[SessionTable] = TableQuery[SessionTable]

  override protected def prepareTable: DBIO[Unit] = sessions.schema.create

  def create(sessionId: HashId, userId: HashId): DBIO[Int] =
    sessions += model.Session(sessionId, userId)

  def read(sessionId: HashId): DBIO[Option[model.Session]] =
    sessions.filter(_.sessionId === sessionId).result.headOption

  def delete(sessionId: HashId): DBIO[Int] =
    sessions.filter(_.sessionId === sessionId).delete

  final class SessionTable(tag: Tag) extends Table[model.Session](tag, schemaName, tableName) {
    import UserRepository.users

    def sessionId = column[HashId]("session_id", O.PrimaryKey)
    def userId = column[HashId]("user_id")

    def userIdFK = foreignKey(s"${tableName}_user_id_fkey", userId, users)(_.userId)

    override def * = (sessionId, userId).mapTo[model.Session]

  }

}
