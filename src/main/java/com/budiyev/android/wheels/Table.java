/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.android.wheels;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * String table, used with {@link CsvParser}
 *
 * @see CsvParser#parse(InputStream, char, String)
 * @see CsvParser#parse(String, char)
 */
public class Table implements Iterable<Row> {
    private final ArrayList<Row> mRows = new ArrayList<>();

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
