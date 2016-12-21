ALTER TABLE post ADD INDEX `forum_date` (`forum`,`date`);
ALTER TABLE post ADD INDEX `thread_date` (`thread`,`date`);
ALTER TABLE post ADD INDEX `thread_fpath_lpath` (`thread`,`first_path`,`last_path`);
ALTER TABLE post ADD INDEX `user_date` (`user`,`date`);
