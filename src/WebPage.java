// Wykonali Michał Gregorczyk oraz Maksymilian Górka

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;

public class WebPage {
    // Map to save our sties with keywords
    static Map <String,String> sitesWithKeywords = new TreeMap<>();
    // List to save our visited sites
    static List<String> visitedSites = new ArrayList<String>();
    // List of all links in our main page
    static List<String> sites = new ArrayList<String>();

    public static void main(String[] args)  {
        // Read and save whole site in Document object
        Document doc = readSite("https://moodle1.up.krakow.pl");

        // Find keywords meta tag in Document head
        String keywords = findKeywords(doc.head());
        // Save correct results in sitesWithKeywords Map and ignore sites without keywords
        if(!keywords.isEmpty()) {
            sitesWithKeywords.put(doc.location(),keywords);
        }

        // Find other sites
        findDifferentSite(doc.body());

        // Search on linked pages
        deepSearch();
        System.out.println("Wszystkie podstrony (krok: 0): " + sites.size());

        int counter = 0;
        while (counter < 1) {
            // Search on pages visited on linked pages
            visitedSearch();
            System.out.println("Wszystkie podstrony (krok: "+(counter+1)+"): " + sites.size());
            counter++;
        }



        // Print all sites with keywords
        System.out.println("Lista stron z keywordami:");
        Set<Map.Entry<String,String>> entrySet = sitesWithKeywords.entrySet();
        for (Map.Entry<String,String> entry: entrySet) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        // Print all visited sites
        System.out.println("Lista odwiedzonych stron:");
        for (String site : visitedSites) {
            System.out.println(site);
        }

    }

    // IN: Url to specific site                 OUT: Document object
    public static Document readSite(String url) {
        Document document = null;
        try {
            // Connect to specific site
            document = Jsoup.connect(url).get();
            // Add site to List if is not already in visitedSites
            if (!visitedSites.contains(url)){
                visitedSites.add(url);
            }
        } catch (IOException e) { }

        return document;
    }
    // IN: Element head (html of head)          OUT: Content of keyword tag (if exist) or empty string (if keywords not specified)
    public static String findKeywords(Element head) {
        // Get every meta tag into metaTags
        Elements metaTags = head.getElementsByTag("meta");

        // For each metaTag in metaTags
        for (Element metaTag : metaTags) {
            // If name of this tag is "keywords"
            if (metaTag.attr("name").equals("keywords")) {
               return metaTag.attr("content");
            }
        }
        return "";
    }
    // IN: Element body (html of body)
    public static void findDifferentSite(Element body) {
        // Get every tag <a> into links
        Elements links = body.getElementsByTag("a");
        // For each tag <a> in links
        for (Element link : links) {
            // If <a> have attribute "href"
            if (!link.attr("href").isEmpty()){
                // If <a> starts with character 'h' (http/https and ignore #)
                if (link.attr("href").charAt(0) == 'h') {
                    // Try to build correct Url for Jsoup (also ignore subpages / only host)
                    String host = null;
                    String protocol = null;
                    String wholeUrl = null;
                    try {
                        host = new URL(link.attr("href")).getHost();
                        protocol = new URL(link.attr("href")).getProtocol();
                        wholeUrl = protocol+"://"+host;
                    } catch (MalformedURLException e) { }

                    // If site is not in List sites > add it
                    if (!sites.contains(wholeUrl)) {
                        sites.add(wholeUrl);
                    }
                }
            }
        }
    }
    // Search in sites List (skip visited)
    public static void deepSearch() {
        // Search on every other site
        for (String site : sites) {
            // Only unique sites
            if (!visitedSites.contains(site)) {
                Document doc = readSite(site);
                // If connection was established and Document saved
                if (doc != null) {
                    // Search for keyword tag and its content
                    String keywords = findKeywords(doc.head());
                    // If not empty (look at function itself) add to Map sitesWithKeywords
                    if (!keywords.isEmpty()) {
                        sitesWithKeywords.put(site, keywords);
                    }
                }
            }
        }
    }
    // Search in depth (generate linked sites of every visited site)
    public static void visitedSearch() {
        // For each visited site
        for (String site : visitedSites) {
            // Try to establish connection and get Document
            Document doc = readSite(site);
            if (doc != null) {
                // Get keywords if they exist and add to Map
                String keywords = findKeywords(doc.head());
                // Find all linked sites on this particular visited site
                findDifferentSite(doc.body());
                if (!keywords.isEmpty()) {
                    sitesWithKeywords.put(site, keywords);
                }
            }
        }
        // Call deepSearch again because most likely new sites were added to List sites
        deepSearch();
    }

}

