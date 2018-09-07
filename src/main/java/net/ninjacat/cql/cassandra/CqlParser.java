/*
 * Copyright 2014 Oleksiy Voronin <ovoronin@gmail.com>
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package net.ninjacat.cql.cassandra;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * Simple parser to split input with multiple statements
 */
final class CqlParser {
    private static final String END_OF_STATEMENT = ";";
    private static final String QUOTE = "\"";
    private static final String SQUOTE = "'";
    private static final String DELIMITERS = QUOTE + SQUOTE + END_OF_STATEMENT;
    private static final Set<String> QUOTE_SET = new HashSet<String>();

    static {
        QUOTE_SET.add(QUOTE);
        QUOTE_SET.add(SQUOTE);
    }

    private final StringTokenizer tokenizer;
    private String delimiter;

    private CqlParser(final String cql) {
        this.tokenizer = new StringTokenizer(cql, DELIMITERS, true);
        this.delimiter = DELIMITERS;
    }

    static CqlParser forScript(final String sql) {
        return new CqlParser(sql);
    }

    private boolean hasMoreTokens() {
        return this.tokenizer.hasMoreTokens();
    }

    private String nextStatement() {
        final StringBuilder statement = new StringBuilder();
        String token = null;
        while (!END_OF_STATEMENT.equals(token)) {
            token = next();
            if (token != null)
                statement.append(token);
            else
                break;
        }
        return statement.toString();
    }

    private String next() {
        if (!hasMoreTokens())
            return null;
        final String token = this.tokenizer.nextToken(this.delimiter);
        if (QUOTE_SET.contains(token)) {
            if (token.equals(this.delimiter)) {
                this.delimiter = DELIMITERS;
            } else {
                this.delimiter = token;
            }
        }
        return token;
    }

    private Iterator<String> statementIterator() {
        return new Iterator<String>() {
            @Override
            public boolean hasNext() {
                return hasMoreTokens();
            }

            @Override
            public String next() {
                return nextStatement();
            }
        };
    }

    /**
     * Get statements from source as Iterable
     *
     * @return Iterable of statements
     */
    Iterable<String> statements() {
        return this::statementIterator;
    }
}
