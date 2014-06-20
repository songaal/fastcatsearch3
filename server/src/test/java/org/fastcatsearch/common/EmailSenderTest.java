package org.fastcatsearch.common;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fastcatsearch.common.EmailSender.GmailProperties;
import org.junit.Test;

public class EmailSenderTest {

	String text = "<h1>메일잘가나</h1>" +
			"<div class=\"columns profilecols js-username mine col-2\" data-name=\"fastcatsearch\">\n" + 
			"    \n" + 
			"      <div class=\"first vcard\" itemscope=\"\" itemtype=\"http://schema.org/Organization\">\n" + 
			"        <div class=\"avatared\">\n" + 
			"            <span class=\"tooltipped downwards\" original-title=\"Change your organization's avatar at gravatar.com\"><a href=\"http://gravatar.com/emails/\"><img height=\"210\" src=\"https://secure.gravatar.com/avatar/027d65ee1b845192ae788cf6d7de0b6d?s=420&amp;d=https://a248.e.akamai.net/assets.github.com%2Fimages%2Fgravatars%2Fgravatar-org-420.png\" width=\"210\"></a></span>\n" + 
			"\n" + 
			"          <h1>\n" + 
			"              <span itemprop=\"name\">fastcatsearch</span>\n" + 
			"              <em itemprop=\"additionalName\">fastcatsearch</em>\n" + 
			"          </h1>\n" + 
			"\n" + 
			"          <div class=\"details\">\n" + 
			"            \n" + 
			"            <dl><dt><span class=\"octicon octicon-mail\"></span></dt><dd><a class=\"email js-obfuscate-email\" data-email=\"%66%61%73%74%63%61%74%73%65%61%72%63%68%40%67%6f%6f%67%6c%65%67%72%6f%75%70%73%2e%63%6f%6d\" href=\"mailto:fastcatsearch@googlegroups.com\">fastcatsearch@googlegroups.com</a></dd></dl>\n" + 
			"            <dl><dt><span class=\"octicon octicon-link\"></span></dt><dd itemprop=\"url\"><a href=\"http://www.fastcatsearch.org\" class=\"url\" rel=\"nofollow me\">http://www.fastcatsearch.org</a></dd></dl>\n" + 
			"            <dl><dt><span class=\"octicon octicon-clock\"></span></dt><dd><span class=\"join-label\">Joined on </span><span class=\"join-date\">Mar 06, 2013</span></dd></dl>\n" + 
			"          </div>\n" + 
			"        </div>\n" + 
			"\n" + 
			"        <ul class=\"stats\">\n" + 
			"          <li>\n" + 
			"            <a href=\"/fastcatsearch/repositories\">\n" + 
			"              <strong>4</strong>\n" + 
			"              <span>public repos</span>\n" + 
			"            </a>\n" + 
			"          </li>\n" + 
			"            <li>\n" + 
			"              <a href=\"/organizations/fastcatsearch/settings#repos_bucket\">\n" + 
			"                <strong>0</strong>\n" + 
			"                <span>private repos</span>\n" + 
			"              </a>\n" + 
			"            </li>\n" + 
			"          <li>\n" + 
			"            <a href=\"/fastcatsearch?tab=members\">\n" + 
			"              <strong>4</strong>\n" + 
			"              <span>members</span>\n" + 
			"            </a>\n" + 
			"          </li>\n" + 
			"        </ul>\n" + 
			"      </div><!-- /.first -->\n" + 
			"\n" + 
			"    <div class=\"last\">\n" + 
			"      <div class=\"tabnav\">\n" + 
			"        <ul class=\"tabnav-tabs\" data-pjax=\"\">\n" + 
			"          <li data-tab=\"repo\">\n" + 
			"            <a href=\"/fastcatsearch\" class=\"tabnav-tab selected\">\n" + 
			"              <span class=\"octicon octicon-repo\"></span>\n" + 
			"              Repositories\n" + 
			"            </a>\n" + 
			"          </li>\n" + 
			"          <li data-tab=\"members\">\n" + 
			"            <a href=\"/fastcatsearch?tab=members\" class=\"tabnav-tab \">\n" + 
			"              <span class=\"octicon octicon-person-team\"></span>\n" + 
			"              Members\n" + 
			"            </a>\n" + 
			"          </li>\n" + 
			"        </ul>\n" + 
			"\n" + 
			"          <div class=\"tabnav-right\">\n" + 
			"            <div class=\"tabnav-widget\">\n" + 
			"                <a href=\"/organizations/fastcatsearch/settings\" class=\"minibutton\"><span class=\"octicon octicon-pencil\"></span>Edit fastcatsearch's Profile</a>\n" + 
			"            </div>\n" + 
			"          </div>\n" + 
			"      </div>\n" + 
			"\n" + 
			"      <div class=\"tab-content js-repo-filter\">\n" + 
			"          \n" + 
			"  <div class=\"repo-tab\">\n" + 
			"      <div class=\"filter-bar\">\n" + 
			"        <input type=\"text\" id=\"your-repos-filter\" class=\"filter_input\" placeholder=\"Find a Repository…\" tabindex=\"2\">\n" + 
			"        <ul class=\"repo_filterer\">\n" + 
			"          <li><a href=\"#\" class=\"repo_filter\" rel=\"mirror\">Mirrors</a></li>\n" + 
			"          <li><a href=\"#\" class=\"repo_filter\" rel=\"fork\">Forks</a></li>\n" + 
			"          <li><a href=\"#\" class=\"repo_filter\" rel=\"source\">Sources</a></li>\n" + 
			"            <li><a href=\"#\" class=\"repo_filter\" rel=\"private\">Private</a></li>\n" + 
			"            <li><a href=\"#\" class=\"repo_filter\" rel=\"public\">Public</a></li>\n" + 
			"          <li class=\"all_repos\"><a href=\"#\" class=\"repo_filter filter_selected\" rel=\"public, li.private\">All</a></li>\n" + 
			"        </ul>\n" + 
			"      </div>\n" + 
			"\n" + 
			"      <ul class=\"repolist js-repo-list\">\n" + 
			"    \n" + 
			"<li class=\"public source\">\n" + 
			"  <ul class=\"repo-stats\">\n" + 
			"      <li>Java</li>\n" + 
			"    <li class=\"stargazers\">\n" + 
			"      <a href=\"/fastcatsearch/fastcatsearch/stargazers\" title=\"You starred this!\" class=\"is-starred\">\n" + 
			"        <span class=\"octicon octicon-star\"></span> 1\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"    <li class=\"forks\">\n" + 
			"      <a href=\"/fastcatsearch/fastcatsearch/network\" title=\"Forks\">\n" + 
			"        <span class=\"octicon octicon-git-branch\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"  </ul>\n" + 
			"\n" + 
			"  <h3>\n" + 
			"    <span class=\"mega-octicon octicon-repo\"></span>\n" + 
			"    <a href=\"/fastcatsearch/fastcatsearch\">fastcatsearch</a>\n" + 
			"  </h3>\n" + 
			"\n" + 
			"\n" + 
			"    <div class=\"body\">\n" + 
			"        <p class=\"description\">\n" + 
			"          fastcatsearch opensource search engine\n" + 
			"        </p>\n" + 
			"\n" + 
			"        <p class=\"updated-at\">Last updated <time class=\"js-relative-date\" datetime=\"2013-05-18T05:30:50-07:00\" title=\"2013-05-18 05:30:50\">3 days ago</time></p>\n" + 
			"\n" + 
			"      <div class=\"participation-graph\">\n" + 
			"        <canvas class=\"bars\" data-color-all=\"#F5F5F5\" data-color-owner=\"#DFDFDF\" data-source=\"/fastcatsearch/fastcatsearch/graphs/owner_participation\" height=\"80\" width=\"640\"></canvas>\n" + 
			"      </div>\n" + 
			"    </div><!-- /.body -->\n" + 
			"</li>\n" + 
			"\n" + 
			"<li class=\"public source\">\n" + 
			"  <ul class=\"repo-stats\">\n" + 
			"      <li>Java</li>\n" + 
			"    <li class=\"stargazers\">\n" + 
			"      <a href=\"/fastcatsearch/fastcatsearch-ir/stargazers\" title=\"Stargazers\" class=\"\">\n" + 
			"        <span class=\"octicon octicon-star\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"    <li class=\"forks\">\n" + 
			"      <a href=\"/fastcatsearch/fastcatsearch-ir/network\" title=\"Forks\">\n" + 
			"        <span class=\"octicon octicon-git-branch\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"  </ul>\n" + 
			"\n" + 
			"  <h3>\n" + 
			"    <span class=\"mega-octicon octicon-repo\"></span>\n" + 
			"    <a href=\"/fastcatsearch/fastcatsearch-ir\">fastcatsearch-ir</a>\n" + 
			"  </h3>\n" + 
			"\n" + 
			"\n" + 
			"    <div class=\"body\">\n" + 
			"        <p class=\"description\">\n" + 
			"          fastcatsearch-ir\n" + 
			"        </p>\n" + 
			"\n" + 
			"        <p class=\"updated-at\">Last updated <time class=\"js-relative-date\" datetime=\"2013-05-16T00:38:53-07:00\" title=\"2013-05-16 00:38:53\">5 days ago</time></p>\n" + 
			"\n" + 
			"      <div class=\"participation-graph\">\n" + 
			"        <canvas class=\"bars\" data-color-all=\"#F5F5F5\" data-color-owner=\"#DFDFDF\" data-source=\"/fastcatsearch/fastcatsearch-ir/graphs/owner_participation\" height=\"80\" width=\"640\"></canvas>\n" + 
			"      </div>\n" + 
			"    </div><!-- /.body -->\n" + 
			"</li>\n" + 
			"\n" + 
			"<li class=\"public source\">\n" + 
			"  <ul class=\"repo-stats\">\n" + 
			"      <li>JavaScript</li>\n" + 
			"    <li class=\"stargazers\">\n" + 
			"      <a href=\"/fastcatsearch/techmanual/stargazers\" title=\"Stargazers\" class=\"\">\n" + 
			"        <span class=\"octicon octicon-star\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"    <li class=\"forks\">\n" + 
			"      <a href=\"/fastcatsearch/techmanual/network\" title=\"Forks\">\n" + 
			"        <span class=\"octicon octicon-git-branch\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"  </ul>\n" + 
			"\n" + 
			"  <h3>\n" + 
			"    <span class=\"mega-octicon octicon-repo\"></span>\n" + 
			"    <a href=\"/fastcatsearch/techmanual\">techmanual</a>\n" + 
			"  </h3>\n" + 
			"\n" + 
			"\n" + 
			"    <div class=\"body\">\n" + 
			"\n" + 
			"        <p class=\"updated-at\">Last updated <time class=\"js-relative-date\" datetime=\"2013-05-10T23:04:15-07:00\" title=\"2013-05-10 23:04:15\">10 days ago</time></p>\n" + 
			"\n" + 
			"      <div class=\"participation-graph\">\n" + 
			"        <canvas class=\"bars\" data-color-all=\"#F5F5F5\" data-color-owner=\"#DFDFDF\" data-source=\"/fastcatsearch/techmanual/graphs/owner_participation\" height=\"80\" width=\"640\"></canvas>\n" + 
			"      </div>\n" + 
			"    </div><!-- /.body -->\n" + 
			"</li>\n" + 
			"\n" + 
			"<li class=\"public source\">\n" + 
			"  <ul class=\"repo-stats\">\n" + 
			"      <li>Java</li>\n" + 
			"    <li class=\"stargazers\">\n" + 
			"      <a href=\"/fastcatsearch/fastcatsearch-api/stargazers\" title=\"Stargazers\" class=\"\">\n" + 
			"        <span class=\"octicon octicon-star\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"    <li class=\"forks\">\n" + 
			"      <a href=\"/fastcatsearch/fastcatsearch-api/network\" title=\"Forks\">\n" + 
			"        <span class=\"octicon octicon-git-branch\"></span> 0\n" + 
			"      </a>\n" + 
			"    </li>\n" + 
			"  </ul>\n" + 
			"\n" + 
			"  <h3>\n" + 
			"    <span class=\"mega-octicon octicon-repo\"></span>\n" + 
			"    <a href=\"/fastcatsearch/fastcatsearch-api\">fastcatsearch-api</a>\n" + 
			"  </h3>\n" + 
			"\n" + 
			"\n" + 
			"    <div class=\"body\">\n" + 
			"        <p class=\"description\">\n" + 
			"          fastcatsearch support utility\n" + 
			"        </p>\n" + 
			"\n" + 
			"        <p class=\"updated-at\">Last updated <time class=\"js-relative-date\" datetime=\"2013-04-17T21:57:54-07:00\" title=\"2013-04-17 21:57:54\">a month ago</time></p>\n" + 
			"\n" + 
			"      <div class=\"participation-graph\">\n" + 
			"        <canvas class=\"bars\" data-color-all=\"#F5F5F5\" data-color-owner=\"#DFDFDF\" data-source=\"/fastcatsearch/fastcatsearch-api/graphs/owner_participation\" height=\"80\" width=\"640\"></canvas>\n" + 
			"      </div>\n" + 
			"    </div><!-- /.body -->\n" + 
			"</li>\n" + 
			"\n" + 
			"    \n" + 
			"</ul>\n" + 
			"\n" + 
			"  </div>\n" + 
			"\n" + 
			"      </div>\n" + 
			"    </div><!-- /.last -->\n" + 
			"\n" + 
			"  </div>";
	
	@Test
	public void testSMTP() throws IOException {
		GmailProperties gmailProperties = new GmailProperties(true);
		gmailProperties.setAuthentication("webmaster@websqrd.com", "dnpqtmznpdjem1@");
		EmailSender emailSender = new EmailSender(gmailProperties);
		String fromAddress = "";
		List<String> recipientToList = new ArrayList<String>();
		List<String> recipientCCList = new ArrayList<String>();
		List<String> recipientBCCList = new ArrayList<String>();
		
		recipientToList.add("swsong@websqrd.com");
//		recipientToList.add("lupfeliz@websqrd.com");
//		recipientCCList.add("lupfeliz@gmail.com");
//		recipientCCList.add("songaal@gmail.com");
//		
//		recipientBCCList.add("songaal@naver.com");
		
		String subject = "웹스퀘어드 메일테스트입니다.";
		
		List<File> files = new ArrayList<File>();
//		files.add(new File("/Users/swsong/Desktop/20130414184_수원시_수원시.hwp"));
//		files.add(new File("/Users/swsong/Desktop/dic.product.txt"));
//		files.add(new File("/Users/swsong/Desktop/fastcat.jpg"));
		
		emailSender.send(fromAddress, recipientToList, recipientCCList, recipientBCCList, subject, text, "text/plain", files);
	}
	
	@Test
	public void testSendmail() throws IOException {
		Sendmail sendmail = new Sendmail("sendmail");
		String fromAddress = "webmaster";
		List<String> recipientToList = new ArrayList<String>();
		
		recipientToList.add("swsong@websqrd.com");
		recipientToList.add("songaal@naver.com");
		
		String subject = "웹스퀘어드 메일테스트입니다."+ new Date();
		String text = "주어진 돈에서 여행을 해야 하는 일정상 돈을 아껴야 하는 안정환은 기내에서 간식으로 나온 삼각 김밥과 물을 싸오는 알뜰한 모습을 보였다. \n안정환은 무더위에 목말라 하는 아들 리환이에게 싸온 물을 먹이기도 하고, \n출출해진 배를 달래려 삼각 김밥을 먹는 모습도 보였다.";
		sendmail.sendText(fromAddress, recipientToList, subject, text);
	}

}
