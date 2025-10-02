package de.one_piece_content_data.builder;

import de.one_piece_api.config.ConnectionsConfig;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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


    public ConnectionsConfig build() {
        return new ConnectionsConfig(
                new ConnectionsConfig.ConnectionCategory(exclusive.bi, exclusive.uni),
                new ConnectionsConfig.ConnectionCategory(normal.bi, normal.uni)
        );
    }


    public static class ConnectionBuilder {
        private final List<ConnectionsConfig.Connection> bi = new ArrayList<>();
        private final List<ConnectionsConfig.Connection> uni = new ArrayList<>();

        private ConnectionBuilder addBidirectional(Identifier connection1, Identifier connection2) {
            bi.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.empty()));
            return this;
        }

        public ConnectionBuilder addUnidirectional(Identifier connection1, Identifier connection2) {
            uni.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.empty()));
            return this;
        }

        private ConnectionBuilder addBidirectional(Identifier connection1, Identifier connection2, Identifier style) {
            bi.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.of(style)));
            return this;
        }

        public ConnectionBuilder addUnidirectional(Identifier connection1, Identifier connection2, Identifier style) {
            uni.add(new ConnectionsConfig.Connection(connection1, connection2, Optional.of(style)));
            return this;
        }

    }


}
