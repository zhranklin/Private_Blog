package com.zhranklin.homepage.client.components

import japgolly.scalajs.react.extra.router.RouterCtl

/**
 * Created by zhranklin on 2017/5/10.
 */
object Nav {
  val title = new {
    val home = "Home"
    val tech = "Tech"
    val search = "Search"
    val notice = "Notice"
    val soj = "SOJ"
  }

  def apply(ctl: RouterCtl[Page], navActive: String) = {
    def item(title: String, target: Page) =
      <.li(^.classSet1("nav-item", "active" → (navActive == title)),
        <.a(^.cls := "nav-link", ctl setOnClick target, ^.href := "#",
          title + " ",
          <.span(^.cls := "sr-only", "(current)")))
    <.nav(^.cls := "navbar fixed-top navbar-expand-md navbar-dark bg-dark",
      <.a(^.cls := "navbar-brand", ctl setOnClick Page.Home, "Zhranklin's Blog", ^.href := "#"),
      <.ul(^.cls := "nav navbar-nav",
        item(title.home, Page.Home),
        item(title.search, Page.Search),
        item(title.notice, Page.Notice),
        item(title.soj, Page.ACM.List)
      ))
  }

}
