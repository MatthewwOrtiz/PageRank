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
        String url = "https://old.reddit.com/r/MuseumOfReddit/";
        HashSet<String> visited = new HashSet<>();
        crawl(url, visited);
    }
    
    private static void crawl(String url, HashSet<String> visited) {
        try {
            while (url.contains("comments") && url.length() > 55) {
                url = url.substring(0, url.lastIndexOf('/'));
            }
            if (visited.contains(url)) {
                return;
            } else {
                visited.add(url);
            }
            
            Connection con = Jsoup.connect(url);
            
            //delay the crawl
            Thread.sleep(3000);
            
            Document doc = con.get();
            String fileName = url.substring(8).replace('/', '_').replaceAll("[?&]", "");
            if(fileName.length() < 2) {
                    fileName = "Reddit";
            }
            
            System.out.println(url);
          
            if(printRepos) {
            new File("repository").mkdir();
           PrintWriter pw = new PrintWriter("repository/" + fileName + ".html");

           pw.print(doc);
           pw.close();
            }
            
            Elements links = doc.select("[href*=https://old.reddit.com/r/MuseumOfReddit/comments/]");
            Elements nextPage = doc.select("[href][rel=nofollow next]");
            if (nextPage != null) {
                links.add(nextPage.first());
            }
            
            
            Elements comments = doc.select("a[href]");
           
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < comments.size(); i++) {
                String s = comments.get(i).attr("href");
                if (s.length() > 0 && s.contains("/user/")) {
                	s = s.replace("https://old.reddit.com","   ");
                	System.out.println(s);
                	//sb.append(s + ',');
                }
                if (s.length() > 0 && s.contains("/u/")) {
                	System.out.println(s);
                	//sb.append(s + ',');
                }
                
            }
            
            if(printComments) 
            {
                new File("comments").mkdir();
	            if (sb.length() > 0) {
	              PrintWriter pw2 = new PrintWriter("comments/" + fileName + ".html");
	               pw2.print(sb.toString());
	                pw2.close();
	            }
            }
            
            
            String urlHref;
            for (int i = 0; i < links.size(); i++) {
                if (links.get(i) != null) {
                    urlHref = links.get(i).attr("href").toLowerCase().replace("www", "old");
                    if(urlHref.contains("http") && urlHref.contains("museumofreddit") && !urlHref.contains(".rss")) {
                        crawl(urlHref, visited);
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Bad URL: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Unable to Create Connection " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }
    
}