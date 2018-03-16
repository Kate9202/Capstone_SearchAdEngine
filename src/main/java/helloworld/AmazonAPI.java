package helloworld;

import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import com.sun.xml.internal.bind.v2.TODO;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class AmazonAPI {
    private static final String ACCESS_KEY_ID = "AKIAJSDJB3FRBHUJLU2Q";
    private static final String SECRET_KEY = "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";
    private static final String ENDPOINT = "webservices.amazon.com";

    public String callAmazonAPI() {
        /*
         * Set up the signed requests helper.
         */
        SignedRequestsHelper helper;

        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, ACCESS_KEY_ID, SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return " ";
        }

        String requestUrl = null;

        Map<String, String> params = new HashMap<String, String>();

        params.put("Service", "AWSECommerceService");
        params.put("Operation", "ItemSearch");
        params.put("AWSAccessKeyId", "AKIAJSDJB3FRBHUJLU2Q");
        params.put("AssociateTag", "yfan0a-20");
        params.put("SearchIndex", "All");
        params.put("Keywords", "nike");
        params.put("ResponseGroup", "Images,ItemAttributes,Offers");
        //TODO: change to config file to protect API keys
        requestUrl = helper.sign(params);

//        System.out.println("Signed URL: \"" + requestUrl + "\"");
        return requestUrl;
    }

}