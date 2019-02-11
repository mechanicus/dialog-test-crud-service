package im.dig.trial.messenger.services.crud.cluster

import akka.actor.Actor
import akka.pattern.pipe
import im.dig.trial.messenger.services.crud.database.profile.api._
import im.dig.trial.messenger.services.crud.database._
import im.dig.trial.messenger.services.messages._

import scala.concurrent.ExecutionContext

/** CRUD-сервис представляет собой актор кластера, к которому
  * обращаются остальные микросервисы посредством отправки сообщений,
  * объявленных в пакете messages */
final class CrudService(
  private implicit val db: Database,
  private implicit val ec: ExecutionContext
) extends Actor {

  override def receive: Receive =
    userActionHandler orElse
    sessionActionHandler orElse
    messageActionHandler orElse
    fileActionHandler orElse
    personalMessageActionHandler orElse
    personalFileActionHandler orElse
    chatActionHandler orElse
    chatMemberActionHandler orElse
    chatMessageActionHandler orElse
    chatFileActionHandler

  private def userActionHandler: Receive = {
    case CreateUser(userId, nickname) =>
      Users.create(userId, nickname) pipeTo sender()
    case ReadUser(userId) =>
      Users.read(userId) pipeTo sender()
    case UpdateUser(userId, nickname) =>
      Users.update(userId, nickname) pipeTo sender()
    case DeleteUser(userId) =>
      Users.delete(userId) pipeTo sender()
    case FindUserByNickname(nickname) =>
      Users.findByNickname(nickname) pipeTo sender()
  }

  private def sessionActionHandler: Receive = {
    case CreateSession(sessionId, userId) =>
      Sessions.create(sessionId, userId) pipeTo sender()
    case ReadSession(sessionId) =>
      Sessions.read(sessionId) pipeTo sender()
    case DeleteSession(sessionId) =>
      Sessions.delete(sessionId) pipeTo sender()
  }

  private def messageActionHandler: Receive = {
    case CreateMessage(messageId, ownerId, content, createdOn) =>
      Messages.create(messageId, ownerId, content, createdOn)
    case ReadMessage(messageId) =>
      Messages.read(messageId) pipeTo sender()
    case ReadMessages(messageIds) =>
      Messages.read(messageIds) pipeTo sender()
  }

  private def fileActionHandler: Receive = {
    case CreateFile(fileId, ownerId, originalName, uploadedOn) =>
      Files.create(fileId, ownerId, originalName, uploadedOn) pipeTo sender()
    case ReadFiles(fileIds) =>
      Files.read(fileIds) pipeTo sender()
  }

  private def personalMessageActionHandler: Receive = {
    case CreatePersonalMessage(receiverId, senderId, messageId, sentOn) =>
      PersonalMessages.create(receiverId, senderId, messageId, sentOn)
    case GetPersonalMessageUpdates(userId, from) =>
      PersonalMessages.getUpdates(userId, from) pipeTo sender()
    case GetPersonalMessageHistory(user1, user2, to, limit) =>
      PersonalMessages.getHistory(user1, user2, to, limit) pipeTo sender()
  }

  private def personalFileActionHandler: Receive = {
    case CreatePersonalFile(receiverId, senderId, fileId, sentOn) =>
      PersonalFiles.create(receiverId, senderId, fileId, sentOn)
    case GetPersonalFileUpdates(userId, from) =>
      PersonalFiles.getUpdates(userId, from) pipeTo sender()
    case GetPersonalFileHistory(user1, user2, to, limit) =>
      PersonalFiles.getHistory(user1, user2, to, limit) pipeTo sender()
  }

  private def chatActionHandler: Receive = {
    case CreateChat(chatId, title) =>
      Chats.create(chatId, title)
    case ReadChat(chatId) =>
      Chats.read(chatId) pipeTo sender()
  }

  private def chatMemberActionHandler: Receive = {
    case CreateChatMember(chatId, userId) =>
      ChatMembers.create(chatId, userId)
    case GetChatMembers(chatId) =>
      ChatMembers.getChatMembers(chatId) pipeTo sender()
    case GetUserChats(userId) =>
      ChatMembers.getUserChats(userId) pipeTo sender()
  }

  private def chatMessageActionHandler: Receive = {
    case CreateChatMessage(chatId, senderId, messageId, sentOn) =>
      ChatMessages.create(chatId, senderId, messageId, sentOn)
    case GetChatsMessageUpdates(chatIds, from) =>
      ChatMessages.getUpdates(chatIds, from) pipeTo sender()
    case GetChatMessageHistory(chatId, to, limit) =>
      ChatMessages.getHistory(chatId, to, limit) pipeTo sender()
  }

  private def chatFileActionHandler: Receive = {
    case CreateChatFile(chatId, senderId, fileId, sentOn) =>
      ChatFiles.create(chatId, senderId, fileId, sentOn)
    case GetChatsFileUpdates(chatIds, from) =>
      ChatFiles.getUpdates(chatIds, from) pipeTo sender()
    case GetChatFileHistory(chatId, to, limit) =>
      ChatFiles.getHistory(chatId, to, limit) pipeTo sender()
  }

}
