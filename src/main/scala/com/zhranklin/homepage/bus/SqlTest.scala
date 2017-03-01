package com.zhranklin.homepage.bus

import com.typesafe.config.ConfigFactory
import scalikejdbc._
object SqlSession {
  val conf = ConfigFactory.load().getConfig("settings.sql.test")
  val driver = conf.getString("driver")
  val url = conf.getString("url")
  val user = conf.getString("user")
  val pass = conf.getString("password")
  Class.forName(driver)
  ConnectionPool.singleton(url, user, pass)
  implicit val session = AutoSession
}

object BusDBInitializer {
  val init = SqlSession
  import SqlSession.session
    def run() {
      DB readOnly { implicit s =>
        try {
          sql"select 1 from user limit 1".map(_.long(1)).single.apply()
        } catch {
          case e: java.sql.SQLException =>
            DB autoCommit { implicit s =>
              sql"""
                    DROP TABLE IF EXISTS user;
                    DROP TABLE IF EXISTS route;
                    DROP TABLE IF EXISTS arrangement;
                    DROP TABLE IF EXISTS appointment;


                    CREATE TABLE user (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    stuId VARCHAR(20) NOT NULL,
                    password VARCHAR(20) NOT NULL,
                    name VARCHAR(20) NOT NULL,
                    gender VARCHAR(20) NOT NULL,
                    phone VARCHAR(20) NOT NULL);

                    CREATE TABLE route (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    "from" VARCHAR(20) NOT NULL,
                    to VARCHAR(20) NOT NULL
                    );

                    CREATE TABLE arrangement (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    time DATETIME NOT NULL,
                    route_id INT NOT NULL,
                    FOREIGN KEY(route_id) REFERENCES route(id)
                    );

                    CREATE TABLE appointment (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    app_time DATETIME NOT NULL,
                    arrangement_id INT,
                    route_id INT NOT NULL,
                    FOREIGN KEY(arrangement_id) REFERENCES arrangement(id),
                    FOREIGN KEY(route_id) REFERENCES route(id)
                    );

                    CREATE TABLE user_appoint (
                    id INT PRIMARY KEY AUTO_INCREMENT,
                    stamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    cust_id INT NOT NULL,
                    appoint_id INT NOT NULL,
                    FOREIGN KEY(cust_id) REFERENCES user(id),
                    FOREIGN KEY(appoint_id) REFERENCES appointment(id)
                    );

                    INSERT INTO user(stuId, password, name, gender, phone) VALUES ('2014141462355', 'psw', 'name1', '男', '12345678');
                    INSERT INTO user(stuId, password, name, gender, phone) VALUES ('2014141462356', 'psw', 'name2', '男', '12345679');
                    INSERT INTO user(stuId, password, name, gender, phone) VALUES ('2014141462357', 'psw', 'name3', '男', '12345670');
                    INSERT INTO user(stuId, password, name, gender, phone) VALUES ('2014141462358', 'psw', 'name4', '男', '12345671');
                    INSERT INTO user(stuId, password, name, gender, phone) VALUES ('2014141462359', 'psw', 'name5', '男', '12345672');

                    INSERT INTO route("from", to) VALUES ('江安', '望江');
                    INSERT INTO route("from", to) VALUES ('江安', '华西');
                    INSERT INTO route("from", to) VALUES ('望江', '江安');
                    INSERT INTO route("from", to) VALUES ('华西', '江安');
   """.execute.apply()
            }
        }
      }
    }

  }

