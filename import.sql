CREATE DATABASE IF NOT EXISTS `TPForum`;
USE `TPForum`;

DROP TABLE IF EXISTS `follows`;
CREATE TABLE `follows` (
  `follower` varchar(150) NOT NULL,
  `followed` varchar(150) NOT NULL,
  PRIMARY KEY (`follower`,`followed`),
  KEY `follower_followed` (`follower`,`followed`),
  KEY `followed_follower` (`followed`,`follower`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `forum`;                                                                       
/*!40101 SET @saved_cs_client     = @@character_set_client */;                                      
/*!40101 SET character_set_client = utf8 */;                                                        
CREATE TABLE `forum` (                                                                              
  `id` int(11) NOT NULL AUTO_INCREMENT,                                                             
  `name` varchar(150) NOT NULL,                                                                     
  `short_name` varchar(150) NOT NULL,                                                               
  `user` varchar(150) NOT NULL,                                                                     
  PRIMARY KEY (`id`),                                                                               
  UNIQUE KEY `id_UNIQUE` (`id`),                                                                    
  UNIQUE KEY `name_UNIQUE` (`name`),                                                                
  UNIQUE KEY `short_name_UNIQUE` (`short_name`)                                                     
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `post`;
CREATE TABLE `post` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `date` datetime NOT NULL,
  `message` text NOT NULL,
  `parent` int(11) DEFAULT NULL,
  `likes` int(11) NOT NULL DEFAULT '0',
  `dislikes` int(11) NOT NULL DEFAULT '0',
  `points` int(11) NOT NULL DEFAULT '0',
  `isApproved` tinyint(4) NOT NULL DEFAULT '0',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0',
  `isEdited` tinyint(4) NOT NULL DEFAULT '0',
  `isHighlighted` tinyint(4) NOT NULL DEFAULT '0',
  `isSpam` tinyint(4) NOT NULL DEFAULT '0',
  `forum` varchar(150) NOT NULL,
  `thread` int(11) NOT NULL,
  `user` varchar(150) NOT NULL,
  `first_path` int(11) DEFAULT NULL,
  `last_path` varchar(150) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `post_forum_date` (`forum`,`date`),
  KEY `post_thread_date` (`thread`,`date`),
  KEY `post_thread_fpath_lpath` (`thread`,`first_path`,`last_path`),
  KEY `post_user_date` (`user`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=1000001 DEFAULT CHARSET=utf8;



DROP TABLE IF EXISTS `subscribed`;                                                                  
/*!40101 SET @saved_cs_client     = @@character_set_client */;                                      
/*!40101 SET character_set_client = utf8 */;                                                        
CREATE TABLE `subscribed` (                                                                         
  `user` varchar(150) NOT NULL,                                                                                                                        
  `thread` int(11) NOT NULL,                                                                        
  PRIMARY KEY (`user`,`thread`)                                                                     
) ENGINE=InnoDB DEFAULT CHARSET=utf8;  



DROP TABLE IF EXISTS `thread`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `thread` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `title` varchar(150) NOT NULL,
  `date` datetime NOT NULL,
  `slug` varchar(150) NOT NULL,
  `message` text NOT NULL,
  `likes` int(11) NOT NULL DEFAULT '0',                                    
  `dislikes` int(11) NOT NULL DEFAULT '0',                                 
  `points` int(11) NOT NULL DEFAULT '0',
  `posts` int(11) NOT NULL DEFAULT '0',
  `isClosed` tinyint(4) NOT NULL DEFAULT '0',
  `isDeleted` tinyint(4) NOT NULL DEFAULT '0',
  `forum` varchar(150) NOT NULL,
  `user` varchar(150) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `thread_forum_date` (`forum`,`date`),
  KEY `thread_user_date` (`user`,`date`)
) ENGINE=InnoDB AUTO_INCREMENT=10001 DEFAULT CHARSET=utf8;


DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (                                                      
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `email` varchar(150) NOT NULL,
  `username` varchar(150) DEFAULT NULL,
  `name` varchar(150) DEFAULT NULL,
  `about` text,
  `isAnonymous` tinyint(4) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `email_UNIQUE` (`email`),
  KEY `user_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=100001 DEFAULT CHARSET=utf8;


CREATE TABLE IF NOT EXISTS `user_forum` (
	`user` varchar(150) NOT NULL,
	`forum` varchar(150) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
