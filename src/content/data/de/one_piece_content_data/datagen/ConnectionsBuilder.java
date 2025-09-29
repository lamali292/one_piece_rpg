package de.one_piece_content_data.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConnectionsBuilder {

    private final ConnectionBuilder normal = new ConnectionBuilder();
    private final ConnectionBuilder exclusive = new ConnectionBuilder();

    public ConnectionsBuilder normal(Consumer<ConnectionBuilder> builder) {
        builder.accept(normal);
        return this;
    }

    public ConnectionsBuilder exclusive(Consumer<ConnectionBuilder> builder) {
        builder.accept(exclusive);
        return this;
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("normal", normal.toJson());
        root.add("exclusive", exclusive.toJson());
        return root;
    }


    public record Connection(String connection1, String connection2) {
        String id1() {
            return DataGenUtil.generateDeterministicId(connection1);
        }

        String id2() {
            return DataGenUtil.generateDeterministicId(connection2);
        }
    }

    public static class ConnectionBuilder {
        private final List<Connection> bi = new ArrayList<>();
        private final List<Connection> uni = new ArrayList<>();

        private ConnectionBuilder addBidirectional(String connection1, String connection2) {
            bi.add(new Connection(connection1, connection2));
            return this;
        }

        public ConnectionBuilder addUnidirectional(String connection1, String connection2) {
            uni.add(new Connection(connection1, connection2));
            return this;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            JsonArray biArray = new JsonArray();
            for (Connection c : bi) {
                JsonArray pair = new JsonArray();
                pair.add(c.id1());
                pair.add(c.id2());
                biArray.add(pair);
            }
            json.add("bidirectional", biArray);
            JsonArray uniArray = new JsonArray();
            for (Connection c : uni) {
                JsonArray pair = new JsonArray();
                pair.add(c.id1());
                pair.add(c.id2());
                uniArray.add(pair);
            }
            json.add("unidirectional", uniArray);
            return json;
        }

    }


}
