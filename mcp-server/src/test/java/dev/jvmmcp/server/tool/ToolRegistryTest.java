package dev.jvmmcp.server.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ToolRegistryTest {

    @Test
    void rejectsRepeatedHandlerWithoutChangingTheRegistry() {
        try (ToolRegistry registry = new ToolRegistry()) {
            ToolHandler handler =
                    new StubToolHandler(
                            List.of("tool"),
                            List.of(Map.of("name", "tool")),
                            (name, args) -> Map.of());
            registry.register(handler);

            assertThatThrownBy(() -> registry.register(handler))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Duplicate tool registration for: tool");
            assertThat(registry.getHandler("tool")).containsSame(handler);
            assertThat(registry.getToolsList()).containsExactly(Map.of("name", "tool"));
        }
    }

    @Test
    void rejectsCollisionsBeforeRegisteringAnyToolsFromTheNewHandler() {
        try (ToolRegistry registry = new ToolRegistry()) {
            ToolHandler original =
                    new StubToolHandler(
                            List.of("shared"),
                            List.of(Map.of("name", "shared")),
                            (name, args) -> Map.of("source", "original"));
            ToolHandler conflicting =
                    new StubToolHandler(
                            List.of("new", "shared"),
                            List.of(Map.of("name", "new"), Map.of("name", "shared")),
                            (name, args) -> Map.of("source", "replacement"));
            registry.register(original);

            assertThatThrownBy(() -> registry.register(conflicting))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Duplicate tool registration for: shared");
            assertThat(registry.getHandler("new")).isEmpty();
            assertThat(registry.execute("shared", Map.of())).containsEntry("source", "original");
            assertThat(registry.getToolsList()).containsExactly(Map.of("name", "shared"));
        }
    }

    @Test
    void rejectsDuplicateNamesWithinOneHandler() {
        try (ToolRegistry registry = new ToolRegistry()) {
            ToolHandler handler =
                    new StubToolHandler(
                            List.of("tool", "tool"),
                            List.of(Map.of("name", "tool")),
                            (name, args) -> Map.of());

            assertThatThrownBy(() -> registry.register(handler))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Duplicate tool registration for: tool");
            assertThat(registry.getHandler("tool")).isEmpty();
            assertThat(registry.getToolsList()).isEmpty();
        }
    }

    @Test
    void listsEachDefinitionOnceInFirstSeenOrder() {
        try (ToolRegistry registry = new ToolRegistry()) {
            Map<String, Object> first = Map.of("name", "first", "description", "original");
            Map<String, Object> second = Map.of("name", "second");
            registry.register(
                    new StubToolHandler(
                            List.of("first"),
                            List.of(first, Map.of("name", "first", "description", "duplicate")),
                            (name, args) -> Map.of()));
            registry.register(
                    new StubToolHandler(
                            List.of("second"),
                            List.of(first, second, second),
                            (name, args) -> Map.of()));

            assertThat(registry.getToolsList()).containsExactly(first, second);
            assertThat(registry.getToolsList()).containsExactly(first, second);
        }
    }

    @Test
    @DisplayName("register and getHandler returns correct handler for registered tool")
    void testRegisterAndLookup() {
        ToolRegistry registry = new ToolRegistry();
        ToolHandler handler =
                new StubToolHandler(
                        List.of("stub_tool1", "stub_tool2"),
                        List.of(Map.of("name", "stub_tool1"), Map.of("name", "stub_tool2")),
                        (name, args) ->
                                Map.of("content", List.of(Map.of("type", "text", "text", "ok"))));

        registry.register(handler);

        assertThat(registry.getHandler("stub_tool1")).contains(handler);
        assertThat(registry.getHandler("stub_tool2")).contains(handler);
        assertThat(registry.getHandler("non_existent")).isEmpty();
        assertThat(registry.getHandler(null)).isEmpty();
    }

    @Test
    @DisplayName("register handles null safely")
    void testRegisterNull() {
        ToolRegistry registry = new ToolRegistry();
        registry.register(null);
        assertThat(registry.getToolsList()).isEmpty();
    }

    @Test
    @DisplayName("getToolsList aggregates all tool definitions across handlers in order")
    void testGetToolsList() {
        ToolRegistry registry = new ToolRegistry();
        ToolHandler handler1 =
                new StubToolHandler(
                        List.of("toolA"),
                        List.of(Map.of("name", "toolA")),
                        (name, args) -> Map.of());
        ToolHandler handler2 =
                new StubToolHandler(
                        List.of("toolB", "toolC"),
                        List.of(Map.of("name", "toolB"), Map.of("name", "toolC")),
                        (name, args) -> Map.of());

        registry.register(handler1);
        registry.register(handler2);

        List<Map<String, Object>> tools = registry.getToolsList();
        assertThat(tools).hasSize(3);
        assertThat(tools.get(0).get("name")).isEqualTo("toolA");
        assertThat(tools.get(1).get("name")).isEqualTo("toolB");
        assertThat(tools.get(2).get("name")).isEqualTo("toolC");
    }

    @Test
    @DisplayName("execute delegates to handler on success")
    void testExecuteSuccess() {
        ToolRegistry registry = new ToolRegistry();
        ToolHandler handler =
                new StubToolHandler(
                        List.of("greet"),
                        List.of(Map.of("name", "greet")),
                        (name, args) ->
                                Map.of(
                                        "content",
                                        List.of(
                                                Map.of(
                                                        "type",
                                                        "text",
                                                        "text",
                                                        "Hello " + args.get("name")))));
        registry.register(handler);

        Map<String, Object> result = registry.execute("greet", Map.of("name", "Dario"));
        assertThat(result.get("isError")).isNull();
        List<?> content = (List<?>) result.get("content");
        assertThat(content).hasSize(1);
        Map<?, ?> item = (Map<?, ?>) content.get(0);
        assertThat(item.get("text")).isEqualTo("Hello Dario");
    }

    @Test
    @DisplayName("execute returns error for unknown or null tool name")
    void testExecuteUnknownTool() {
        ToolRegistry registry = new ToolRegistry();

        Map<String, Object> result = registry.execute("unknown", Map.of());
        assertThat(result.get("isError")).isEqualTo(true);
        List<?> content = (List<?>) result.get("content");
        Map<?, ?> item = (Map<?, ?>) content.get(0);
        assertThat(item.get("text").toString()).contains("Unknown tool: unknown");

        Map<String, Object> nullResult = registry.execute(null, Map.of());
        assertThat(nullResult.get("isError")).isEqualTo(true);
        List<?> nullContent = (List<?>) nullResult.get("content");
        Map<?, ?> nullItem = (Map<?, ?>) nullContent.get(0);
        assertThat(nullItem.get("text").toString()).contains("Unknown tool: null");
    }

    @Test
    @DisplayName("execute catches exceptions from handler and formats error result")
    void testExecuteHandlerThrows() {
        ToolRegistry registry = new ToolRegistry();
        ToolHandler handler =
                new StubToolHandler(
                        List.of("failing"),
                        List.of(Map.of("name", "failing")),
                        (name, args) -> {
                            throw new IllegalStateException("Simulated explosion");
                        });
        registry.register(handler);

        Map<String, Object> result = registry.execute("failing", Map.of());
        assertThat(result.get("isError")).isEqualTo(true);
        List<?> content = (List<?>) result.get("content");
        Map<?, ?> item = (Map<?, ?>) content.get(0);
        assertThat(item.get("text").toString())
                .contains("Error executing tool: Simulated explosion");
    }

    @Test
    @DisplayName("execute automatically scrubs sensitive tokens and credentials from tool output")
    void testExecuteRedactsSensitiveDataInContent() {
        ToolRegistry registry = new ToolRegistry();
        ToolHandler handler =
                new StubToolHandler(
                        List.of("secret_tool"),
                        List.of(Map.of("name", "secret_tool")),
                        (name, args) ->
                                Map.of(
                                        "content",
                                        List.of(
                                                Map.of(
                                                        "type",
                                                        "text",
                                                        "text",
                                                        "Found connection jdbc:postgresql://postgres:myPass123@localhost/db with token=xyz789"))));
        registry.register(handler);

        Map<String, Object> result = registry.execute("secret_tool", Map.of());
        assertThat(result.get("isError")).isNull();
        List<?> content = (List<?>) result.get("content");
        Map<?, ?> item = (Map<?, ?>) content.get(0);
        String text = item.get("text").toString();
        assertThat(text)
                .contains("jdbc:postgresql://postgres:***@localhost/db")
                .contains("token=***")
                .doesNotContain("myPass123")
                .doesNotContain("xyz789");
    }

    @Test
    @DisplayName("execute scrubs sensitive credentials from error messages")
    void testExecuteRedactsSensitiveDataInError() {
        ToolRegistry registry = new ToolRegistry();
        ToolHandler handler =
                new StubToolHandler(
                        List.of("failing_secret"),
                        List.of(Map.of("name", "failing_secret")),
                        (name, args) -> {
                            throw new IllegalStateException(
                                    "Authentication failed for token=secretApiToken456");
                        });
        registry.register(handler);

        Map<String, Object> result = registry.execute("failing_secret", Map.of());
        assertThat(result.get("isError")).isEqualTo(true);
        List<?> content = (List<?>) result.get("content");
        Map<?, ?> item = (Map<?, ?>) content.get(0);
        String text = item.get("text").toString();
        assertThat(text).contains("token=***").doesNotContain("secretApiToken456");
    }

    @Test
    @DisplayName("execute terminates and flags error when tool execution times out")
    void testExecuteTimesOutOnHungTool() {
        ToolRegistry registry = new ToolRegistry(java.time.Duration.ofMillis(200));
        ToolHandler handler =
                new StubToolHandler(
                        List.of("hung_tool"),
                        List.of(Map.of("name", "hung_tool")),
                        (name, args) -> {
                            Thread.sleep(1000);
                            return Map.of(
                                    "content", List.of(Map.of("type", "text", "text", "done")));
                        });
        registry.register(handler);

        Map<String, Object> result = registry.execute("hung_tool", Map.of());
        assertThat(result.get("isError")).isEqualTo(true);
        List<?> content = (List<?>) result.get("content");
        Map<?, ?> item = (Map<?, ?>) content.get(0);
        assertThat(item.get("text").toString()).contains("Tool execution timed out");
    }

    @Test
    @DisplayName("close shuts down the virtual thread executor service")
    void testCloseShutsDownExecutor() throws Exception {
        ToolRegistry registry = new ToolRegistry();
        java.lang.reflect.Field field = ToolRegistry.class.getDeclaredField("executorService");
        field.setAccessible(true);
        java.util.concurrent.ExecutorService es =
                (java.util.concurrent.ExecutorService) field.get(registry);
        assertThat(es.isShutdown()).isFalse();

        registry.close();
        assertThat(es.isShutdown()).isTrue();
    }

    @Test
    @DisplayName("execute performs deep redaction on complex nested maps and lists")
    void testExecuteDeepRedaction() {
        try (ToolRegistry registry = new ToolRegistry()) {
            ToolHandler handler =
                    new StubToolHandler(
                            List.of("nested_tool"),
                            List.of(Map.of("name", "nested_tool")),
                            (name, args) ->
                                    Map.of(
                                            "content",
                                                    List.of(
                                                            Map.of(
                                                                    "type",
                                                                    "nested",
                                                                    "metadata",
                                                                    Map.of(
                                                                            "secretKey",
                                                                                    "superSecretPass",
                                                                            "connectionString",
                                                                                    "jdbc:postgresql://user:pass123@localhost:5432/db",
                                                                            "headers",
                                                                                    List.of(
                                                                                            "Bearer secretTokenXYZ",
                                                                                            "normal-header")))),
                                            "extraInfo", Map.of("api_key", "sk-1234567890abcdef")));
            registry.register(handler);

            Map<String, Object> result = registry.execute("nested_tool", Map.of());
            assertThat(result.get("isError")).isNull();

            List<?> content = (List<?>) result.get("content");
            Map<?, ?> item = (Map<?, ?>) content.get(0);
            Map<?, ?> metadata = (Map<?, ?>) item.get("metadata");
            assertThat(metadata.get("secretKey")).isEqualTo("***");
            assertThat(metadata.get("connectionString"))
                    .isEqualTo("jdbc:postgresql://user:***@localhost:5432/db");

            List<?> headers = (List<?>) metadata.get("headers");
            assertThat(headers.get(0)).isEqualTo("Bearer ***");
            assertThat(headers.get(1)).isEqualTo("normal-header");

            Map<?, ?> extraInfo = (Map<?, ?>) result.get("extraInfo");
            assertThat(extraInfo.get("api_key")).isEqualTo("***");
        }
    }

    private static class StubToolHandler implements ToolHandler {
        private final List<String> names;
        private final List<Map<String, Object>> defs;
        private final ToolExecutor executor;

        StubToolHandler(List<String> names, List<Map<String, Object>> defs, ToolExecutor executor) {
            this.names = names;
            this.defs = defs;
            this.executor = executor;
        }

        @Override
        public List<String> toolNames() {
            return names;
        }

        @Override
        public List<Map<String, Object>> toolDefinitions() {
            return defs;
        }

        @Override
        public Map<String, Object> execute(String toolName, Map<String, Object> arguments)
                throws Exception {
            return executor.run(toolName, arguments);
        }
    }

    @FunctionalInterface
    private interface ToolExecutor {
        Map<String, Object> run(String name, Map<String, Object> args) throws Exception;
    }
}
