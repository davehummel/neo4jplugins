package common;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by dmhum on 5/15/2016.
 */
public class ModerationCode {

    public static final String MOD_ID_PROP="mod_code_id";
    public static final String MOD_TEXT="mod_code";

    public static final Label MODERATION_CODE = Label.label("MOD_CODE");

    public static final String[][] DEFAULTS = {{"","OK"},{"Test Review"}};

    static public void CLEAR_AND_SEED_DB(GraphDatabaseService db){
        try(Transaction tx = db.beginTx()) {
            db.findNodes(MODERATION_CODE).forEachRemaining(node -> node.delete());

            for (int i = 0; i < DEFAULTS.length; i++) {
                for (int j = 0; j < DEFAULTS[i].length; j++) {
                    Node node;
                    node = db.createNode(MODERATION_CODE);
                    node.setProperty(MOD_ID_PROP, i);
                    node.setProperty(MOD_TEXT, DEFAULTS[i][j]);
                }
            }
            tx.success();
        }
    }

    static final Map<String,Long> codes = new HashMap<>();

    static public void load(GraphDatabaseService db){
        codes.clear();
        db.findNodes(MODERATION_CODE).forEachRemaining(node -> codes.put((String)node.getProperty(MOD_TEXT),(Long)node.getProperty(MOD_ID_PROP)));
    }
}
