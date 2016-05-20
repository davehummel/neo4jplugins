package internal.product;

import common.CommonLabels;
import common.CommonProperties;
import common.CommonRelationships;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by dmhum_000 on 5/17/2016.
 */
public class CategoryProcessor {

    private static final String NO_CATEGORY = "Miscellaneous";

   static public List<String> splitCategories (String categories){
       if (categories.endsWith(":"))
           categories = categories.substring(0,categories.length()-1);

        if (categories.trim().isEmpty()){
            categories = NO_CATEGORY;
        }

        return Arrays.asList(categories.split(":"));
    }

    static public Node mergeCategoryPath(Node rootNode, List<String> catStrings, ProductImportResource.Summary summary){
        boolean freshBranch = false;
        if (catStrings.isEmpty())
            throw new NullPointerException("Empty Category String");
        Node catNode = rootNode;
        OUTER:for (String catString:catStrings){
            if (!freshBranch){
                Iterable<Relationship> relationships = catNode.getRelationships(CommonRelationships.FITS, Direction.INCOMING);
                for (Relationship r : relationships) {
                    Node temp = r.getStartNode();
                    if (catString.equalsIgnoreCase((String) temp.getProperty(CommonProperties.CATEGORY_NAME))) {
                        catNode = temp;
                        continue OUTER;
                    }
                }
                freshBranch = true;
            }
            if (freshBranch){
                Node newNode = catNode.getGraphDatabase().createNode(CommonLabels.SUBJECT,CommonLabels.CATEGORY);
                newNode.createRelationshipTo(catNode,CommonRelationships.FITS);
                newNode.setProperty(CommonProperties.CATEGORY_NAME,catString);
                catNode = newNode;
                summary.cat_creates++;
            }

        }
        return catNode;
    }

}
