package im.dig.trial.messenger.services.crud.database

import cats.implicits._
import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.model._

import scala.concurrent.{ExecutionContext, Future}


object Users {

  def getAll(implicit db: Database): Future[Seq[User]] =
    db.run(UserRepository.all)

  def create(userId: UserId, nickname: Nickname)(implicit db: Database, ec: ExecutionContext): Future[Either[String, Int]] = {
    val actions = UserRepository.findByNickname(nickname).flatMap { optUser =>
      if (optUser.isDefined) {
        DBIO.successful(s"user with nickname = '${nickname.value}' already exists".asLeft)
      } else {
        UserRepository.create(userId, nickname).map(_.asRight)
      }
    }
    db.run(actions.transactionally)
  }

  def read(userId: UserId)(implicit db: Database): Future[Option[User]] =
    db.run(UserRepository.read(userId))

  def update(userId: UserId, nickname: Nickname)(implicit db: Database, ec: ExecutionContext): Future[Either[String, Int]] = {
    val actions = UserRepository.findByNickname(nickname).flatMap { optUser =>
      if (optUser.isDefined) {
        DBIO.successful(s"user with nickname = '${nickname.value}' already exists".asLeft)
      } else {
        UserRepository.update(userId, nickname).map(_.asRight)
      }
    }
    db.run(actions.transactionally)
  }

  def delete(userId: UserId)(implicit db: Database): Future[Int] =
    db.run(UserRepository.delete(userId))

  def findByNickname(nickname: Nickname)(implicit db: Database): Future[Option[User]] =
    db.run(UserRepository.findByNickname(nickname))

}


object UserRepository extends AbstractRepository {

  override protected val schemaName: Option[String] = Some("messenger")
  override protected val tableName: String = "user"

  val users: TableQuery[UserTable] = TableQuery[UserTable]

  override protected val prepareTable: DBIO[Unit] = users.schema.create

  def all: DBIO[Seq[User]] = users.result

  def create(userId: UserId, nickname: Nickname): DBIO[Int] =
    users += User(userId, nickname)

  def read(userId: UserId): DBIO[Option[User]] =
    users.filter(_.userId === userId).result.headOption

  def update(userId: UserId, nickname: Nickname): DBIO[Int] =
    users.filter(_.userId === userId).map(_.nickname).update(nickname)

  def delete(userId: UserId): DBIO[Int] =
    users.filter(_.userId === userId).delete

  def findByNickname(nickname: Nickname): DBIO[Option[User]] =
    users.filter(_.nickname === nickname).result.headOption


  final class UserTable(tag: Tag) extends Table[User](tag, schemaName, tableName) {
    def userId = column[UserId]("user_id", O.PrimaryKey)
    def nickname = column[Nickname]("nickname", O.Unique, O.SqlType("text"))
    override def * = (userId, nickname).mapTo[User]
  }

}
