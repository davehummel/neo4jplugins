package common;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by dmhum_000 on 5/15/2016.
 */
public class MergeRecommendation {

  //  private final Map<String,Set<String>> toFromMap = new HashMap<>();
    private final Map<String,String> fromToMap = new HashMap<>();

    public MergeRecommendation(Stream<String> stream){
        stream.forEach(s -> {
            String[] data = s.split("¦");
            for (int i = 2; i< data.length ; i++){
                fromToMap.put(data[i],data[0]);
            }
        });
    }

    public MergeRecommendation() {

    }


    public String addMerge(String from, String to,boolean override){
        String prev;
        if (!override && (prev=fromToMap.get(from))!=null)
            return prev;
        String finalTo = fromToMap.get(to);
        if (finalTo!=null)
            to=finalTo;
        fromToMap.put(from,to);
        return null;
    }


    public String getMerge(String from){
        return fromToMap.get(from);
    }

    public String toString(){
        Map<String,Set<String>> toFromMap = new TreeMap<>();
        for (Map.Entry<String,String> fromTo:fromToMap.entrySet()){
            Set<String> fromSet = toFromMap.get(fromTo.getValue());
            if (fromSet == null){
                fromSet = new TreeSet<>();
                toFromMap.put(fromTo.getValue(),fromSet);
            }
            fromSet.add(fromTo.getKey());
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String,Set<String>> toFrom:toFromMap.entrySet()){
            sb.append(toFrom.getKey());
            sb.append("¦");
            for(String from:toFrom.getValue()){
                sb.append("¦");
                sb.append(from);
            }
            sb.append('\n');
        }
        return sb.toString();
    }


}
