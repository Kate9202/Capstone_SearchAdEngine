package helloworld.crawler;


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import helloworld.ad.Ads;

import java.io.*;
import java.util.List;
//TODO: import is much less than original file, no modification on xml file. What might be the reason?

public class WebCrawler {
    public static void main(String[] args) throws IOException{ //TODO: what is IOException?
        if (args.length < 2){
            System.out.println("Usage: Crawler <rawQueryDataFilePath> <adsDataFilePath> <proxyFilePath> <logFilePath>");
            System.exit(0);
        } //TODO: what is this part for?

        ObjectMapper mapper = new ObjectMapper();
        //TODO: what is ObjectMapper?
        String rawQueryDataFilePath = args[0];
        String adsDataFilePath = args[1];
        String proxyFilePath = args[2];
        String logFilePath = args[3];
        AmazonCrawler crawler = new AmazonCrawler(proxyFilePath, logFilePath); //logFile saves all the exceptions

        //Part 1: create a file to store ads data.
        File file = new File(adsDataFilePath);
        if (!file.exists()){
            file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile()); //TODO: what does this line do?
        BufferedWriter bw = new BufferedWriter(fw);//TODO: what does this line do?

        try(BufferedReader br = new BufferedReader(new FileReader(rawQueryDataFilePath))){

            String line;
            while ((line = br.readLine()) != null){
                if ((line.isEmpty()))
                    continue;
                System.out.println(line);
                String[] fields  = line.split(",");//split every row in feed file
                String query = fields[0].trim();
                double bidPrice = Double.parseDouble(fields[1].trim());
                int campaignId = Integer.parseInt(fields[2].trim());
                int queryGroupId = Integer.parseInt(fields[3].trim());
                int startIndex = 0;

                //Save data from crawler in ads
                for (Integer pageNum = 1; pageNum <= 10; pageNum++){
                    List<Ads> ads = crawler.GetAdBasicInfoByQuery(query, bidPrice, campaignId, queryGroupId, pageNum, startIndex);
                    for (Ads ad: ads) {
                        String jsonInString = mapper.writeValueAsString(ads);
//                        System.out.println(jsonInString);
                        bw.write(jsonInString);
                        bw.newLine();
                    }
                    startIndex = startIndex + ads.size();
                    Thread.sleep(4000); //Make the crawler sleep for 4s to get rid of banning
                }
            }
            bw.close();
        } catch (InterruptedException ex){
            Thread.currentThread().interrupt();
        } catch (JsonGenerationException e){
            e.printStackTrace();
        } catch (JsonMappingException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }//TODO: what does every exception mean?

        crawler.cleanup();
    }

}
