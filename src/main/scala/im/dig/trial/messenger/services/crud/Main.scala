package im.dig.trial.messenger.services.crud

import akka.actor.{ActorSystem, Props}
import im.dig.trial.messenger.services.crud.cluster.CrudService
import im.dig.trial.messenger.services.crud.database._
import im.dig.trial.messenger.services.crud.database.profile.api._

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

object Main {

  def main(args: Array[String]): Unit = {
    val system = ActorSystem("MessengerBackend")
    implicit val db: Database = Database.forConfig("database")
    implicit val ec: ExecutionContext = system.dispatchers.lookup("akka.io.pinned-dispatcher")
    prepareDatabase()
    system.actorOf(Props(classOf[CrudService], db, ec), "crud-service")
  }

  // подготавливаем схему БД к работе
  // создаем схему и таблицы, если они еще не существуют
  private def prepareDatabase()(implicit db: Database, ec: ExecutionContext): Unit = {
    val execution = db.run {
      DBIO.seq(
        UserRepository.prepareSchema,
        SessionRepository.prepareSchema,
        MessageRepository.prepareSchema,
        FileRepository.prepareSchema,
        PersonalMessageRepository.prepareSchema,
        PersonalFileRepository.prepareSchema,
        ChatRepository.prepareSchema,
        ChatMemberRepository.prepareSchema,
        ChatMessageRepository.prepareSchema,
        ChatFileRepository.prepareSchema
      )
    }
    Await.result(execution, Duration.Inf)
  }

}
