package de.one_piece_api.config;

import de.one_piece_api.interfaces.StyledConnection;
import de.one_piece_api.util.DataGenUtil;
import net.minecraft.util.Identifier;
import net.puffish.skillsmod.api.config.ConfigContext;
import net.puffish.skillsmod.api.json.JsonArray;
import net.puffish.skillsmod.api.json.JsonElement;
import net.puffish.skillsmod.api.util.Problem;
import net.puffish.skillsmod.api.util.Result;
import net.puffish.skillsmod.common.SkillConnection;
import net.puffish.skillsmod.config.skill.SkillConnectionsConfig;
import net.puffish.skillsmod.config.skill.SkillConnectionsGroupConfig;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Stream;

public record ConnectionsConfig(
        ConnectionCategory exclusive,
        ConnectionCategory normal
) {

    private static SkillConnection createConnection(Connection connection, boolean bidirectional) {
        var con = new SkillConnection(
                DataGenUtil.generateDeterministicId(connection.sourceSkill()),
                DataGenUtil.generateDeterministicId(connection.targetSkill()),
                bidirectional
        );
        var styled = (StyledConnection) (Object) con;
        connection.styleId().ifPresent(styled::onepiece$setStyle);
        return con;
    }

    private static Map<String, Collection<String>> buildNeighborsMap(
            List<SkillConnection> bidirectional,
            List<SkillConnection> unidirectional
    ) {
        Map<String, Collection<String>> neighbors = new HashMap<>();

        for (SkillConnection conn : bidirectional) {
            neighbors.computeIfAbsent(conn.skillAId(), k -> new HashSet<>()).add(conn.skillBId());
            neighbors.computeIfAbsent(conn.skillBId(), k -> new HashSet<>()).add(conn.skillAId());
        }

        for (SkillConnection conn : unidirectional) {
            neighbors.computeIfAbsent(conn.skillBId(), k -> new HashSet<>()).add(conn.skillAId());
        }

        return neighbors;
    }

    private static SkillConnectionsGroupConfig createGroupConfig(
            List<SkillConnection> connections,
            Map<String, Collection<String>> neighbors
    ) {
        try {
            Constructor<SkillConnectionsGroupConfig> constructor =
                    SkillConnectionsGroupConfig.class.getDeclaredConstructor(List.class, Map.class);
            constructor.setAccessible(true);
            return constructor.newInstance(connections, neighbors);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SkillConnectionsGroupConfig", e);
        }
    }

    public static Result<SkillConnectionsConfig, Problem> parse2(JsonElement jsonElement, ConfigContext configContext) {
        return parse(jsonElement, configContext).mapSuccess(connectionsConfig->{
            List<SkillConnection> exclusiveBi = connectionsConfig.exclusive().bidirectional().stream()
                    .map(connection -> createConnection(connection, true))
                    .toList();

            List<SkillConnection> exclusiveUni = connectionsConfig.exclusive().unidirectional().stream()
                    .map(connection -> createConnection(connection, false))
                    .toList();

            List<SkillConnection> normalBi = connectionsConfig.normal().bidirectional().stream()
                    .map(connection -> createConnection(connection, true))
                    .toList();

            List<SkillConnection> normalUni = connectionsConfig.normal().unidirectional().stream()
                    .map(connection -> createConnection(connection, false))
                    .toList();

            Map<String, Collection<String>> exclusiveNeighbors = buildNeighborsMap(exclusiveBi, exclusiveUni);
            Map<String, Collection<String>> normalNeighbors = buildNeighborsMap(normalBi, normalUni);

            SkillConnectionsGroupConfig exclusiveGroup = createGroupConfig(
                    Stream.concat(exclusiveBi.stream(), exclusiveUni.stream()).toList(),
                    exclusiveNeighbors
            );

            SkillConnectionsGroupConfig normalGroup = createGroupConfig(
                    Stream.concat(normalBi.stream(), normalUni.stream()).toList(),
                    normalNeighbors
            );

            return new SkillConnectionsConfig(normalGroup, exclusiveGroup);
        });
    }

    public static Result<ConnectionsConfig, Problem> parse(JsonElement jsonElement, ConfigContext configContext) {
        return jsonElement.getAsObject().andThen(rootObject -> {
            List<Problem> problems = new ArrayList<>();

            var exclusiveResult = rootObject.get("exclusive")
                    .andThen(element -> parseConnectionCategory(element, "exclusive"))
                    .ifFailure(problems::add);

            var normalResult = rootObject.get("normal")
                    .andThen(element -> parseConnectionCategory(element, "normal"))
                    .ifFailure(problems::add);

            if (!problems.isEmpty()) {
                return Result.failure(Problem.combine(problems));
            }

            return Result.success(new ConnectionsConfig(
                    exclusiveResult.getSuccess().orElseThrow(),
                    normalResult.getSuccess().orElseThrow()
            ));
        });
    }

    private static Result<ConnectionCategory, Problem> parseConnectionCategory(JsonElement element, String categoryName) {
        return element.getAsObject().andThen(categoryObject -> {
            var problems = new ArrayList<Problem>();

            var bidirectionalResult = categoryObject.get("bidirectional")
                    .andThen(JsonElement::getAsArray)
                    .andThen(array -> parseConnectionList(array, categoryName + ".bidirectional"))
                    .ifFailure(problems::add);

            var unidirectionalResult = categoryObject.get("unidirectional")
                    .andThen(JsonElement::getAsArray)
                    .andThen(array -> parseConnectionList(array, categoryName + ".unidirectional"))
                    .ifFailure(problems::add);

            if (!problems.isEmpty()) {
                return Result.failure(Problem.combine(problems));
            }

            return Result.success(new ConnectionCategory(
                    bidirectionalResult.getSuccess().orElse(List.of()),
                    unidirectionalResult.getSuccess().orElse(List.of())
            ));
        });
    }

    private static Result<List<Connection>, Problem> parseConnectionList(JsonArray array, String path) {
        var connections = new ArrayList<Connection>();
        var problems = new ArrayList<Problem>();

        array.stream().forEach(element -> {
           parseSkillConnection(element, path).ifSuccess(connections::add).ifFailure(problems::add);
        });

        if (!problems.isEmpty()) {
            return Result.failure(Problem.combine(problems));
        }

        return Result.success(connections);
    }

    private static Result<Connection, Problem> parseSkillConnection(JsonElement element, String path) {
        return element.getAsObject().andThen(connObject -> {
            var problems = new ArrayList<Problem>();

            var sourceResult = connObject.get("source")
                    .andThen(e->e.getAsString().mapSuccess(Identifier::of))
                    .ifFailure(problems::add);

            var targetResult = connObject.get("target")
                    .andThen(e->e.getAsString().mapSuccess(Identifier::of))
                    .ifFailure(problems::add);

            var styleResult = connObject.get("style")
                    .andThen(e->e.getAsString().mapSuccess(Identifier::of))
                    .ifFailure(problems::add);

            if (!problems.isEmpty()) {
                return Result.failure(Problem.combine(problems));
            }


            return Result.success(new Connection(
                    sourceResult.getSuccess().orElseThrow(),
                    targetResult.getSuccess().orElseThrow(),
                    styleResult.getSuccess()
            ));
        });
    }

    public record ConnectionCategory(
            List<Connection> bidirectional,
            List<Connection> unidirectional
    ) {
    }

    public record Connection(
            Identifier sourceSkill,
            Identifier targetSkill,
            Optional<Identifier> styleId
    ) {
    }
}
