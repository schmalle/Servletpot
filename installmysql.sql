DROP DATABASE IF EXISTS ServletPot;
CREATE DATABASE ServletPot;

use ServletPot;

CREATE TABLE Files(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 length LONG,
                                 hash   LONG,
                                 found  TEXT,
                                 counter long);

CREATE TABLE URIs(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 uri    BLOB,
                                 hash   long,
                                 length    long,
                                 counter long);

CREATE TABLE IPs(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 ip     TEXT,
                                 found  TEXT,
                                 counter long);

CREATE TABLE DontUseURIs(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 uri    TEXT);

CREATE TABLE Posts(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 uri    BLOB,
                                 attack BLOB,
                                 hash   long,
                                 length    long,
                                 counter long,
                                 found  TEXT,
                                 ip     TEXT);

CREATE TABLE Gets(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 uri    BLOB,
                                 hash   long,
                                 length    long,
                                 found  TEXT,
                                 ip     TEXT);

CREATE TABLE Config(id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                 counter    long);


INSERT INTO  Config      VALUES (1, 0);
INSERT INTO  DontUseURIs VALUES (1, "www.google.");
INSERT INTO  DontUseURIs VALUES (2, "www.amazon.");
INSERT INTO  DontUseURIs VALUES (3, "www.t-online.");
INSERT INTO  DontUseURIs VALUES (4, "www.telekom.");
INSERT INTO  DontUseURIs VALUES (5, "www.yahoo.");


GRANT ALL PRIVILEGES
       ON ServletPot.*
       TO 'servletpot'@'localhost'
       IDENTIFIED BY 'pw100pw200';
       