package com.zhranklin.homepage.notice

import org.jsoup.Jsoup
import org.jsoup.nodes.Document

object NoticeServiceObjects {
  import NoticeFetcher._

  val csNoticeFetcher = new FunNoticeFetcher(contentF("#BodyLabel"), dateF("td[width]:matches(时间.*来源)"))

  class CSService(name: String, part: String, tpl: String) extends NoticeService(s"计算机学院 - $name",
    new SelectorIndexService(s".*/$part/.*\\d{10,}.*", s"http://$tpl<index>.htm"), csNoticeFetcher)

  class LawService(title: String, listId: String) extends NoticeService(s"法学院 - $title",
    new IndexService("http://law.scu.edu.cn/xjax?arg=8573&arg=<index>&arg=20&arg=list&clazz=PortalArticleAction&method=list") {
      import org.json4s._
      import jackson.parseJson
      def extractFromRawUrl(a: E) = ???
      def rawUrlsFromDoc(doc: Document) = ???
      def getUrl(id: String) = s"http://law.scu.edu.cn/detail.jsp?portalId=725&cid=8385&nextcid=$listId&aid=$id"
      override def noticeUrlsFromUrl(url: String): Iterable[NoticeEntry] = {
        val jsonStr = Jsoup.connect(url).execute().body()
        val json = parseJson(jsonStr)
        json.\("data").asInstanceOf[JArray].arr.map(
          jo ⇒ NoticeEntry(getUrl(jo.\("id").values.toString), Some(jo.\("subject").values.toString)))
      }
    }, new FunNoticeFetcher(contentF("div.text"), dateF("span:contains(发布时间)")))

  val serviceList = List(
    new NoticeService("教务处 - 通知", new SelectorIndexService("newsShow.*",
      "http://jwc.scu.edu.cn/jwc/moreNotice.action?url=moreNotice.action&type=2&keyWord=&pager.pageNow=<index>"),
      new FunNoticeFetcher(selectorF("input[name=news.content]")(_.first.attr("value")),
        dateF("table[width=900] td:contains(发布时间)"))),
    new CSService("学术看板", "xskb", "cs.scu.edu.cn/cs/xsky/xskb/H951901index_"),
    new CSService("学院通知", "xytz", "cs.scu.edu.cn/cs/xytz/H9502index_"),
    new CSService("学院通知", "xyxw", "cs.scu.edu.cn/cs/xyxw/H9501index_"),
    new CSService("访谈录", "ftl", "cs.scu.edu.cn/cs/fwzy/ftl/H951204index_"),
    new CSService("川大在线", "cdzx", "news.scu.edu.cn/news2012/cdzx/I0201index_"),
    new NoticeService("电气信息学院 - 学院通知", new SelectorIndexService(".*detail\\.jsp", "http://seei.scu.edu.cn/student,p<index>,index.jsp"),
      new FunNoticeFetcher(contentF("td[width=770]"), dateF("p:contains(发布时间)"))),
//  new NoticeService("数学学院 - 学术报告", new SelectorIndexService("xsbg_show\\.asp\\?classid.*", "http://math.scu.edu.cn/xsbg.asp?PAGE=<index>"),
//    new FunNoticeFetcher(contentF("td[width=796]"), dateF("td:contains(发布时间)"))),
    new NoticeService("数学学院 - 学院新闻", new SelectorIndexService("News_show\\.asp\\?classid.*", "http://math.scu.edu.cn/news.asp?PAGE=<index>"),
      new FunNoticeFetcher(contentF("td[height=504]"), dateF("td:contains(更新时间)"))),
    new NoticeService("经济学院 - 学院新闻", new SelectorIndexService("/news/\\d+.html", "http://sesu.scu.edu.cn/news/list_1_<index>.html"),
      new FunNoticeFetcher(contentF("div.mt20"), dateF("div.info"))),
    new NoticeService("经济学院 - 学院公告", new SelectorIndexService("/gonggao/\\d+.html", "http://sesu.scu.edu.cn/gonggao/list_2_<index>.html"),
      new FunNoticeFetcher(contentF("div.mt20"), dateF("div.info"))),
    new LawService("学院新闻", "8572"),
    new LawService("学院公告", "8573"),
    new NoticeService("文学与新闻学院 - 学院动态", new SelectorIndexService("ReadNews\\.asp", "http://www.sculj.cn/Special_News.asp?SpecialID=40&SpecialName=%D1%A7%D4%BA%B6%AF%CC%AC&page=<index>"),
      new FunNoticeFetcher(contentF("tr:has(td[height=50]:matches(阅读\\d+次)) + tr > td"), dateF("td[height=50]:matches(阅读\\d+次)"))),
    new NoticeService("外国语学院 - 学院公告", new SelectorIndexService("/foreign/a/xueyuangonggao/\\d+/\\d+/\\d+\\.html", "http://flc2.scu.edu.cn/foreign/a/xueyuangonggao/list_27_<index>.html"),
      new FunNoticeFetcher(contentF("div.content td"), dateF("div.info")))
  )
}