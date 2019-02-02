package im.dig.trial.messenger.services.crud.database

import im.dig.trial.messenger.services.crud.database.profile.api._
import slick.jdbc.meta.MTable

import scala.concurrent.ExecutionContext

trait AbstractRepository {

  protected def schemaName: Option[String]
  protected def tableName: String
  protected def prepareTable: DBIO[Unit]

  def prepareSchema(implicit ec: ExecutionContext): DBIO[Unit] = DBIO.seq(
    createSchemaIfNotExists,
    MTable.getTables(None, schemaName, Some(tableName), None) flatMap { tables =>
      if (tables.isEmpty) {
        prepareTable
      } else {
        DBIO.sequence(IndexedSeq.empty)
      }
    }
  )

  private def createSchemaIfNotExists: DBIO[Int] = schemaName match {
    case Some(name) => sqlu"""CREATE SCHEMA IF NOT EXISTS "#$name""""
    case None => DBIO.successful(0)
  }

}
