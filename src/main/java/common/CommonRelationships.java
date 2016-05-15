package common;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class CommonRelationships {

    // OWNS = Account ownership relationship, BrandFamily - Brand
    static final RelationshipType OWNS = RelationshipType.withName("OWNS");

    // OFFERS = Presents for feedback.  Merchants offer products, Brands with sites also offer products.
    // this implies an ownership of a product/subject.
    static final RelationshipType OFFERS = RelationshipType.withName("OFFERS");

    // CARRIES = Merchants CARRY brands and brand families
    static final RelationshipType CARRIES = RelationshipType.withName("CARRIES");

    // MARKETS = Brands MARKET products and services
    static final RelationshipType MARKETS = RelationshipType.withName("MARKETS");

    // DESCRIBES = UGC DESCRIBES any Subject like a product or service or a brand
    static final RelationshipType DESCRIBES = RelationshipType.withName("DESCRIBES");

}

