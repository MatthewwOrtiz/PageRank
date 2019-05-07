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
//        	System.out.print('.');
//        	System.out.println(inOut.size());
        	if (inOut.size() == 50) {
        		return;
        	}
            if (!url.contains("https://gameofthrones.fandom.com/wiki/")) {
                String temp = "https://gameofthrones.fandom.com".concat(url);
                url = temp;
            }
            if (inOut.containsKey(url)) {
                inOut.get(url).inlinks++;
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
//            	System.out.println("Help me");
                if (links.get(i) != null) {
                    urlHref = links.get(i).attr("href");
                    if(urlHref.contains("/wiki/") 
                            && !urlHref.contains("?") 
                            && !urlHref.substring(6).contains(":")
                            && !urlHref.contains("wikipedia")
                            && !urlHref.contains("wikia")) {
                        
                        inOut.get(url).outlinks++;
                        if (!inOut.get(url).linksTo.containsKey(urlHref)) {
                        	//System.out.println(urlHref);
                            inOut.get(url).linksTo.put(urlHref, 1);
                        } else {
                            int l = inOut.get(url).linksTo.get(urlHref);
                            inOut.get(url).linksTo.replace(urlHref, l, l + 1);
                        }
                        
                        
                        crawl(urlHref, url);
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
        page[] ranks = new page[s];
        double[] ranksOld = new double[s];
        int ind = 0;
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            page p = (page) pair.getValue();
            if (p.outlinks != 0) {
                p.pageRank = (double) 1 / s;
                ranks[ind] = p;
            } else {
                it.remove();
            }
            ind++;
//            System.out.println(p.name + " " + p.inlinks + " " + p.outlinks + " " + p.index);
//            System.out.print("init pageRank ");
//            System.out.println(1.0/s); // initial page rank
        }
//        ranksOld = Arrays.copyOf(ranks, s);
        for (int i = 0; i < s; i++) {
        	ranksOld[i] = ranks[i].pageRank;
        }
        
        boolean loop = true;
        int iters = 0;

        System.out.println();
        while (loop) {
//        	System.out.println("I mean it");
//        	System.out.print('.');
        	int ctr = 0;
        	iters++;
	        for (int i = 0; i < s-1; i++) {
//	        	System.out.print(ranks[i].pageRank + " ");
	        	ranks[i].pageRank = 0;
	            for (int j = 0; j < s-1; j++) {
	            	if (i != j && ranks[i].linkedFrom.contains(ranks[j].name)) {
	            		ranks[i].pageRank += ranks[j].pageRank / ranks[j].outlinks;
	            
//	            		System.out.println(ranks[j].pageRank + " " + ranks[j].outlinks);
	            	}
	            	
	            }
//	            System.out.println(ranks[i].pageRank);
	        }
//	        System.out.println();
	        for (int i = 0; i < s; i++) {
	        	if (Math.abs(ranks[i].pageRank - ranksOld[i]) <= .001) {
//	        		System.out.print(ranks[i].pageRank + " ");
//	        		System.out.println(ranksOld[i]);
	        		ctr++;
	        		if (ctr == s) {
	        			loop = false;
	        			break;
	        		}
	        	}
	        }
	        for (int i = 0; i < s; i++) {
	        	ranksOld[i] = ranks[i].pageRank;
	        }
	        if (iters == 10) {
	        	loop = false;
	        }
        }
        System.out.println(iters);
        for (int i = 0; i< s-1; i ++) {
        	System.out.println(ranks[i].pageRank + " " + ranks[i].name);
        }

    }

    private static class page {
        String name;
        int inlinks, outlinks; //number of inlinks and outlinks
        double pageRank; //Current pageRank for a given Page
        HashMap<String, Integer> linksTo; //What pages the current page links to and how many times
        LinkedList<String> linkedFrom;
        void print(){
    	   int o =0;
        for (String n: linksTo.keySet()){
            String key = n.toString();
            String value = linksTo.get(n).toString();  
            System.out.print(key + " " + value + " "); 
            System.out.println((float)linksTo.get(n)/outlinks);
    
            if (o%linksTo.size()==0){
            	//System.out.println();
            }
        }
       	}
        
        public page(String s) {
            name = s;
            inlinks = 1;
            linksTo = new HashMap<>();
            linkedFrom = new LinkedList<>();
        }
    }
}