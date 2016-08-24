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
import android.support.annotation.Nullable;

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

    /**
     * Add row that contains specified cells
     *
     * @param cells Cells
     */
    public void add(@NonNull Object... cells) {
        mRows.add(new StringRow(cells));
    }

    /**
     * Add row that contains specified cells
     *
     * @param cells Cells
     */
    public void add(@NonNull Iterable<Object> cells) {
        mRows.add(new StringRow(cells));
    }

    /**
     * Set or add row to specified position
     * <br>
     * If {@code position} is greater than or equal to {@link StringTable#size()},
     * empty rows will be inserted.
     *
     * @param position Position to set row
     * @param cells    Cells
     * @return Previous value or {@code null}
     */
    @Nullable
    public StringRow set(int position, @NonNull Object... cells) {
        return set(position, new StringRow(cells));
    }

    /**
     * Set or add row to specified position
     * <br>
     * If {@code position} is greater than or equal to {@link StringTable#size()},
     * empty rows will be inserted.
     *
     * @param position Position to set row
     * @param cells    Cells
     * @return Previous value or {@code null}
     */
    @Nullable
    public StringRow set(int position, @NonNull Iterable<Object> cells) {
        return set(position, new StringRow(cells));
    }

    /**
     * Set or add row to specified position
     * <br>
     * If {@code position} is greater than or equal to {@link StringTable#size()},
     * empty rows will be inserted.
     *
     * @param position Position to set row
     * @param row      Row
     * @return Previous value or {@code null}
     */
    @Nullable
    public StringRow set(int position, @NonNull StringRow row) {
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        Objects.requireNonNull(row);
        int size = mRows.size();
        if (position >= size) {
            int empty = position - size;
            for (int i = 0; i < empty; i++) {
                mRows.add(new StringRow());
            }
            mRows.add(row);
            return null;
        } else {
            return mRows.set(position, row);
        }
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

    /**
     * Clear table completely
     */
    public void clear() {
        clearCells();
        clearRows();
    }

    /**
     * Delete all cells from table (leave all rows empty)
     */
    public void clearCells() {
        for (StringRow row : mRows) {
            row.clear();
        }
    }

    /**
     * Delete all rows from table (don't touch cells)
     */
    public void clearRows() {
        mRows.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof StringTable) {
            StringTable other = ((StringTable) o);
            int size = size();
            if (other.size() == size) {
                for (int i = 0; i < size; i++) {
                    if (!Objects.equals(other.row(i), row(i))) {
                        return false;
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hashCode = Integer.MAX_VALUE;
        for (StringRow row : this) {
            hashCode ^= row.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("StringTable [");
        for (int i = 0, s = size(); i < s; i++) {
            stringBuilder.append(System.lineSeparator()).append(row(i));
        }
        return stringBuilder.append(']').toString();
    }
}
