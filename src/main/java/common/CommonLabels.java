package common;

import org.neo4j.graphdb.Label;

/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class CommonLabels {


    public static final Label ACCOUNT = Label.label("Account");
    public static final Label SUBJECT = Label.label("Subject");
    public static final Label MERCHANT = Label.label("Merchant");
    public static final Label BRAND_FAMILY = Label.label("BrandFamily");
    public static final Label BRAND = Label.label("Brand");
    public static final Label SOURCE = Label.label("Source");
    public static final Label CATALOG = Label.label("Catalog");
    public static final Label PRODUCT = Label.label("Product");
    public static final Label SERVICE = Label.label("Service");
    public static final Label PACKAGE = Label.label("Package");


    public static final Label UGC = Label.label("UGC");
    public static final Label REVIEW = Label.label("Review");
    public static final Label QUESTION = Label.label("Question");
    public static final Label ANSWER = Label.label("Answer");
    public static final Label RESPONSE = Label.label("Response");
    public static final Label UPDATE = Label.label("Update");


    public static final Label[] COMPLETE_PACKAGE= {SUBJECT,PACKAGE};
    public static final Label[] COMPLETE_SERVICE= {SUBJECT,SERVICE};
    public static final Label[] COMPLETE_PRODUCT= {SUBJECT,PRODUCT};

    public static final Label[] COMPLETE_ACCOUNT= {ACCOUNT};
    public static final Label[] COMPLETE_BRANDFAMILY= {SUBJECT,BRAND_FAMILY,SOURCE};
    public static final Label[] COMPLETE_BRAND= {SUBJECT,BRAND,SOURCE};
    public static final Label[] COMPLETE_MERCHANT= {SUBJECT,MERCHANT,CATALOG};
    public static final Label[] COMPLETE_MERCHANT_BRAND= {SUBJECT,MERCHANT,BRAND,CATALOG,SOURCE};

    public static final Label[] COMPLETE_REVIEW= {UGC,REVIEW};
    public static final Label[] COMPLETE_UPDATE= {UGC,UPDATE};
    public static final Label[] COMPLETE_QUESTION= {UGC,QUESTION};
    public static final Label[] COMPLETE_ANSWER= {UGC,ANSWER};
    public static final Label[] COMPLETE_RESPONSE= {UGC,RESPONSE};

}

