/**
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2016 Yuriy Budiyev
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.budiyev.wheels;

import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Yuriy Budiyev (yuriy.budiyev@yandex.ru)
 * @link https://github.com/yuriy-budiyev/Wheels
 */
public final class CsvParser {
    private static final char QUOTE = '\"';
    private static final char CR = '\r';
    private static final char LF = '\n';

    private CsvParser() {
    }

    @NonNull
    public static Table parse(@NonNull String string, char separator) {
        return new Table(string, separator);
    }

    @NonNull
    public static Table parse(@NonNull InputStream inputStream, char separator) {
        return new Table(inputStream, separator);
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
                        row.append(current);
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
                            row.append(current);
                            previous = -1;
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
        private final ArrayList<String> mColumns = new ArrayList<>();
        private final char mSeparator;

        private Row(String row, char separator) {
            mSeparator = separator;
            StringBuilder column = new StringBuilder();
            boolean inQuotes = false;
            int length = row.length();
            for (int i = 0; i < length; i++) {
                char current = row.charAt(i);
                if (current == separator && !inQuotes) {
                    mColumns.add(column.toString());
                    column.delete(0, column.length());
                } else if (current == QUOTE) {
                    int n = i + 1;
                    if (n < length && row.charAt(n) == QUOTE) {
                        column.append(current);
                        i++;
                    } else {
                        inQuotes = !inQuotes;
                    }
                } else {
                    column.append(current);
                }
            }
            if (column.length() != 0) {
                mColumns.add(column.toString());
            }
        }

        /**
         * Column
         *
         * @param index Column position in row
         * @return Column at index
         */
        public String column(int index) {
            return mColumns.get(index);
        }

        /**
         * Remove column from current row
         *
         * @param index Column index
         * @return Removed column value
         */
        public String remove(int index) {
            return mColumns.remove(index);
        }

        /**
         * Size of row
         *
         * @return Columns count
         */
        public int size() {
            return mColumns.size();
        }

        @Override
        public Iterator<String> iterator() {
            return mColumns.iterator();
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder();
            int size = mColumns.size();
            for (int i = 0; i < size; i++) {
                stringBuilder.append(column(i));
                if (i < size - 1) {
                    stringBuilder.append(mSeparator).append(" ");
                }
            }
            return stringBuilder.toString();
        }
    }
}
