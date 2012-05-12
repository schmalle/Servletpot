package org.metams.ServletPot.plugins.http;

import java.io.PrintWriter;
import java.util.Random;

/**
 * User: flake
 * Date: Jul 13, 2010
 * Time: 9:44:44 PM
 */
public class Robots
{

    private String[] m_robotBase = {


    "Disallow:/search",
    "Disallow:/groups   ",
    "Disallow:/images     ",
    "Disallow:/catalogs    ",
    "Disallow:/catalogues    ",
    "Disallow:/news            ",
    "Allow:/news/directory       ",
    "Disallow:/nwshp               ",
    "Disallow:/setnewsprefs?         ",
    "Disallow:/index.html?             ",
    "Disallow:/?                         ",
    "Disallow:/addurl/image?               ",
    "Disallow:/pagead/                       ",
    "Disallow:/relpage/                        ",
    "Disallow:/relcontent                        ",
    "Disallow:/imgres                              ",
    "Disallow:/imglanding                            ",
    "Disallow:/keyword/                                ",
    "Disallow:/u/    ",
    "Disallow:/univ/   ",
    "Disallow:/cobrand   ",
    "Disallow:/custom      ",
    "Disallow:/advanced_group_search",
    "Disallow:/googlesite  ",
    "Disallow:/preferences   ",
    "Disallow:/setprefs        ",
    "Disallow:/swr               ",
    "Disallow:/url                 ",
    "Disallow:/default               ",
    "Disallow:/m?                      ",
    "Disallow:/m/?                       ",
    "Disallow:/m/blogs?                    ",
    "Disallow:/m/directions?                 ",
    "Disallow:/m/ig                            ",
    "Disallow:/m/images?                         ",
    "Disallow:/m/local?                            ",
    "Disallow:/m/movies?                             ",
    "Disallow:/m/news?                                 ",
    "Disallow:/m/news/i?  ",
    "Disallow:/m/place?     ",
    "Disallow:/m/products?    ",
    "Disallow:/m/products/      ",
    "Disallow:/m/setnewsprefs?    ",
    "Disallow:/m/search?            ",
    "Disallow:/m/swmloptin?           ",
    "Disallow:/m/trends                 ",
    "Disallow:/m/video?                   ",
    "Disallow:/wml?                         ",
    "Disallow:/wml/?                          ",
    "Disallow:/wml/search?                      ",
    "Disallow:/xhtml?                             ",
    "Disallow:/xhtml/?      ",
    "Disallow:/xhtml/search?  ",
    "Disallow:/xml?             ",
    "Disallow:/imode?             ",
    "Disallow:/imode/?              ",
    "Disallow:/imode/search?          ",
    "Disallow:/jsky?                    ",
    "Disallow:/jsky/?                     ",
    "Disallow:/jsky/search?                 ",
    "Disallow:/pda?      ",
    "Disallow:/pda/?       ",
    "Disallow:/pda/search?   ",
    "Disallow:/sprint_xhtml    ",
    "Disallow:/sprint_wml        ",
    "Disallow:/pqa                 ",
    "Disallow:/palm                  ",
    "Disallow:/gwt/                    ",
    "Disallow:/purchases                 ",
    "Disallow:/hws                         ",
    "Disallow:/bsd?                          ",
    "Disallow:/linux?                          ",
    "Disallow:/mac?                              ",
    "Disallow:/microsoft? ",
    "Disallow:/unclesam?    ",
    "Disallow:/answers/search?q=",
    "Disallow:/local?             ",
    "Disallow:/local_url            ",
    "Disallow:/froogle?               ",
    "Disallow:/products?                ",
    "Disallow:/products/                  ",
    "Disallow:/froogle_                     ",
    "Disallow:/product_                       ",
    "Disallow:/products_                        ",
    "Disallow:/print                              ",
    "Disallow:/books   ",
    "Disallow:/bkshp?q=  ",
    "Allow:/booksrightsholders",
    "Disallow:/patents?         ",
    "Disallow:/patents/           ",
    "Allow:/patents/about           ",
    "Disallow:/scholar                ",
    "Disallow:/complete                 ",
    "Disallow:/sponsoredlinks             ",
    "Disallow:/videosearch?                 ",
    "Disallow:/videopreview?                  ",
    "Disallow:/videoprograminfo?                ",
    "Disallow:/maps?  ",
    "Disallow:/mapstt?  ",
    "Disallow:/mapslt?    ",
    "Disallow:/maps/stk/    ",
    "Disallow:/maps/br?       ",
    "Disallow:/mapabcpoi?       ",
    "Disallow:/maphp?             ",
    "Disallow:/places/              ",
    "Disallow:/maps/place             ",
    "Disallow:/help/maps/streetview/partners/welcome/",
    "Disallow:/lochp?",
    "Disallow:/center  ",
    "Disallow:/ie?       ",
    "Disallow:/sms/demo?   ",
    "Disallow:/katrina?      ",
    "Disallow:/blogsearch?     ",
    "Disallow:/blogsearch/       ",
    "Disallow:/blogsearch_feeds    ",
    "Disallow:/advanced_blog_search  ",
    "Disallow:/reader/                 ",
    "Allow:/reader/play                  ",
    "Disallow:/uds/                        ",
    "Disallow:/chart?                        ",
    "Disallow:/transit?                        ",
    "Disallow:/mbd?                              ",
    "Disallow:/extern_js/                          ",
    "Disallow:/calendar/feeds/ ",
    "Disallow:/calendar/ical/    ",
    "Disallow:/cl2/feeds/          ",
    "Disallow:/cl2/ical/             ",
    "Disallow:/coop/directory          ",
    "Disallow:/coop/manage               ",
    "Disallow:/trends?                     ",
    "Disallow:/trends/music?    ",
    "Disallow:/trends/hottrends?  ",
    "Disallow:/trends/viz?          ",
    "Disallow:/notebook/search?       ",
    "Disallow:/musica                   ",
    "Disallow:/musicad                    ",
    "Disallow:/musicas                      ",
    "Disallow:/musicl                         ",
    "Disallow:/musics                           ",
    "Disallow:/musicsearch                        ",
    "Disallow:/musicsp                              ",
    "Disallow:/musiclp                                ",
    "Disallow:/browsersync                              ",
    "Disallow:/call                                       ",
    "Disallow:/archivesearch?   ",
    "Disallow:/archivesearch/url  ",
    "Disallow:/archivesearch/advanced_search",
    "Disallow:/base/reportbadoffer",
    "Disallow:/urchin_test/",
    "Disallow:/movies?       ",
    "Disallow:/codesearch?     ",
    "Disallow:/codesearch/feeds/search?",
    "Disallow:/wapsearch?  ",
    "Disallow:/safebrowsing  ",
    "Allow:/safebrowsing/diagnostic",
    "Allow:/safebrowsing/report_error/",
    "Allow:/safebrowsing/report_phish/  ",
    "Disallow:/reviews/search?            ",
    "Disallow:/orkut/albums                 ",
    "Disallow:/jsapi                          ",
    "Disallow:/views?                           ",
    "Disallow:/c/                                 ",
    "Disallow:/cbk                                  ",
    "Disallow:/recharge/dashboard/car      ",
    "Disallow:/recharge/dashboard/static /   ",
    "Disallow:/translate_a/                    ",
    "Disallow:/translate_c                       ",
    "Disallow:/translate_f                         ",
    "Disallow:/translate_static/                     ",
    "Disallow:/translate_suggestion                    ",
    "Disallow:/profiles/me                               ",
    "Allow:/profiles         ",
    "Disallow:/s2/profiles/me  ",
    "Allow:/s2/profiles          ",
    "Allow:/s2/photos              ",
    "Allow:/s2/static                ",
    "Disallow:/s2                      ",
    "Disallow:/transconsole/portal/      ",
    "Disallow:/gcc/                        ",
    "Disallow:/aclk                          ",
    "Disallow:/cse?                            ",
    "Disallow:/cse/home                          ",
    "Disallow:/cse/panel ",
    "Disallow:/cse/manage  ",
    "Disallow:/tbproxy/      ",
    "Disallow:/comparisonads/  ",
    "Disallow:/imesync/          ",
    "Disallow:/shenghuo/search?    ",
    "Disallow:/support/forum/search? ",
    "Disallow:/reviews/polls/          ",
    "Disallow:/hosted/images/            ",
    "Disallow:/ppob/?                      ",
    "Disallow:/ppob?                         ",
    "Disallow:/ig/add?                         ",
    "Disallow:/adwordsresellers                  ",
    "Disallow:/accounts/o8                         ",
    "Allow:/accounts/o8/id   ",
    "Disallow:/topicsearch?q=  ",
    "Disallow:/xfx7/             ",
    "Disallow:/squared/api         ",
    "Disallow:/squared/search        ",
    "Disallow:/squared/table           ",
    "Disallow:/toolkit/                  ",
    "Allow:/toolkit/*.html                 ",
    "Disallow: /qnasearch?                   ",
    "Disallow: /errors/                        ",
    "Disallow: /app/updates                      ",
    "Disallow: /sidewiki/entry/                    ",
    "Disallow: /quality_form?                        ",
    "Disallow: /labs/popgadget/search                  ",
    "Disallow: /buzz/post       ",
    "Disallow: /compressiontest/  ",
    "Disallow: /analytics/reporting/",
    "Disallow: /analytics/admin/      ",
    "Disallow: /analytics/web/          ",
    "Disallow: /analytics/feeds/          ",
    "Disallow: /analytics/settings/         ",
    "Disallow: /alerts/                       ",
    "Allow: /alerts/manage"
    };


	/**
	 *
	 * @param out
	 * @return
	 */
    public boolean makeRobots(PrintWriter out)
    {

        java.util.Random r = new Random();


         int waitTime = r.nextInt(10); //(int)(Math.random()*10000);

        int runner = waitTime;

        out.println("User-agent: *<br>");

        while (runner <= m_robotBase.length - 1)
        {
            out.println(m_robotBase[runner] + "<br>");
            runner+=waitTime;

        }

    return true;
    }

	/**
	 *
	 * @param out
	 * @return
	 */
    public boolean makeRobots(StringBuilder out)
    {

        java.util.Random r = new Random();


         int waitTime = r.nextInt(10); //(int)(Math.random()*10000);

        int runner = waitTime;

        out.append("User-agent: *<br>");

        while (runner <= m_robotBase.length - 1)
        {
            out.append(m_robotBase[runner] + "<br>");
            runner+=waitTime;

        }

    return true;
    }

}
