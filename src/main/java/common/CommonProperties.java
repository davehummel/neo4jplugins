package common;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class CommonProperties {

    private static final HashMap<Integer,Integer> MOD_DESCISIONS = new HashMap<>();


    private static int MOD_DESC_COUNTER = 0;

    public static final String ACCOUNT_ID = "act_id";

    public static final String MERCHANT_ID = "mrc_id";
    public static final String MERCHANT_NAME = "name";
    public static final String MERCHANT_GROUP_ID = "mg_id";

    public static final String BRAND_NAME = "name";

    public static final String PRODUCT_PAGE_ID = "pg_id";
    public static final String PRODUCT_MODEL_ID = "mdl_id";
    public static final String PRODUCT_ID = "prd_id";
    public static final String PRODUCT_URL = "url";
    public static final String PRODUCT_IMG_URL = "img";
    public static final String PRODUCT_NAME = "name";
    public static final String PRODUCT_LOCALE = "locale";
    public static final String PRODUCT_VARIANT = "variant";
    public static final String PRODUCT_DESC = "desc";
    public static final String PRODUCT_FIRST_DATE = "date";
    public static final String PRODUCT_UPDATED_DATE = "updated";
    public static final String PRODUCT_PRICE = "price";
    public static final String PRODUCT_INSTOCK = "stock";
    public static final String PRODUCT_UPC = "upc";
    public static final String PRODUCT_DISCONTINUED = "discon";
    public static final String CATEGORY_NAME = "name";

    public static final String REVIEW_ID = "id";
    public static final String REVIEW_MOD_DESC = "mod";
    public static final String REVIEW_SOURCE = "src";
    public static final String REVIEW_DATE = "src";
    public static final String REVIEW_TYPE = "src";



    static public void setAccountProperties(Node node, long accountID) throws MissingReqPropException{
        if (accountID==0)
            throw new MissingReqPropException("Account ID");
        node.setProperty(ACCOUNT_ID,accountID);
    }

    static public void setBrandProperties(Node node, String brandName) throws MissingReqPropException {
        if (brandName == null || brandName.trim().isEmpty())
            throw new MissingReqPropException("Brand Name");
        node.setProperty(BRAND_NAME,brandName);
    }

    static public void setMerchantProperties(Node node, long merchantID, long merchantGroupID) throws MissingReqPropException {
        if (merchantID==0)
            throw new MissingReqPropException("Merchant ID");
//        if (merchantName == null || merchantName.trim().isEmpty())
//            throw new MissingReqPropException("Merchant Name");
        if (merchantGroupID == 0)
            throw new MissingReqPropException("Merchant Group ID");
        node.setProperty(MERCHANT_ID,merchantID);
//        node.setProperty(MERCHANT_NAME,merchantName);
        node.setProperty(MERCHANT_GROUP_ID,merchantGroupID);

    }

    static public void  setBaseProductProperties(Node node, long productID, String productName, String pageID, String variant, String locale, String description, String url, String img, boolean discontinued, long date) throws MissingReqPropException {
        if (productID==0)
            throw new MissingReqPropException("Product ID");
        if (productName == null || productName.trim().isEmpty())
            throw new MissingReqPropException("Product Name");
        if (pageID == null || pageID.trim().isEmpty())
            throw new MissingReqPropException("Page ID");

        node.setProperty(PRODUCT_PAGE_ID,pageID.toLowerCase());
        node.setProperty(PRODUCT_ID,productID);
        node.setProperty(PRODUCT_NAME,productName.toLowerCase());

        if (!(locale == null || locale.trim().isEmpty()))
            node.setProperty(PRODUCT_LOCALE,locale.toLowerCase());
        if (!(variant == null || variant.trim().isEmpty()))
            node.setProperty(PRODUCT_VARIANT,variant.toLowerCase());
//        if (!(url == null || url.trim().isEmpty()))
//            node.setProperty(PRODUCT_URL,url.toLowerCase());
//        if (!(img == null || img.trim().isEmpty()))
//            node.setProperty(PRODUCT_IMG_URL,img.toLowerCase());
//        if (!(description == null || description.trim().isEmpty()))
//            node.setProperty(PRODUCT_DESC,encodeDescription(description));

        node.setProperty(PRODUCT_DISCONTINUED,discontinued);
        node.setProperty(PRODUCT_UPDATED_DATE,date);
    }



    public static String encodeDescription(String description) {
        //TODO limit length, remove HTML tags, remove repeating spaces, remove non standard chars
        //TODO possibly encode/compress
        if (description.length()>512)
            description= description.substring(0,512);
        return description.toLowerCase();
    }

    public static String decodeDesString(String description){
        //TODO if compressed, then decompress
        return description;
    }

    static public void setReviewProperties(Node node, long reviewID, long reviewDate, String source, String type, String modCode){
        node.setProperty(REVIEW_ID,reviewID);
        node.setProperty(REVIEW_MOD_DESC,modCode == null? "OK":modCode);
        node.setProperty(REVIEW_DATE,reviewDate);
        node.setProperty(REVIEW_SOURCE,source);
        node.setProperty(REVIEW_TYPE,type);
    }

    static private int getModerationDescitionCode(String code){
        int hash = code.hashCode();
        Integer val = MOD_DESCISIONS.get(hash);
        if (val == null){
            val = MOD_DESC_COUNTER;
            MOD_DESCISIONS.put(hash,MOD_DESC_COUNTER);
            MOD_DESC_COUNTER++;
        }
        return val;
    }

    public static class MissingReqPropException extends Exception {
        public MissingReqPropException(String propertyName){
            super("Missing required property:"+propertyName);
        }
    }
}

