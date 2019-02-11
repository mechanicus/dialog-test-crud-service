package im.dig.trial.messenger.services.model

import java.security.SecureRandom
import java.time.LocalDateTime

import cats._
import cats.implicits._
import org.apache.commons.codec.binary.Hex


// Здесь собраны все классы общей для всех сервисов бэкенда модели.

/** Универсальный идентификатор любой сущности в БД.
  * SHA-256 выбран в силу крайне низкой вероятности коллизий
  * и вероятности подбора методом перебора значений */
@SerialVersionUID(1L)
final case class SHA256(bytes: Array[Byte])

object SHA256 {
  private val random = new SecureRandom()
  def generate(): SHA256 = {
    val bytes = Array.fill[Byte](32)(0)
    random.nextBytes(bytes)
    SHA256(bytes)
  }
  implicit val showSHA256: Show[SHA256] =
    sha256 => Hex.encodeHexString(sha256.bytes)
  implicit val readSHA256: Read[SHA256] = string => {
    if (string.length =!= 64)
      throw new IllegalArgumentException(s"String '$string' is not a SHA256 hash")
    SHA256(Hex.decodeHex(string))
  }
}



/** Спец тип для имени пользователя. Допускаются только буквенно-числовые
  * имена длиннее двух символов */
@SerialVersionUID(1L)
final case class Nickname(value: String)

object Nickname {
  implicit val readNickname: Read[Nickname] = { string =>
    if (string.length < 3)
      throw new IllegalArgumentException("Nickname should contain at least 3 characters")
    if (string.exists(!_.isLetterOrDigit))
      throw new IllegalArgumentException("Nickname should be alphanumeric")
    Nickname(string)
  }
  implicit val showNickname: Show[Nickname] = _.value
}



/** Спец тип для непустой строки. Например тело сообщения или имя чата */
@SerialVersionUID(1L)
final case class NonEmptyString(value: String)

object NonEmptyString {
  implicit val readNonEmptyString: Read[NonEmptyString] = { string =>
    if (string.isEmpty)
      throw new IllegalArgumentException("String should be non empty")
    NonEmptyString(string)
  }
  implicit val showNonEmptyString: Show[NonEmptyString] = _.value
}



/** Кроссплатформенное имя файла */
@SerialVersionUID(1L)
final case class Filename(value: String)

object Filename {
  private val invalidFilenameCharacters = """\/:*?"<>|""".toSet + '\u0000'
  implicit val readFilename: Read[Filename] = { string =>
    if (string.exists(char => invalidFilenameCharacters.contains(char)))
      throw new IllegalArgumentException("The filename contains invalid characters")
    Filename(string)
  }
  implicit val showFilename: Show[Filename] = _.value
}



/** Сущность пользователя мессенджера */
@SerialVersionUID(1L)
final case class User (
  userId: UserId,
  nickname: Nickname
)

/** Сессия клиента. Позволяет идентифицировать один отдельный сеанс работы
  * конкретного пользовательского клиента с бэкендом */
@SerialVersionUID(1L)
final case class Session (
  sessionId: SessionId,
  userId: UserId
)

/** Любое сообщение, созданное пользователем */
@SerialVersionUID(1L)
final case class Message (
  messageId: MessageId,
  ownerId: UserId,
  content: NonEmptyString,
  createdOn: LocalDateTime
)

/** Любой файл, созданный пользователем */
@SerialVersionUID(1L)
final case class File (
  fileId: FileId,
  ownerId: UserId,
  originalName: Filename,
  uploadedOn: LocalDateTime
)

/** Сообщение, отправленное отдельному пользователю (личное сообщение) */
@SerialVersionUID(1L)
final case class PersonalMessage (
  receiverId: UserId,
  senderId: UserId,
  messageId: MessageId,
  sentOn: LocalDateTime
)

/** Файл, отправленный отдельному пользователю */
@SerialVersionUID(1L)
final case class PersonalFile (
  receiverId: UserId,
  senderId: UserId,
  fileId: FileId,
  sentOn: LocalDateTime
)

/** Сущность чата */
@SerialVersionUID(1L)
final case class Chat (
  chatId: ChatId,
  title: NonEmptyString
)

/** Пользователь чата */
@SerialVersionUID(1L)
final case class ChatMember (
  chatId: ChatId,
  userId: UserId
)

/** Сообщение, отправленное в чат */
@SerialVersionUID(1L)
final case class ChatMessage (
  chatId: ChatId,
  senderId: UserId,
  messageId: MessageId,
  sentOn: LocalDateTime
)

/** Файл, отправленный в чат */
@SerialVersionUID(1L)
final case class ChatFile (
  chatId: ChatId,
  senderId: UserId,
  fileId: FileId,
  sentOn: LocalDateTime
)
