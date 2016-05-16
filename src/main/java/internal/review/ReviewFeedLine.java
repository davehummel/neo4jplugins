package internal.review;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dmhum_000 on 5/13/2016.
 */
public class ReviewFeedLine {

    private static SimpleDateFormat dateFMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public final long accountID;
    public final long merchantGroupID;
    public final long merchantID;
    public final long productID;
    public final long reviewID;
    public final String l1Reason;
    public final String l1Status;
    public final String source;
    public final String reviewerType;
    public final long date;


    public ReviewFeedLine(String input){
        input = input.toLowerCase();
        List<String> result = new ArrayList<String>();
        int start = 0;
        boolean inQuotes = false;
        for (int current = 0; current < input.length(); current++) {
            if (input.charAt(current) == '\"') inQuotes = !inQuotes; // toggle state
            boolean atLastChar = (current == input.length() - 1);
            if(atLastChar) result.add(input.substring(start));
            else if (input.charAt(current) == ',' && !inQuotes) {
                int realCurrent = current;
                while(start!=current){
                    boolean done = true;
                    char x = input.charAt(start);
                    if (x == '"' || x == '\'' || x == ' '){
                        start++;
                        done = false;
                    }
                    x = input.charAt(current);
                    if (x == '"' || x == '\'' || x == ' '){
                        current--;
                        done = false;
                    }
                    if (done)
                        break;
                }

                result.add(input.substring(start,current));
                start = realCurrent + 1;
            }
        }

        try {
            accountID = Long.parseLong(result.get(0));
        }
        catch(NumberFormatException e){
                throw new NullPointerException("Bad AccountID.");
        }

        try {
            merchantGroupID = Long.parseLong(result.get(1));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Merchant Group ID.");
        }


        try {
            merchantID = Long.parseLong(result.get(2));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Merchant ID.");
        }

        try {
            productID = Long.parseLong(result.get(3));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Product ID.");
        }

        try {
            reviewID = Long.parseLong(result.get(4));
        }
        catch(NumberFormatException e){
            throw new NullPointerException("Bad Review ID.");
        }

        try {
            date = dateFMT.parse(result.get(5)).getTime();
        } catch (ParseException e) {
            throw new NullPointerException(e.getMessage()+"<"+result.get(25)+">");
        }

        l1Reason = result.get(11).toLowerCase();

        l1Status = result.get(12).toLowerCase();

        source = result.get(15).toLowerCase();

        reviewerType = result.get(26).toLowerCase();


    }

}
