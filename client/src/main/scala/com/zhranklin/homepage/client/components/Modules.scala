package com.zhranklin.homepage.client.components

import org.scalajs.jquery.JQueryStatic

import scala.scalajs.js.annotation.JSImport
import scala.scalajs.js

/**
 * Created by Zhranklin zhangwu@corp.netease.com at 2018/3/22
 */
object Modules {

  @js.native
  @JSImport("marked", JSImport.Namespace)
  private object _Marked extends js.Object
  val Marked = _Marked.asInstanceOf[js.Dynamic]

  @js.native
  @JSImport("jquery", JSImport.Namespace)
  object JQuery extends JQueryStatic

  @js.native
  @JSImport("highlightjs", JSImport.Namespace)
  object _Hljs extends js.Object
  val Hljs = _Hljs.asInstanceOf[js.Dynamic]

  @js.native
  @JSImport("simplemde", JSImport.Namespace)
  class SimpleMDE(options: JsObj) extends js.Object

}
