import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.*;
import java.util.*;
import java.util.LinkedList;
import org.jsoup.Connection;

public class driver {
    /**
     * @param args the command line arguments
     */
    static boolean printRepos = false;
    static HashMap<String, page> inOut;
    static LinkedList<page> ranks;
    
    public static void main(String[] args) {
        inOut = new HashMap<>();
        ranks = new LinkedList<>();
        String url = "https://gameofthrones.fandom.com/wiki/Game_of_Thrones_(TV_series)";
        crawl(url);
        initRanks();
    }
    
    private static void crawl(String url) {
        try {
            if (!url.contains("https://gameofthrones.fandom.com/wiki/")) {
                String temp = "https://gameofthrones.fandom.com".concat(url);
                url = temp;
            }
            if (inOut.containsKey(url)) {
                inOut.get(url).inlinks++;
                return;
            } else {
                inOut.put(url, new page(url));
            }
            Connection con = Jsoup.connect(url);            
            Document doc = con.get();

            System.out.println(url);
          
            if(printRepos) {
                String fileName = url.substring(8).replace('/', '_').replaceAll("[?&]", "");
                if(fileName.length() < 2) {
                    fileName = "GoTWiki";
                }
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
                            && !urlHref.contains("wikipedia")
                            && !urlHref.contains("wikia")) {
                        
                        inOut.get(url).outlinks++;
                        if (!inOut.get(url).linksTo.containsKey(urlHref)) {
                            inOut.get(url).linksTo.put(urlHref, 1);
                        } else {
                            int l = inOut.get(url).linksTo.get(urlHref);
                            inOut.get(url).linksTo.replace(urlHref, l, l + 1);
                        }
                        
                        crawl(urlHref);
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Bad URL: " + e.getMessage() + " " + url);
        } catch (IOException e) {
            System.out.println("Unable to Create Connection " + e.getMessage() + " " + url);
        } catch (StackOverflowError e) {
            System.out.println("Stack Overflow");
        }
    }
    
    private static void initRanks() {
        int s = inOut.size();
        Iterator it = inOut.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            page p = (page) pair.getValue();
            if (p.outlinks != 0) {
                p.pageRank = 1 / s;
                ranks.add(p);
            } else {
                it.remove();
            }
        }
    }

    private static class page {
        String name;
        int inlinks, outlinks;//number of inlinks and outlinks
        float pageRank;//Current pageRank for a given Page
        HashMap<String, Integer> linksTo;//What pages the current page links to and how many times

        public page(String s) {
            name = s;
            inlinks = 1;
            linksTo = new HashMap<>();
        }
    }
}