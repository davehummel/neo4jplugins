package common;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.RelationshipType;

/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class CommonRelationships {

    // OWNS = Account ownership relationship, BrandFamily - Brand
    static final RelationshipType OWNS = RelationshipType.withName("OWNS");

    // OFFERS = Any subject is offered by some parent.  Merchants offer products, Brands with sites can also offer products.
    // this implies an ownership of a internal.product/subject.
    static final RelationshipType OFFERS = RelationshipType.withName("OFFERS");

    // CARRIES = Merchants CARRY brands and internal.brand families
    static final RelationshipType CARRIES = RelationshipType.withName("CARRIES");

    // MARKETS = Brands MARKET products and services
    static final RelationshipType MARKETS = RelationshipType.withName("MARKETS");

    // DESCRIBES = UGC DESCRIBES any Subject like a internal.product or service or a internal.brand
    static final RelationshipType DESCRIBES = RelationshipType.withName("DESCRIBES");

    // RELATED = Subjects are related if they are similar to each other .. ie internal.product variants or internal.product matches
    static final RelationshipType RELATED = RelationshipType.withName("RELATED");

    // FITS = Subjects FITS categories
    static final RelationshipType FITS = RelationshipType.withName("FITS");

}

