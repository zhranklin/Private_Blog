package com.zhranklin.homepage

import com.zhranklin.homepage.Dtos._

/**
 * Created by Zhranklin on 2017/3/31.
 */
object Dtos {
  case class ArticleEdit(title: String, author: String, section: String,
                         mdown: Option[String] = None, html: String = "", abs: String, tags: List[String])
  case class ArticleItem(id: String, title: String, author: String, create_time: String, abs: String, tags: List[String])

  case class ProblemItem(id: String, title: String, submit: Int, solved: Int)
  case class Problem(title: String, html: String)

}

object Apis {
  
  trait ArticleApi {
    def list(): List[ArticleItem]
    def get(id: String): Option[ArticleEdit]
    def save(id: Option[String], article: ArticleEdit)
  }

  trait AcmApi {
    def list(): List[ProblemItem]
    def detail(id: String): Problem
  }

}
