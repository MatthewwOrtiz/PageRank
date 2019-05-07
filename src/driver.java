import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.*;
import java.util.HashSet;
import java.util.regex.Pattern;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;

/**
 *
 * @author mwono
 */

public class driver {
    /**
     * @param args the command line arguments
     */
	static boolean printRepos = false;
	static boolean printComments = false;
	
	
	
	
    public static void main(String[] args) {
        String url = "https://gameofthrones.fandom.com/wiki/Game_of_Thrones_(TV_series)";
        HashSet<String> visited = new HashSet<>();
        crawl(url, visited);
    }
    
    private static void crawl(String url, HashSet<String> visited) {
        try {
            String page = url.replace("https://gameofthrones.fandom.com/wiki/", "");
            if (visited.contains(page)) {
                return;
            } else {
                visited.add(page);
            }
            if (!url.contains("https://gameofthrones.fandom.com/wiki/")) {
                String temp = "https://gameofthrones.fandom.com".concat(url);
                url = temp;
            }
            Connection con = Jsoup.connect(url);
            
            Document doc = con.get();
            String fileName = url.substring(8).replace('/', '_').replaceAll("[?&]", "");
            if(fileName.length() < 2) {
                    fileName = "GoTWiki";
            }
            
            System.out.println(url);
          
            if(printRepos) {
            new File("repository").mkdir();
                PrintWriter pw = new PrintWriter("repository/" + fileName + ".html");

                pw.print(doc);
                pw.close();
            }

            Elements links = doc.select("div#WikiaArticle").first().select("[href]");
            
            String urlHref;
            for (int i = 0; i < links.size(); i++) {
                if (links.get(i) != null) {
                    urlHref = links.get(i).attr("href");
                    if(urlHref.contains("/wiki/") 
                            && !urlHref.contains("?") 
                            && !urlHref.substring(6).contains(":")
                            && !urlHref.contains("wikipedia")) {
                        crawl(urlHref, visited);
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Bad URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Unable to Create Connection " + e.getMessage());
        }
    }
    
}