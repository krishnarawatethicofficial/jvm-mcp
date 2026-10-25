package dev.jvmmcp.core.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PrimitiveArrayJsonTest {

    @ParameterizedTest
    @MethodSource("primitiveArrays")
    @DisplayName("toJson should serialize every primitive array type as a JSON array")
    void shouldSerializePrimitiveArrays(Object value, String expected) {
        assertThat(SimpleJson.toJson(value)).isEqualTo(expected);
    }

    static Stream<Arguments> primitiveArrays() {
        return Stream.of(
                Arguments.of(new boolean[] {true, false}, "[true,false]"),
                Arguments.of(new byte[] {-128, 127}, "[-128,127]"),
                Arguments.of(new short[] {-32768, 32767}, "[-32768,32767]"),
                Arguments.of(
                        new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE},
                        "[-2147483648,2147483647]"),
                Arguments.of(
                        new long[] {Long.MIN_VALUE, Long.MAX_VALUE},
                        "[-9223372036854775808,9223372036854775807]"),
                Arguments.of(new float[] {-1.5f, 2.5f}, "[-1.5,2.5]"),
                Arguments.of(new double[] {-1.5, 2.5}, "[-1.5,2.5]"),
                Arguments.of(new char[] {'a', 'b'}, "[\"a\",\"b\"]"),
                Arguments.of(new boolean[0], "[]"),
                Arguments.of(new byte[0], "[]"),
                Arguments.of(new short[0], "[]"),
                Arguments.of(new int[0], "[]"),
                Arguments.of(new long[0], "[]"),
                Arguments.of(new float[0], "[]"),
                Arguments.of(new double[0], "[]"),
                Arguments.of(new char[0], "[]"));
    }

    @Test
    @DisplayName("toJson should preserve character escaping inside primitive arrays")
    void shouldEscapeCharacters() {
        assertThat(SimpleJson.toJson(new char[] {'a', '"', '\\', '\n', '\t'}))
                .isEqualTo("[\"a\",\"\\\"\",\"\\\\\",\"\\n\",\"\\t\"]");
    }

    @Test
    @DisplayName("toJson should preserve null handling for non-finite primitive values")
    void shouldSerializeNonFiniteValuesAsNull() {
        assertThat(
                        SimpleJson.toJson(
                                new double[] {
                                    Double.NaN,
                                    Double.POSITIVE_INFINITY,
                                    Double.NEGATIVE_INFINITY,
                                    1.5
                                }))
                .isEqualTo("[null,null,null,1.5]");
        assertThat(
                        SimpleJson.toJson(
                                new float[] {
                                    Float.NaN,
                                    Float.POSITIVE_INFINITY,
                                    Float.NEGATIVE_INFINITY,
                                    2.5f
                                }))
                .isEqualTo("[null,null,null,2.5]");
    }

    @Test
    @DisplayName("toJson should serialize primitive arrays nested in reference arrays and maps")
    void shouldSerializeNestedArrays() {
        Object[] values = {new int[] {1, 2}, null, new int[][] {{3}, {4, 5}}};
        assertThat(SimpleJson.toJson(Map.of("values", values)))
                .isEqualTo("{\"values\":[[1,2],null,[[3],[4,5]]]}");
    }

    public record ArrayRecord(int[] counts, boolean[] flags) {}

    @Test
    @DisplayName("toJson should serialize primitive array record fields")
    void shouldSerializeArrayRecordFields() {
        assertThat(SimpleJson.toJson(new ArrayRecord(new int[] {1, 2}, new boolean[] {true})))
                .isEqualTo("{\"counts\":[1,2],\"flags\":[true]}");
    }

    @Test
    @DisplayName("toJson should preserve reference array serialization")
    void shouldPreserveReferenceArrays() {
        assertThat(SimpleJson.toJson(new Object[] {"text", 1, null, true}))
                .isEqualTo("[\"text\",1,null,true]");
        assertThat(SimpleJson.toJson(new Object[0])).isEqualTo("[]");
    }

    @Test
    @DisplayName("toJson should accept primitive array values at the maximum supported depth")
    void shouldAcceptDepthBoundary() {
        Object value = new int[] {7};
        for (int i = 0; i < 99; i++) {
            value = new Object[] {value};
        }
        assertThat(SimpleJson.toJson(value)).isEqualTo("[".repeat(100) + "7" + "]".repeat(100));
    }

    @Test
    @DisplayName("toJson should still reject primitive values beyond the nesting limit")
    void shouldRejectExcessiveDepth() {
        Object value = new int[] {7};
        for (int i = 0; i < 100; i++) {
            value = new Object[] {value};
        }
        Object nested = value;
        assertThatThrownBy(() -> SimpleJson.toJson(nested))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("depth");
    }

    @Test
    @DisplayName("toJson should still reject self-referencing arrays")
    void shouldRejectCycles() {
        Object[] value = new Object[1];
        value[0] = value;
        assertThatThrownBy(() -> SimpleJson.toJson(value))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("depth");
    }
}
