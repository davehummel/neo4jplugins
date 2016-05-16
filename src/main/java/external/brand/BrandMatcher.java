package external.brand;

import common.MergeRecommendation;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.spell.JaroWinklerDistance;
import org.neo4j.cypher.internal.frontend.v2_3.ast.Merge;
import org.neo4j.cypher.internal.frontend.v2_3.ast.Parameter;
import org.neo4j.driver.v1.*;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class BrandMatcher {

    private final Statement queryStatement = new Statement("MATCH (b:Brand) return b.name LIMIT 1000000");

    private final Driver driver;



    public static void main(String[] parms){
        BrandMatcher matcher = new BrandMatcher(GraphDatabase.driver("bolt://localhost", AuthTokens.basic("neo4j", "kgtnbtrp")));
        List<String> brands = matcher.getBrands();

        MergeRecommendation recommendation = matcher.recommendMerge(brands);

        System.out.println(recommendation.toString());

    }


    public BrandMatcher(Driver driver){
        this.driver = driver;
    }
    
    public List<String> getBrands(){
        List<String> brands = new ArrayList<>();
        Session session = driver.session();
        try(Transaction tx = session.beginTransaction()){
            StatementResult result = tx.run(queryStatement);
            while (result.hasNext())
                brands.add(result.next().get(0).asString());
            tx.success();
        }
        return brands;
    }

    public MergeRecommendation recommendMerge(List<String> brands){
        MergeRecommendation recommendation = new MergeRecommendation();


        for (int i = 0 ; i < brands.size() ; i++){
            System.out.println(i);
            String left = brands.get(i);
            for (int j = i+1; j < brands.size() ; j++){
                String right = brands.get(j);
                int thresh = left.length();
                if (right.length()<left.length())
                    thresh = right.length();
                thresh = thresh/2;
                if (  StringUtils.getLevenshteinDistance(left,right)<thresh){
                    System.out.print('.');
                    if (left.length()<right.length()){
                        String other = recommendation.addMerge(right,left,false);
                        if (other!=null && other.length()>left.length())
                             recommendation.addMerge(right,left,true);
                    } else {
                        String other = recommendation.addMerge(left,right,false);
                        if (other!=null && other.length()>right.length())
                            recommendation.addMerge(left,right,true);
                        break;
                    }
                }
            }
        }
        return recommendation;
    }


}
