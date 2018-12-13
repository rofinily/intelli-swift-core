package com.fr.swift.base.json.writer.impl;

import com.fr.swift.base.json.writer.JsonWriter;

import java.io.IOException;
import java.util.Collection;

/**
 * @author yee
 * @date 2018-12-12
 */
public class CollectionJsonWriter implements JsonWriter<Collection> {
    private static final ObjectJsonWriter OBJECT_JSON_WRITER = new ObjectJsonWriter();

    @Override
    public String write(Collection collection) throws IOException {
        StringBuffer buffer = new StringBuffer(1024);
        writeCollection(buffer, collection);
        return buffer.toString();
    }

    private void writeCollection(StringBuffer buffer, Collection list) throws IOException {
        if (list == null) {
            buffer.append("null");
            return;
        }
        if (list.isEmpty()) {
            buffer.append("[]");
            return;
        }
        buffer.append('[');
        boolean isFirst = true;
        for (Object o : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                buffer.append(',');
            }
            buffer.append(OBJECT_JSON_WRITER.write(o));
        }
        buffer.append(']');
    }
}
