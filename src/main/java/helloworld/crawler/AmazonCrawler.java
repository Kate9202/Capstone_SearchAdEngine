package helloworld.crawler;

import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.aspectj.weaver.bcel.Utility;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import helloworld.ad.Ads;



public class AmazonCrawler {
    /*
    Searching result page
    [Original url]
    https://www.amazon.com/s/ref=nb_sb_noss_1?url=search-alias%3Daps&field-keywords=fugees&rh=i%3Aaps%2Ck%3Afugees
    [Cleaned-up url]
    https://www.amazon.com/s/page=2&keywords=first+aid
    https://www.amazon.com/s/field-keywords=nike
    */

    private static final String AMAZON_QUERY_URL = " https://www.amazon.com/s/ref=sr_pg_2?page=2&keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/65.0.3325.146 Safari/537.36";
    //User agent extracted from: http://www.toolsvoid.com/what-is-my-ip-address
    private final String authUser = "bittiger";
    private final String authPassword = "cs504"; //TODO: consider load a config file instead of hard code
    private List<String> proxyList;
    private List<String> titleList;
    private List<String> categoryList;
    private List<String> detailUrlList;

    private HashSet crawledUrl;
    private int adId;

    BufferedWriter logBFWriter;

    private int index = 0;

    public AmazonCrawler(String proxy_file, String log_file){
        crawledUrl = new HashSet();//TODO: why initiate a new hashset
        adId = 5000;
        initProxyList(proxy_file);
        initHtmlSelector();
        initLog(log_file);
    }

    public void cleanup(){
        //This close the log file
        if (logBFWriter != null){
            try {
                logBFWriter.close();
            } catch (IOException e) { //TODO: what is IOException?
                e.printStackTrace();
            }
        }
    }

    /*
    Detail product page
    [raw url]
    https://www.amazon.com/KNEX-Model-Building-Set-Engineering/dp/B00HROBJXY/ref=sr_1_14/132-5596910-9772831?ie=UTF8&qid=1493512593&sr=8-14&keywords=building+toys
    [normalizedUrl]
    https://www.amazon.com/KNEX-Model-Building-Set-Engineering/dp/B00HROBJXY
    */

    private String normalizeUrl(String url){
        //Clean up the url for detail product page
        int i = url.indexOf("ref");
        String normalizedUrl = url.substring(0, i-1);//trim original url
        return normalizedUrl;
    }

    private void initProxyList(String proxy_file){
        proxyList = new ArrayList<String>();//TODO: do i still initialize this if initialized before?
        try (BufferedReader br = new BufferedReader(new FileReader(proxy_file))){
            String line;
            while ((line = br.readLine())!= null){
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e){
            e.printStackTrace();
        }
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                authUser, authPassword.toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyUser", authUser);
        System.setProperty("http.proxyPassword", authPassword);
        System.setProperty("socksProxyPort","61336");//set proxy port
    }

    private void initHtmlSelector(){//TODO: problem here.
        titleList = new ArrayList<String>();
        //titleList - eye cream, nike running shoe, first aid, fugees,
        //> div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
        //> div > div.a-row.a-spacing-none.s-color-subdued > div.a-row.a-spacing-micro > a > h2
        //> div > div:nth-child(3) > div.a-row.a-spacing-none.a-spacing-top-mini > a > h2 --> eye cream
        //> div > div.a-row.a-spacing-none.s-color-subdued > div.a-row.a-spacing-micro > a > h2
        //> div > div.a-fixed-left-grid > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div.a-row.a-spacing-none.scx-truncate-medium.sx-line-clamp-2 > a > h2

        //in examples
        //> div > div:nth-child(3) > div.a-row.a-spacing-top-mini > a > h2
        //> div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1)  > a > h2
        //> div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a > h2



        categoryList = new ArrayList<String>();
        //categoryList



        detailUrlList = new ArrayList<String>();
        //detailUrlList

    }

    private void initLog(String log_path){
        try {
            File log = new File(log_path);
            if (!log.exists(){
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            logBFWriter = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setProxy(){
        //Rotate proxy in the list, round robbin
        if (index == proxyList.size()){
            index = 0;
            //TODO: why use round robbin instead of normal for loop?
            //Answer: the section only need one output every time the method is called. [??]
        }
        String proxy = proxyList.get(index);
        System.setProperty("socksProxyHost", proxy);
        index++;
    }

    private void testProxy(){
        System.setProperty("socksProxyHost", "173.208.78.34"); //set proxy server
        System.setProperty("socksProxyPort", "61336"); //set proxy port
        String test_url =  "http://www.toolsvoid.com/what-is-my-ip-address";
        try {
            Document doc = Jsoup.connect(test_url).userAgent(USER_AGENT).timeout(10000).get();
            String iP = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").first().text();
            System.out.println("IP-Address: " + iP);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Ads> GetAdBasicInfoByQuery(String query, double bidPrice, int campaignId, int queryGroupId, Integer pageNum, int startIndex){
        //TODO: why does 'pageNum' use Integer instead of int
        List<Ads> products = new ArrayList<>();
        try {
            if (false){
                testProxy();
                return products;
            }
            //TODO: what does this block do?
            setProxy();//rotate proxy everytime

            //Step 1: construct url
            String url = AMAZON_QUERY_URL + query;
            if (pageNum > 1) {
                url = url + "&page=" + pageNum.toString();
            }//TODO:consider make page parameter for everyone cause page = 1 also works?
            System.out.println("request_url = " + url);

            //Step 2:
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");//TODO: what does this mean?
            headers.put("Accept-Language", "en-US, en; q=0.8");
            Document doc = Jsoup.connect(url).header(headers).userAgent(USER_AGENT).timeout(100000).get();

            Elements results = doc.select("li[data-asin]");
            if (results.size() == 0){
                logBFWriter.write("0 result for query: " + query + ", pageNum " + pageNum.toString());
                logBFWriter.newLine();
            }

            System.out.println("number of results = " + results.size());
            Elements prods = doc.select("a[title][href]");
            System.out.println("number of results from dom = "+ prods.size());

            for (int i = 0; i < results.size(); i++){
                Ads ad = new Ads();
                int index = startIndex + i;

                //CSS path
                //detail url
                //#result_19 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a
                //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a
                //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a
                //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a
                //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > a

                boolean crawled  = false;
                for (String detail_path: detailUrlList) {
                    detail_path = "#result_" + Integer.toString(index) + detail_path;
                    Element detail_url_ele = doc.select(detail_path).first();
                    if (detail_url_ele != null){
                        String detail_url = detail_url_ele.attr("href");
                        String normalizedUrl = normalizeUrl(detail_url);
                        if (crawledUrl.contains(normalizedUrl)){
                            logBFWriter.write("crawled url" + normalizedUrl);
                            logBFWriter.newLine();
                            crawled = true;
                            break;
                        }
                        crawledUrl.add(normalizedUrl);
                        System.out.println("normalized url = " + normalizedUrl);
                        ad.detail_url = normalizedUrl;
                        break;
                    }
                }

                if (crawled){
                    continue;
                }

                if (ad.detail_url == null || ad.detail_url == ""){
                    logBFWriter.write("cannot parse detail for query: " + query);
                    logBFWriter.newLine();
                    System.out.println("Cannot parse detail for query: " + query);
                    continue;
                }
                ad.query = query;
                ad.query_group_id = queryGroupId;

                //title
                //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
                //#result_3 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
                //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
                //#result_1 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div:nth-child(1) > a > h2
                for (String title: titleList){
                    String title_ele_path = "#result_" + Integer.toString(index) + title;
                    Element title_ele = doc.select(title_ele_path).first();
                    if (title_ele != null) {
                        System.out.println("title = " + title_ele.text());
                        ad.title = title_ele.text();
                        break;
                    }
                }

                if (ad.title == null || ad.title == ""){
                    logBFWriter.write("cannot parse title for query: " + query);
                    logBFWriter.newLine();
                    continue;
                }

                ad.keyWords = Utility.cleanedTokenize(ad.title); //change title to keyWords
                //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img

                //thumbnail
                String thumbnail_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a > img";
                Element thumbnail_ele = doc.select(thumbnail_path).first();
                if (thumbnail_ele != null) {
                    //System.out.println("thumbnail = " + thumbnail_ele.attr("src"));
                    ad.thumbnail = thumbnail_ele.attr("src");
                } else {
                    logBFWriter.write("cannot parse thumbnail for query:" + query + ", title: " + ad.title);
                    logBFWriter.newLine();
                    continue;
                }

                //brand
                String brand_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div.a-row.a-spacing-small > div > span:nth-child(2)";
                Element brand = doc.select(brand_path).first();
                if (brand != null) {
                    //System.out.println("brand = " + brand.text());
                    ad.brand = brand.text();
                }
                //#result_2 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span
                ad.bidPrice = bidPrice;
                ad.campaignId = campaignId;
                ad.price = 0.0;
                //#result_0 > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span

                //price
                String price_whole_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > span";
                String price_fraction_path = "#result_" + Integer.toString(index) + " > div > div > div > div.a-fixed-left-grid-col.a-col-right > div:nth-child(3) > div.a-column.a-span7 > div.a-row.a-spacing-none > a > span > span > sup.sx-price-fractional";
                Element price_whole_ele = doc.select(price_whole_path).first();
                if (price_whole_ele != null) {
                    String price_whole = price_whole_ele.text();
                    //System.out.println("price whole = " + price_whole);
                    //remove ","
                    //1,000
                    if (price_whole.contains(",")) {
                        price_whole = price_whole.replaceAll(",", "");
                    }

                    try {
                        ad.price = Double.parseDouble(price_whole); //can also try "off-screen"?
                    } catch (NumberFormatException ne) {
                        // TODO Auto-generated catch block
                        ne.printStackTrace();
                        //log
                    }
                }

                Element price_fraction_ele = doc.select(price_fraction_path).first();
                if (price_fraction_ele != null) {
                    //System.out.println("price fraction = " + price_fraction_ele.text());
                    try {
                        ad.price = ad.price + Double.parseDouble(price_fraction_ele.text()) / 100.0;
                    } catch (NumberFormatException ne) {
                        ne.printStackTrace();
                    }
                }
                //System.out.println("price = " + ad.price );

                //category
                for (String category : categoryList) {
                    Element category_ele = doc.select(category).first();
                    if (category_ele != null) {
                        //System.out.println("category = " + category_ele.text());
                        ad.category = category_ele.text();
                        break;
                    }
                }
                if (ad.category == "") {
                    logBFWriter.write("cannot parse category for query:" + query + ", title: " + ad.title);
                    logBFWriter.newLine();
                    continue;
                }
                ad.adId = adId;
                adId++;
                products.add(ad);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return products;
    }
}