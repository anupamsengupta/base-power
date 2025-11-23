package com.power.base.dao.clickhouse.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Utility class for handling ClickHouse arrays in JDBC operations.
 * ClickHouse arrays may require special handling depending on JDBC driver version.
 */
public class ClickHouseArrayUtil {

    /**
     * Create a SQL array from a list for ClickHouse.
     * Note: ClickHouse JDBC driver may handle arrays differently than standard JDBC.
     *
     * @param connection the database connection
     * @param typeName   the ClickHouse type name (e.g., "String", "Float64", "Date", "DateTime64(3)")
     * @param values     the list of values
     * @return SQL Array object
     * @throws SQLException if array creation fails
     */
    public static java.sql.Array createArray(Connection connection, String typeName, List<?> values) throws SQLException {
        if (values == null || values.isEmpty()) {
            // Return empty array
            return connection.createArrayOf(typeName, new Object[0]);
        }
        return connection.createArrayOf(typeName, values.toArray());
    }

    /**
     * Create a nested SQL array (Array of Arrays) for ClickHouse.
     * Used for nested structures like Array(Array(String)).
     *
     * @param connection the database connection
     * @param nestedList the list of lists
     * @return SQL Array object
     * @throws SQLException if array creation fails
     */
    public static java.sql.Array createNestedArray(Connection connection, List<List<String>> nestedList) throws SQLException {
        if (nestedList == null || nestedList.isEmpty()) {
            return connection.createArrayOf("Array(String)", new Object[0]);
        }

        // Convert List<List<String>> to Object[] where each element is String[]
        Object[] arrayOfArrays = nestedList.stream()
                .map(list -> list != null ? list.toArray(new String[0]) : new String[0])
                .toArray();

        // Note: ClickHouse JDBC may need special handling for nested arrays
        // This is a simplified approach; actual implementation may vary by driver version
        return connection.createArrayOf("Array(String)", arrayOfArrays);
    }

    /**
     * Extract array from ResultSet as a List.
     * Handles both primitive arrays (e.g., double[], int[]) and Object arrays.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @param type       the element type
     * @param <T>        the element type
     * @return list of values
     * @throws SQLException if extraction fails
     */
    @SuppressWarnings("unchecked")
    public static <T> List<T> extractArray(java.sql.ResultSet rs, String columnName, Class<T> type) throws SQLException {
        java.sql.Array array = rs.getArray(columnName);
        if (array == null) {
            return List.of();
        }

        Object arrayObj = array.getArray();
        List<T> result = new java.util.ArrayList<>();
        
        // Handle primitive arrays
        if (arrayObj instanceof double[]) {
            double[] primitiveArray = (double[]) arrayObj;
            for (double value : primitiveArray) {
                result.add((T) Double.valueOf(value));
            }
        } else if (arrayObj instanceof float[]) {
            float[] primitiveArray = (float[]) arrayObj;
            for (float value : primitiveArray) {
                result.add((T) Float.valueOf(value));
            }
        } else if (arrayObj instanceof long[]) {
            long[] primitiveArray = (long[]) arrayObj;
            for (long value : primitiveArray) {
                result.add((T) Long.valueOf(value));
            }
        } else if (arrayObj instanceof int[]) {
            int[] primitiveArray = (int[]) arrayObj;
            for (int value : primitiveArray) {
                result.add((T) Integer.valueOf(value));
            }
        } else if (arrayObj instanceof short[]) {
            short[] primitiveArray = (short[]) arrayObj;
            for (short value : primitiveArray) {
                result.add((T) Short.valueOf(value));
            }
        } else if (arrayObj instanceof byte[]) {
            byte[] primitiveArray = (byte[]) arrayObj;
            for (byte value : primitiveArray) {
                result.add((T) Byte.valueOf(value));
            }
        } else if (arrayObj instanceof boolean[]) {
            boolean[] primitiveArray = (boolean[]) arrayObj;
            for (boolean value : primitiveArray) {
                result.add((T) Boolean.valueOf(value));
            }
        } else if (arrayObj instanceof Object[]) {
            // Handle Object arrays
            Object[] objects = (Object[]) arrayObj;
            for (Object obj : objects) {
                if (obj != null) {
                    result.add((T) obj);
                }
            }
        } else {
            // Fallback: try to convert to array
            int length = java.lang.reflect.Array.getLength(arrayObj);
            for (int i = 0; i < length; i++) {
                Object obj = java.lang.reflect.Array.get(arrayObj, i);
                if (obj != null) {
                    result.add((T) obj);
                }
            }
        }
        
        return result;
    }

    /**
     * Extract nested array from ResultSet as List of Lists.
     *
     * @param rs         the ResultSet
     * @param columnName the column name
     * @return list of lists of strings
     * @throws SQLException if extraction fails
     */
    public static List<List<String>> extractNestedArray(java.sql.ResultSet rs, String columnName) throws SQLException {
        java.sql.Array array = rs.getArray(columnName);
        if (array == null) {
            return List.of();
        }

        Object[] objects = (Object[]) array.getArray();
        List<List<String>> result = new java.util.ArrayList<>();
        for (Object obj : objects) {
            if (obj != null) {
                if (obj instanceof Object[]) {
                    Object[] innerArray = (Object[]) obj;
                    List<String> innerList = new java.util.ArrayList<>();
                    for (Object innerObj : innerArray) {
                        innerList.add(innerObj != null ? innerObj.toString() : null);
                    }
                    result.add(innerList);
                } else if (obj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> innerList = (List<String>) obj;
                    result.add(new java.util.ArrayList<>(innerList));
                }
            }
        }
        return result;
    }
}

