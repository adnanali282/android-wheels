/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import android.support.annotation.NonNull;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

/**
 * Parser of CSV format
 */
public final class CsvParser {
    static final String QUOTE_STRING = "\"";
    static final String DOUBLE_QUOTE_STRING = "\"\"";
    static final char QUOTE = '\"';
    static final char LF = '\n';

    private CsvParser() {
    }

    /**
     * Encode {@link Table} in CSV format
     *
     * @param table        Table
     * @param outputStream Stream to save result
     * @param separator    Column separator
     * @param encoding     Text encoding
     * @return true if success, false otherwise
     */
    public static boolean encode(Table table, OutputStream outputStream, char separator,
            String encoding) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, encoding))) {
            for (Row row : table) {
                int size = row.size();
                for (int i = 0; i < size; i++) {
                    writer.append(QUOTE)
                            .append(row.cell(i).replace(QUOTE_STRING, DOUBLE_QUOTE_STRING))
                            .append(QUOTE);
                    if (i != size - 1) {
                        writer.append(separator);
                    }
                }
                writer.append(LF);
            }
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Encode {@link Table} in CSV format
     *
     * @param table     Table
     * @param separator Column separator
     * @return Encoded string
     */
    @NonNull
    public static String encode(Table table, char separator) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Row row : table) {
            int size = row.size();
            for (int i = 0; i < size; i++) {
                stringBuilder.append(QUOTE)
                        .append(row.cell(i).replace(QUOTE_STRING, DOUBLE_QUOTE_STRING))
                        .append(QUOTE);
                if (i != size - 1) {
                    stringBuilder.append(separator);
                }
            }
            stringBuilder.append(LF);
        }
        return stringBuilder.toString();
    }

    /**
     * Parse CSV into {@link Table}
     *
     * @param inputStream Source data stream
     * @param separator   Column separator
     * @param encoding    Text encoding
     * @return Table
     */
    @NonNull
    public static Table parse(InputStream inputStream, char separator, String encoding) {
        return new Table(inputStream, separator, encoding);
    }

    /**
     * Parse CSV into {@link Table}
     *
     * @param string    Source string
     * @param separator Column separator
     * @return Table
     */
    @NonNull
    public static Table parse(String string, char separator) {
        return new Table(string, separator);
    }
}
