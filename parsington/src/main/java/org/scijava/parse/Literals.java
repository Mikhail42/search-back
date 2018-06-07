/*
 * #%L
 * Parsington: the SciJava mathematical expression parser.
 * %%
 * Copyright (C) 2015 - 2016 Board of Regents of the University of
 * Wisconsin-Madison.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package org.scijava.parse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility methods for parsing literals from strings. These methods largely
 * conform to the Java specification's ideas of what constitutes a numeric or
 * string literal.
 *
 * @author Curtis Rueden
 */
public final class Literals {

	private static final Pattern DECIMAL = Pattern.compile("([0-9]+).*");

	private Literals() {
		// NB: Prevent instantiation of utility class.
	}

	/**
	 * Parses a string literal which is enclosed in single or double quotes.
	 * <p>
	 * For literals in double quotes, this parsing mechanism is intended to be as
	 * close as possible to the numeric literals supported by the Java programming
	 * language itself. Literals in single quotes are completely verbatim, with no
	 * escaping performed.
	 * </p>
	 *
	 * @param s The string from which the string literal should be parsed.
	 * @return The parsed string value, unescaped according to Java conventions.
	 *         Returns null if the string does not begin with a single or double
	 *         quote.
	 */
	public static String parseString(final CharSequence s) {
		return parseString(s, new Position());
	}

	/**
	 * Parses a decimal literal (integer or otherwise; e.g., {@code 1234567890},
	 * {@code 1234.0987} or {@code 1.2e34}).
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseDecimal(final CharSequence s) {
		return parseDecimal(s, new Position());
	}

	/**
	 * Parses a literal of any known type (booleans, strings and numbers).
	 *
	 * @param s The string from which the literal should be parsed.
	 * @return The parsed value, of a type consistent with Java's support for
	 *         literals: either {@link Boolean}, {@link String} or a concrete
	 *         {@link Number} subclass. Returns null if the string does
	 *         not match the syntax of a known literal.
	 * @see #parseString(CharSequence)
	 */
	public static Object parseLiteral(final CharSequence s) {
		return parseLiteral(s, new Position());
	}

	/**
	 * Parses a string literal which is enclosed in single or double quotes.
	 * <p>
	 * For literals in double quotes, this parsing mechanism is intended to be as
	 * close as possible to the numeric literals supported by the Java programming
	 * language itself. Literals in single quotes are completely verbatim, with no
	 * escaping performed.
	 * </p>
	 *
	 * @param s The string from which the string literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed string value, unescaped according to Java conventions.
	 *         Returns null if the string does not begin with a single or double
	 *         quote.
	 */
	public static String parseString(final CharSequence s, final Position pos) {
		final char quote = pos.ch(s);
		if (quote != '"' && quote != '\'') return null;
		int index = pos.get() + 1;

		boolean escaped = false;
		final StringBuilder sb = new StringBuilder();
		while (true) {
			if (index >= s.length()) pos.die("Unclosed string literal");
			final char c = s.charAt(index);

			if (escaped) {
				escaped = false;
				if (isOctal(c)) { // octal sequence
					String octal = "" + c;
					final char c1 = pos.ch(s, index + 1);
					if (isOctal(c1)) {
						octal += c1;
						if (c >= '0' && c <= '3') {
							final char c2 = pos.ch(s, index + 2);
							if (isOctal(c2)) octal += c2;
						}
					}
					sb.append((char) Integer.parseInt(octal, 8));
					index += octal.length();
					continue;
				}
				switch (c) {
					case 'b': // backspace
						sb.append('\b');
						break;
					case 't': // tab
						sb.append('\t');
						break;
					case 'n': // linefeed
						sb.append('\n');
						break;
					case 'f': // form feed
						sb.append('\f');
						break;
					case 'r': // carriage return
						sb.append('\r');
						break;
					case '"': // double quote
						sb.append('"');
						break;
					case '\\': // backslash
						sb.append('\\');
						break;
					case 'u': // unicode sequence
						final char u1 = hex(s, pos, index + 1);
						final char u2 = hex(s, pos, index + 2);
						final char u3 = hex(s, pos, index + 3);
						final char u4 = hex(s, pos, index + 4);
						sb.append((char) Integer.parseInt("" + u1 + u2 + u3 + u4, 16));
						index += 4;
						break;
					default: // invalid escape
						pos.die("Invalid escape sequence");
				}
			}
			else if (c == '\\' && quote == '"') escaped = true;
			else if (c == quote) break;
			else sb.append(c);
			index++;
		}
		pos.set(index + 1);
		return sb.toString();
	}

	/**
	 * Parses a decimal literal (e.g., {@code 1234.0987} or {@code 1.2e34}).
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseDecimal(final CharSequence s, final Position pos) {
		if (!isNumberSyntax(s, pos)) return null;
		final Matcher m = matcher(DECIMAL, s, pos);
		if (!m.matches()) return null;
		Number result = Integer.parseInt( m.group(1));
		return verifyResult(result, m, pos);
	}

	/**
	 * Parses a numeric literal of any known type.
	 * <p>
	 * This parsing mechanism is intended to be as close as possible to the
	 * numeric literals supported by the Java programming language itself.
	 * </p>
	 *
	 * @param s The string from which the numeric literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed numeric value, of a type consistent with Java's support
	 *         for numeric primitives&mdash;or for values outside the normal range
	 *         of Java primitives, {@link BigInteger} or {@link BigDecimal} as
	 *         appropriate. Returns null if the string does not begin with the
	 *         numeric literal telltale of a 0-9 digit with optional minus.
	 */
	public static Number parseNumber(final CharSequence s, final Position pos) {
		final Number decimal = parseDecimal(s, pos);
		if (decimal != null) return decimal;
		return null;
	}

	/**
	 * Parses a literal of any known type (booleans, strings and numbers).
	 *
	 * @param s The string from which the literal should be parsed.
	 * @param pos The offset from which the literal should be parsed. If parsing
	 *          is successful, the position will be advanced to the next index
	 *          after the parsed literal.
	 * @return The parsed value, of a type consistent with Java's support for
	 *         literals: either {@link Boolean}, {@link String} or a concrete
	 *         {@link Number} subclass. Returns null if the string does
	 *         not match the syntax of a known literal.
	 * @see #parseString(CharSequence, Position)
	 * @see #parseNumber(CharSequence, Position)
	 */
	public static Object parseLiteral(final CharSequence s, final Position pos) {
		final String str = parseString(s, pos);
		if (str != null) return str;

		final Number num = parseNumber(s, pos);
		if (num != null) return num;

		return null;
	}

	// -- Helper methods --

	private static boolean isOctal(final char c) {
		return c >= '0' && c <= '7';
	}

	private static char hex(final CharSequence s, final Position pos,
							final int index)
	{
		final char c = pos.ch(s, index);
		if (c >= '0' && c <= '9') return c;
		if (c >= 'a' && c <= 'f') return c;
		if (c >= 'A' && c <= 'F') return c;
		pos.die("Invalid unicode sequence");
		return '\0'; // NB: Unreachable.
	}

	private static boolean
	isNumberSyntax(final CharSequence s, final Position pos)
	{
		final int i = pos.get();
		final boolean sign = s.charAt(i) == '-' || s.charAt(i) == '+';
		final char digit = s.charAt(sign ? i + 1 : i);
		return digit >= '0' && digit <= '9';
	}

	private static Matcher matcher(final Pattern p, final CharSequence s, final Position pos)
	{
		return p.matcher(sub(s, pos));
	}

	private static CharSequence sub(final CharSequence s, final Position pos) {
		return pos.get() == 0 ? s : new SubSequence(s, pos.get());
	}

	private static Number verifyResult(final Number result, final Matcher m,
									   final Position pos)
	{
		if (result == null) pos.die("Illegal numeric literal");
		pos.inc(m.group(1).length());
		return result;
	}
}