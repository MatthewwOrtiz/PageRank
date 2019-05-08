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
    //static LinkedList<page> ranks;
    
    public static void main(String[] args) {
        inOut = new HashMap<>();
//        ranks = new LinkedList<>();
        String url = "https://gameofthrones.fandom.com/wiki/Game_of_Thrones_(TV_series)";
        crawl(url, url);
        initRanks();
    }
    
    private static void crawl(String url, String from) {
        try {
            if (inOut.size() == 2250) {
                return;
            }
            if (!url.contains("https://gameofthrones.fandom.com/wiki/")) {
                String temp = "https://gameofthrones.fandom.com".concat(url);
                url = temp;
            }
            if (inOut.containsKey(url)) {
//                inOut.get(url).inlinks++;
                inOut.get(url).linkedFrom.add(from);
                return;
            } else {
                inOut.put(url, new page(url));
            }
            Connection con = Jsoup.connect(url);            
            Document doc = con.get();

//            System.out.println(url);
          
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
                            && !urlHref.contains("wikia")
                            && !urlHref.contains("wiktionary")
                            && !urlHref.contains("fandom")
                            && !urlHref.contains("House_Martel")) {
                        
                        inOut.get(url).outlinks++;
                        
                        crawl(urlHref, url);
                    }
                }
            }
        } catch (MalformedURLException e) {
            System.out.println("Bad URL: " + e.getMessage() + " " + url);
//            inOut.remove(url);
        } catch (IOException e) {
            System.out.println("Unable to Create Connection " + e.getMessage() + " " + url);
//            inOut.remove(url);
        } catch (StackOverflowError e) {
            System.out.println("Stack Overflow");
        }
    }
    
    private static void initRanks() {
        int s = inOut.size();
        Iterator it = inOut.entrySet().iterator();
        page[] ranks = new page[s];
        double[] ranksOld = new double[s];
        int ind = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            page p = (page) pair.getValue();
            if (p.outlinks != 0) {
                p.pageRank = (double) 1 / s;
                ranks[ind] = p;
                ind++;
            } else {
                it.remove();
            }
        }
        for (int i = 0; i < s-1; i++) {
            if (ranks[i] != null) {
                ranksOld[i] = ranks[i].pageRank;
            } else {
                System.out.println(i);
            }
        }
        
        boolean loop = true;
        int iters = 0;

        System.out.println();
        while (loop) {
            int ctr = 0;
            iters++;
            for (int i = 0; i < s-1; i++) {
                if (ranks[i] != null) {
//	        	System.out.print(ranks[i].pageRank + " ");
                    ranks[i].pageRank = 0;
	            for (int j = 0; j < s-1; j++) {
	            	if (i != j && ranks[j] != null && ranks[i].linkedFrom.contains(ranks[j].name)) {
                            ranks[i].pageRank += ranks[j].pageRank / ranks[j].outlinks;
	            
//                          System.out.println(ranks[j].pageRank + " " + ranks[j].outlinks);
	            	}
	            	
	            }
                }
//	            System.out.println(ranks[i].pageRank);
            }
//	        System.out.println();
            for (int i = 0; i < s-1; i++) {
                
                if (ranks[i] != null && Math.abs(ranks[i].pageRank - ranksOld[i]) <= Math.pow(10, -10)) {
//	        		System.out.print(ranks[i].pageRank + " ");
//	        		System.out.println(ranksOld[i]);
                    ctr++;
                    if (ctr == s-1) {
                        loop = false;
                        break;
                    }
                }
            }
            for (int i = 0; i < s-1; i++) {
                if (ranks[i] != null){
                    ranksOld[i] = ranks[i].pageRank;
                }
            }
            if (iters == 100) {
                loop = false;
            }
        }
        System.out.println(iters);
        Arrays.sort(ranks);
        for (int i = 0; i< 100; i ++) {
            if (ranks[i] != null) {
                System.out.println(ranks[i].pageRank + " " + ranks[i].name);
            }
        }

    }

    private static class page implements Comparable<page>{
        String name;
        int outlinks; //number of outlinks
        double pageRank; //Current pageRank for a given Page
        LinkedList<String> linkedFrom;
        
        public page(String s) {
            name = s;
            linkedFrom = new LinkedList<>();
        }

        @Override
        public  int compareTo(page o) {
            return Double.compare(o.pageRank, pageRank);
        }
    }
}