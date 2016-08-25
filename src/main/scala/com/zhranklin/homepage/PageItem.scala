package com.zhranklin.homepage

import java.util.Date

import com.fasterxml.jackson.annotation.JsonIgnoreType

/**
  * Created by Zhranklin on 16/8/22.
  */
trait PageItem {
  val itemTitle: String
  val itemText: String
  val itemLink: String
  val itemTags: List[String]
  val itemTime: Date
}
