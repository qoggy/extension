package io.github.qoggy.extension.core.util;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for ToStringHelper
 *
 * @author yutianhong
 * @version 1.0
 * @since 2025/10/5 15:30
 */
class ToStringHelperTest {

    @Test
    void testToStringHelper_withObject() {
        String result = ToStringHelper.toStringHelper(this)
                .add("name", "test")
                .toString();

        assertEquals("ToStringHelperTest{name=test}", result);
    }

    @Test
    void testToStringHelper_withClassName() {
        String result = ToStringHelper.toStringHelper("TestClass")
                .add("field", "value")
                .toString();

        assertEquals("TestClass{field=value}", result);
    }

    @Test
    void testAdd_multipleFields() {
        String result = ToStringHelper.toStringHelper("Person")
                .add("name", "张三")
                .add("age", 25)
                .add("active", true)
                .toString();

        assertEquals("Person{name=张三, age=25, active=true}", result);
    }

    @Test
    void testAdd_nullValue() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("field1", "value1")
                .add("field2", null)
                .add("field3", "value3")
                .toString();

        assertEquals("Test{field1=value1, field2=null, field3=value3}", result);
    }

    @Test
    void testOmitNullValues() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("field1", "value1")
                .add("field2", null)
                .add("field3", "value3")
                .omitNullValues()
                .toString();

        assertEquals("Test{field1=value1, field3=value3}", result);
    }

    @Test
    void testOmitNullValues_allNull() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("field1", null)
                .add("field2", null)
                .omitNullValues()
                .toString();

        assertEquals("Test{}", result);
    }

    @Test
    void testOmitEmptyValues_string() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("field1", "value1")
                .add("field2", "")
                .add("field3", "value3")
                .omitEmptyValues()
                .toString();

        assertEquals("Test{field1=value1, field3=value3}", result);
    }

    @Test
    void testOmitEmptyValues_collection() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("list1", Arrays.asList("a", "b"))
                .add("list2", new ArrayList<>())
                .add("list3", Arrays.asList("c"))
                .omitEmptyValues()
                .toString();

        assertEquals("Test{list1=[a, b], list3=[c]}", result);
    }

    @Test
    void testOmitEmptyValues_map() {
        Map<String, String> emptyMap = new HashMap<>();
        Map<String, String> nonEmptyMap = new HashMap<>();
        nonEmptyMap.put("key", "value");

        String result = ToStringHelper.toStringHelper("Test")
                .add("map1", nonEmptyMap)
                .add("map2", emptyMap)
                .omitEmptyValues()
                .toString();

        assertEquals("Test{map1={key=value}}", result);
    }

    @Test
    void testOmitEmptyValues_array() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("arr1", new int[]{1, 2, 3})
                .add("arr2", new int[0])
                .add("arr3", new String[]{"a", "b"})
                .omitEmptyValues()
                .toString();

        assertEquals("Test{arr1=[1, 2, 3], arr3=[a, b]}", result);
    }

    @Test
    void testOmitEmptyValues_optional() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("opt1", Optional.of("value"))
                .add("opt2", Optional.empty())
                .add("opt3", OptionalInt.of(42))
                .add("opt4", OptionalInt.empty())
                .omitEmptyValues()
                .toString();

        assertEquals("Test{opt1=Optional[value], opt3=OptionalInt[42]}", result);
    }

    @Test
    void testArray_primitiveArray() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("intArray", new int[]{1, 2, 3})
                .add("boolArray", new boolean[]{true, false})
                .toString();

        assertEquals("Test{intArray=[1, 2, 3], boolArray=[true, false]}", result);
    }

    @Test
    void testArray_objectArray() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("stringArray", new String[]{"a", "b", "c"})
                .add("objectArray", new Object[]{"hello", 123, true})
                .toString();

        assertEquals("Test{stringArray=[a, b, c], objectArray=[hello, 123, true]}", result);
    }

    @Test
    void testArray_nestedArray() {
        int[][] nestedArray = {{1, 2}, {3, 4}};
        String result = ToStringHelper.toStringHelper("Test")
                .add("nested", nestedArray)
                .toString();

        assertEquals("Test{nested=[[1, 2], [3, 4]]}", result);
    }

    @Test
    void testCombinedOptions_omitNullAndEmpty() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("field1", "value1")
                .add("field2", null)
                .add("field3", "")
                .add("field4", new ArrayList<>())
                .add("field5", "value5")
                .omitNullValues()
                .omitEmptyValues()
                .toString();

        assertEquals("Test{field1=value1, field5=value5}", result);
    }

    @Test
    void testEmptyToString() {
        String result = ToStringHelper.toStringHelper("Empty").toString();
        assertEquals("Empty{}", result);
    }

    @Test
    void testToStringHelper_nullObject_shouldThrow() {
        assertThrows(NullPointerException.class, () -> {
            ToStringHelper.toStringHelper((Object) null);
        });
    }

    @Test
    void testToStringHelper_nullClassName_shouldThrow() {
        assertThrows(NullPointerException.class, () -> {
            ToStringHelper.toStringHelper((String) null);
        });
    }

    @Test
    void testAdd_nullName_shouldThrow() {
        assertThrows(NullPointerException.class, () -> {
            ToStringHelper.toStringHelper("Test").add(null, "value");
        });
    }

    @Test
    void testChaining_orderPreserved() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("first", 1)
                .omitNullValues()
                .add("second", 2)
                .omitEmptyValues()
                .add("third", 3)
                .toString();

        assertEquals("Test{first=1, second=2, third=3}", result);
    }

    @Test
    void testSpecialCharacters() {
        String result = ToStringHelper.toStringHelper("Test")
                .add("field", "value with spaces and symbols: !@#$%")
                .toString();

        assertEquals("Test{field=value with spaces and symbols: !@#$%}", result);
    }
}
