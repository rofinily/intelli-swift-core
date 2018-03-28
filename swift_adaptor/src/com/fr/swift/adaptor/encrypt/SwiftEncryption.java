package com.fr.swift.adaptor.encrypt;

/**
 * This class created on 2018/3/23
 *
 * @author Lucifer
 * @description
 * @since Advanced FineBI Analysis 1.0
 */
public class SwiftEncryption {

    private static int HEAD_LENGTH = 4;
    private static int HEAD_START = 0;
    private static int HEAD_END = 4;

    //拼fieldId
    public static String encryptFieldId(String tableId, String fieldName) {
        return formatString(tableId.length(), HEAD_LENGTH) + tableId + fieldName;
    }

    //fieldId解析成tableid和columnName
    public static String[] decryptFieldId(String fieldId) {
        int tableIdLength = Integer.valueOf(fieldId.substring(HEAD_START, HEAD_END));
        String tableId = fieldId.substring(HEAD_END, HEAD_END + tableIdLength);
        String fieldName = fieldId.substring(HEAD_END + tableIdLength, fieldId.length());
        return new String[]{tableId, fieldName};
    }

    public static String formatString(Object object, int length) {
        return String.format("%0" + length + "d", object);
    }
}
