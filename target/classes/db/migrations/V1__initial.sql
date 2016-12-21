CREATE TABLE user (
  id        INT             NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name      CHAR(100),
  username  CHAR(30),
  email     CHAR(30) UNIQUE NOT NULL,
  about     CHAR(255),
  anonymous BOOLEAN         NOT NULL DEFAULT 0
)
  CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;

CREATE TABLE forum (
  id         INT              NOT NULL AUTO_INCREMENT PRIMARY KEY,
  name       CHAR(100) UNIQUE NOT NULL,
  short_name CHAR(30) UNIQUE  NOT NULL,
  user_id    INT              NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user (id)
)
  CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;


CREATE TABLE thread (
  id       INT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  title    CHAR(100) NOT NULL,
  message  CHAR(255) NOT NULL,
  slug     CHAR(100) NOT NULL,
  date     DATETIME  NOT NULL,
  closed   BOOLEAN   NOT NULL DEFAULT 0,
  deleted  BOOLEAN   NOT NULL DEFAULT 0,
  likes    INT       NOT NULL DEFAULT 0,
  dislikes INT       NOT NULL DEFAULT 0,
  posts    INT       NOT NULL DEFAULT 0,
  user_id  INT       NOT NULL,
  forum_id INT       NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (forum_id) REFERENCES forum (id)
)
  CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;


CREATE TABLE post (
  id            INT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
  message       CHAR(255) NOT NULL,
  date          DATETIME  NOT NULL,
  path          CHAR(255) NOT NULL,
  parent        INT,
  isApproved    BOOLEAN   NOT NULL DEFAULT 0,
  isHighlighted BOOLEAN   NOT NULL DEFAULT 0,
  isEdited      BOOLEAN   NOT NULL DEFAULT 0,
  isSpam        BOOLEAN   NOT NULL DEFAULT 0,
  isDeleted     BOOLEAN   NOT NULL DEFAULT 0,
  likes         INT       NOT NULL DEFAULT 0,
  dislikes      INT       NOT NULL DEFAULT 0,
  user_id       INT       NOT NULL,
  forum_id      INT       NOT NULL,
  thread_id     INT       NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (forum_id) REFERENCES forum (id),
  FOREIGN KEY (thread_id) REFERENCES thread (id)
)
  CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;





CREATE TABLE subscription (
  user_id   INT NOT NULL,
  thread_id INT NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user (id),
  FOREIGN KEY (thread_id) REFERENCES thread (id),
  PRIMARY KEY (user_id, thread_id)
)
  CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;


CREATE TABLE following (
  follower_id INT NOT NULL,
  followee_id INT NOT NULL,
  FOREIGN KEY (follower_id) REFERENCES user (id),
  FOREIGN KEY (followee_id) REFERENCES user (id),
  PRIMARY KEY (follower_id, followee_id)
)
  CHARACTER SET utf8
  DEFAULT COLLATE utf8_general_ci;