USE moviedb;

ALTER TABLE movies ADD FULLTEXT(title);
ALTER TABLE stars ADD FULLTEXT(name);

