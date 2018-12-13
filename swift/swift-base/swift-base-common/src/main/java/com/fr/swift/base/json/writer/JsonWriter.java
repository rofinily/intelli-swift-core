package com.fr.swift.base.json.writer;

import java.io.IOException;

/**
 * @author yee
 * @date 2018-12-12
 */
public interface JsonWriter<T> {
    String write(T t) throws IOException;
}
