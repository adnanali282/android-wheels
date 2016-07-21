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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * String table, used with {@link CsvParser}
 */
public class Table implements Iterable<Row> {
    private final ArrayList<Row> mRows = new ArrayList<>();

    Table(String table, char separator) {
        StringBuilder row = new StringBuilder();
        boolean inQuotes = false;
        int length = table.length();
        for (int i = 0; i < length; i++) {
            char current = table.charAt(i);
            if (current == CsvParser.LF && !inQuotes) {
                mRows.add(new Row(row.toString(), separator));
                row.delete(0, row.length());
            } else {
                if (current == CsvParser.QUOTE) {
                    inQuotes = !inQuotes;
                }
                row.append(current);
            }
        }
        mRows.add(new Row(row.toString(), separator));
    }

    Table(InputStream table, char separator, String charset) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(table, charset))) {
            StringBuilder row = new StringBuilder();
            boolean inQuotes = false;
            for (; ; ) {
                int c = reader.read();
                char current;
                if (c == -1) {
                    break;
                } else {
                    current = (char) c;
                }
                if (current == CsvParser.LF && !inQuotes) {
                    mRows.add(new Row(row.toString(), separator));
                    row.delete(0, row.length());
                } else {
                    if (current == CsvParser.QUOTE) {
                        inQuotes = !inQuotes;
                    }
                    row.append(current);
                }
            }
            mRows.add(new Row(row.toString(), separator));
        } catch (IOException ignored) {
        }
    }

    public Table() {
    }

    public Table(int rows, int columns) {
        for (int i = 0; i < rows; i++) {
            add(new Row(columns));
        }
    }

    public Table(Row... rows) {
        for (Row row : rows) {
            add(row);
        }
    }

    public Table(Iterable<Row> rows) {
        for (Row row : rows) {
            add(row);
        }
    }

    @Override
    public Iterator<Row> iterator() {
        return mRows.iterator();
    }

    /**
     * Row
     *
     * @param index Row position in table
     * @return Row at index
     */
    public Row row(int index) {
        return mRows.get(index);
    }

    /**
     * Add empty row
     */
    public void add() {
        mRows.add(new Row());
    }

    /**
     * Add row to table
     *
     * @param row Row
     */
    public void add(Row row) {
        mRows.add(row);
    }

    /**
     * Remove row from table
     *
     * @param index Row index
     * @return Removed row
     */
    public Row remove(int index) {
        return mRows.remove(index);
    }

    /**
     * Size of table
     *
     * @return Rows count
     */
    public int size() {
        return mRows.size();
    }
}
