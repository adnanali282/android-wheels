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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Row of {@link Table}
 */
public class Row implements Iterable<String> {
    private final ArrayList<String> mCells = new ArrayList<>();

    Row(String row, char separator) {
        StringBuilder cell = new StringBuilder();
        boolean inQuotes = false;
        boolean inElementQuotes = false;
        int length = row.length();
        for (int i = 0; i < length; i++) {
            char current = row.charAt(i);
            if (current == separator && !inElementQuotes) {
                mCells.add(cell.toString());
                cell.delete(0, cell.length());
            } else if (current == CsvParser.QUOTE) {
                int n = i + 1;
                int p = i - 1;
                if ((p > -1 && row.charAt(p) == separator || i == 0) && !inElementQuotes) {
                    inElementQuotes = true;
                } else if ((n < length && row.charAt(n) == separator || n == length) &&
                        inElementQuotes) {
                    inElementQuotes = false;
                } else if (n < length && row.charAt(n) == CsvParser.QUOTE) {
                    cell.append(current);
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else {
                cell.append(current);
            }
        }
        mCells.add(cell.toString());
    }

    public Row() {
    }

    public Row(int cells) {
        for (int i = 0; i < cells; i++) {
            add();
        }
    }

    public Row(Object... cells) {
        for (Object cell : cells) {
            add(String.valueOf(cell));
        }
    }

    public Row(Iterable<Object> cells) {
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
    public String cell(int index) {
        return mCells.get(index);
    }

    /**
     * Add cell with null value to row
     */
    public void add() {
        mCells.add(null);
    }

    /**
     * Add cell to row
     *
     * @param cell Cell value
     */
    public void add(String cell) {
        mCells.add(cell);
    }

    /**
     * Remove cell from row
     *
     * @param index Column index
     * @return Removed cell value
     */
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
}
