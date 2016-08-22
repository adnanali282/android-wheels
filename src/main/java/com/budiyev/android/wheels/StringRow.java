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
 * Row of {@link StringTable}
 *
 * @see CsvParser#parse(InputStream, char, String)
 * @see CsvParser#parse(String, char)
 */
public class StringRow implements Iterable<String> {
    private static final String EMPTY = "";
    private final ArrayList<String> mCells = new ArrayList<>();

    public StringRow() {
    }

    public StringRow(int cells) {
        for (int i = 0; i < cells; i++) {
            add();
        }
    }

    public StringRow(@NonNull Object... cells) {
        for (Object cell : cells) {
            add(String.valueOf(cell));
        }
    }

    public StringRow(@NonNull Iterable<Object> cells) {
        for (Object cell : cells) {
            add(String.valueOf(cell));
        }
    }

    @Override
    public Iterator<String> iterator() {
        return mCells.iterator();
    }

    /**
     * Column
     *
     * @param index Column position in row
     * @return Column at index
     */
    @NonNull
    public String cell(int index) {
        return mCells.get(index);
    }

    /**
     * Add cell with null value to row
     */
    public void add() {
        mCells.add(EMPTY);
    }

    /**
     * Add cell to row
     *
     * @param cell Cell value
     */
    public void add(@NonNull String cell) {
        mCells.add(Objects.requireNonNull(cell));
    }

    /**
     * Set or add cell to specified position (column)
     * <br>
     * If {@code position} is greater than or equal to {@link StringRow#size()},
     * empty cells will be inserted.
     *
     * @param position Position to set cell value
     * @param cell     Cell value
     * @return Previous value or {@code null}
     */
    @Nullable
    public String set(int position, @NonNull String cell) {
        if (position < 0) {
            throw new IllegalArgumentException();
        }
        Objects.requireNonNull(cell);
        int size = mCells.size();
        if (position >= size) {
            int empty = position - size;
            for (int i = 0; i < empty; i++) {
                mCells.add(EMPTY);
            }
            mCells.add(cell);
            return null;
        } else {
            return mCells.set(position, cell);
        }
    }

    /**
     * Remove cell from row
     *
     * @param index Column index
     * @return Removed cell value
     */
    @NonNull
    public String remove(int index) {
        return mCells.remove(index);
    }

    /**
     * Size of row
     *
     * @return Columns count
     */
    public int size() {
        return mCells.size();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof StringRow) {
            StringRow other = (StringRow) o;
            int size = size();
            if (other.size() == size) {
                for (int i = 0; i < size; i++) {
                    if (!Objects.equals(other.cell(i), cell(i))) {
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
        for (String cell : this) {
            hashCode ^= cell.hashCode();
        }
        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("StringRow [");
        for (int i = 0, s = size(); i < s; i++) {
            stringBuilder.append(cell(i));
            if (i < s - 1) {
                stringBuilder.append(", ");
            }
        }
        return stringBuilder.append(']').toString();
    }
}
