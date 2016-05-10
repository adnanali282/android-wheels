/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2016 Yuriy Budiyev
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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Yuriy Budiyev (yuriy.budiyev@yandex.ru)
 * @link https://github.com/yuriy-budiyev/android-wheels
 */
public final class CsvParser {
    private static final String QUOTE_STRING = "\"";
    private static final String DOUBLE_QUOTE_STRING = "\"\"";
    private static final char QUOTE = '\"';
    private static final char CR = '\r';
    private static final char LF = '\n';

    private CsvParser() {
    }

    public static boolean encode(Table table, OutputStream outputStream, char separator) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, "UTF-8"))) {
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

    @NonNull
    public static Table parse(InputStream inputStream, char separator) {
        return new Table(inputStream, separator);
    }

    @NonNull
    public static Table parse(String string, char separator) {
        return new Table(string, separator);
    }

    public static class Table implements Iterable<Row> {
        private final ArrayList<Row> mRows = new ArrayList<>();

        private Table(String table, char separator) {
            StringBuilder row = new StringBuilder();
            boolean inQuotes = false;
            int length = table.length();
            for (int i = 0; i < length; i++) {
                char current = table.charAt(i);
                if (current == LF && !inQuotes) {
                    mRows.add(new Row(row.toString(), separator));
                    row.delete(0, row.length());
                } else if (current == QUOTE) {
                    int n = i + 1;
                    if (n < length && table.charAt(n) == QUOTE) {
                        row.append(current).append(current);
                        i++;
                    } else {
                        inQuotes = !inQuotes;
                    }
                } else {
                    if (current != CR) {
                        row.append(current);
                    }
                }
            }
            if (row.length() != 0) {
                mRows.add(new Row(row.toString(), separator));
            }
        }

        private Table(InputStream table, char separator) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(table))) {
                StringBuilder row = new StringBuilder();
                boolean inQuotes = false;
                int previous = -1;
                for (; ; ) {
                    int c = reader.read();
                    char current;
                    if (c == -1) {
                        break;
                    } else {
                        current = (char) c;
                    }
                    if (current == LF && !inQuotes) {
                        mRows.add(new Row(row.toString(), separator));
                        row.delete(0, row.length());
                        previous = -1;
                    } else if (current == QUOTE) {
                        if (previous != -1 && (char) previous == QUOTE) {
                            row.append(current).append(current);
                            previous = -1;
                        } else {
                            previous = (int) current;
                        }
                        inQuotes = !inQuotes;
                    } else {
                        if (current != CR) {
                            row.append(current);
                            previous = (int) current;
                        }
                    }
                }
                if (row.length() != 0) {
                    mRows.add(new Row(row.toString(), separator));
                }
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

    public static class Row implements Iterable<String> {
        private final ArrayList<String> mCells = new ArrayList<>();

        private Row(String row, char separator) {
            StringBuilder cell = new StringBuilder();
            boolean inQuotes = false;
            int length = row.length();
            for (int i = 0; i < length; i++) {
                char current = row.charAt(i);
                if (current == separator && !inQuotes) {
                    mCells.add(cell.toString());
                    cell.delete(0, cell.length());
                } else if (current == QUOTE) {
                    int n = i + 1;
                    if (n < length && row.charAt(n) == QUOTE) {
                        cell.append(current);
                        i++;
                    } else {
                        inQuotes = !inQuotes;
                    }
                } else {
                    cell.append(current);
                }
            }
            if (cell.length() != 0) {
                mCells.add(cell.toString());
            }
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
}
