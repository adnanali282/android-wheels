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

import android.support.annotation.NonNull;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * String table, used with {@link CsvParser}
 *
 * @see CsvParser#parse(InputStream, char, String)
 * @see CsvParser#parse(String, char)
 */
public class StringTable implements Iterable<StringRow> {
    private final ArrayList<StringRow> mRows = new ArrayList<>();

    public StringTable() {
    }

    public StringTable(int rows, int columns) {
        for (int i = 0; i < rows; i++) {
            add(new StringRow(columns));
        }
    }

    public StringTable(@NonNull StringRow... rows) {
        for (StringRow row : rows) {
            add(row);
        }
    }

    public StringTable(@NonNull Iterable<StringRow> rows) {
        for (StringRow row : rows) {
            add(row);
        }
    }

    @Override
    public Iterator<StringRow> iterator() {
        return mRows.iterator();
    }

    /**
     * Row
     *
     * @param index Row position in table
     * @return Row at index
     */
    @NonNull
    public StringRow row(int index) {
        return mRows.get(index);
    }

    /**
     * Add empty row
     */
    public void add() {
        mRows.add(new StringRow());
    }

    /**
     * Add row to table
     *
     * @param row Row
     */
    public void add(@NonNull StringRow row) {
        mRows.add(Objects.requireNonNull(row));
    }

    public void add(@NonNull Object... cells) {
        mRows.add(new StringRow(cells));
    }

    public void add(@NonNull Iterable<Object> cells) {
        mRows.add(new StringRow(cells));
    }

    /**
     * Remove row from table
     *
     * @param index Row index
     * @return Removed row
     */
    @NonNull
    public StringRow remove(int index) {
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
