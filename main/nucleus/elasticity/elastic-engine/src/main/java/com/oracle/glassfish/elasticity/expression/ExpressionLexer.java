/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2009-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */
package com.oracle.glassfish.elasticity.expression;

import org.omg.CORBA.IdentifierHelper;

import com.oracle.glassfish.elasticity.expression.Token.TokenId;

/**
 * Lexer
 * 
 * @author mk
 *
 */
public class ExpressionLexer {

	private CharSequence stream;
	
	private int index = 0;
	
	private int size;

	public static final Token EOSTREAM = new TokenImpl(TokenId.EOSTREAM, "");
	public static final Token MULT = new TokenImpl(TokenId.MULT, "*");
	public static final Token DIV = new TokenImpl(TokenId.DIV, "/");
	public static final Token PLUS = new TokenImpl(TokenId.PLUS, "+");
	public static final Token MINUS = new TokenImpl(TokenId.MINUS, "-");
	public static final Token OPAR = new TokenImpl(TokenId.OPAR, "(");
	public static final Token CPAR = new TokenImpl(TokenId.CPAR, ")");
	public static final Token OBRACE = new TokenImpl(TokenId.OBRACE, "{");
	public static final Token CBRACE = new TokenImpl(TokenId.CBRACE, "}");
	public static final Token DOT = new TokenImpl(TokenId.DOT, ".");

	public static final Token LAND = new TokenImpl(TokenId.LOGICAL_AND, "&&");
	public static final Token LOR = new TokenImpl(TokenId.LOGICAL_OR, "||");
	public static final Token AND = new TokenImpl(TokenId.BIT_AND, "&");
	public static final Token OR = new TokenImpl(TokenId.BIT_OR, "|");

	public static final Token TRUE = new TokenImpl(TokenId.TRUE, "true");
	public static final Token FALSE = new TokenImpl(TokenId.FALSE, "false");
	
	public static final Token LT = new TokenImpl(TokenId.LT, "<");
	public static final Token LTE = new TokenImpl(TokenId.LTE, "<=");
	public static final Token GT = new TokenImpl(TokenId.LT, ">");
	public static final Token GTE = new TokenImpl(TokenId.LTE, ">=");
	public static final Token EQ = new TokenImpl(TokenId.EQ, "=");
	public static final Token EQEQ = new TokenImpl(TokenId.EQEQ, "==");
	
	public ExpressionLexer(CharSequence stream) {
		this.stream = stream;
		this.index = 0;
		this.size = stream.length();
	}
	
	public Token next() {
		Token tok = EOSTREAM;
		if (index >= size) {
			tok = EOSTREAM;
		} else if (Character.isJavaIdentifierStart(stream.charAt(index))) {
			for (int p = index; index < size; index++) {
				if (! Character.isJavaIdentifierPart(stream.charAt(index))) {
					String value = stream.subSequence(p,  index).toString();
					TokenId tokId = "true".equals(value) 
							? TokenId.TRUE
							: ("false".equals(value) ? TokenId.FALSE : TokenId.IDENTIFIER);
					tok = new TokenImpl(tokId, value);
					break;
				}
			}
		} else if (Character.isDigit(stream.charAt(index))) {
			int startIndex = index;
			boolean isDouble = false;
			for (int p = index; index < size; index++) {
				if (! Character.isDigit(stream.charAt(index))) {
					break;
				}
			}	

			if (stream.charAt(index) == '.') {
				isDouble = true;
				for (int p = ++index; index < size; index++) {
					if (! Character.isDigit(stream.charAt(index))) {
						break;
					}
				}	
			}
			
			tok = new TokenImpl(
					isDouble ? TokenId.DOUBLE : TokenId.INTEGER,
							stream.subSequence(startIndex,  index).toString());
		} else if (Character.isWhitespace(stream.charAt(index))) {
			for (int p = index; index < size; index++) {
				if (! Character.isWhitespace(stream.charAt(index))) {
					tok = new TokenImpl(TokenId.WHITESPACE, stream.subSequence(p,  index).toString());
					break;
				}
			}
			
			//return tok OR call nextToken() to eat whitespace
			return next();
		} else {
			switch (stream.charAt(index)) {
			case '*' : 
				tok = MULT;
				index++;
				break;
			case '/' :  
				tok = DIV;
				index++;
				break;
			case '+' : 
				tok = PLUS;
				index++;
				break;
			case '-' : 
				tok = MINUS;
				index++;
				break;
			case '(' : 
				tok = OPAR;
				index++;
				break;
			case ')' : 
				tok = CPAR;
				index++;
				break;
			case '{' : 
				tok = OBRACE;
				index++;
				break;
			case '}' : 
				tok = CBRACE;
				index++;
				break;
			case '.' : 
				tok = DOT;
				index++;
				break;
			case '>' :
				tok = GT;
				index++;
				if (index < size && stream.charAt(index) == '=') {
					index++;
					tok = GTE;
				}
				break;
			case '<' :
				tok = LT;
				index++;
				if (index < size && stream.charAt(index) == '=') {
					index++;
					tok = LTE;
				}
				break;
			case '=' :
				tok = EQ;
				index++;
				if (index < size && stream.charAt(index) == '=') {
					index++;
					tok = EQEQ;
				}
				break;
			case '&' :
				tok = AND;
				index++;
				if (index < size && stream.charAt(index) == '&') {
					index++;
					tok = LAND;
				}
				break;
			case '|' :
				tok = OR;
				index++;
				if (index < size && stream.charAt(index) == '|') {
					index++;
					tok = LOR;
				}
				break;
			default: tok = new TokenImpl(TokenId.UNKNOWN, "" + stream.charAt(index++));
			}
		}
		
		return tok;
	}
	
	private static final class TokenImpl
		implements Token {
		
		private TokenId id;
		private String value;
		
		
		public TokenImpl(TokenId id, String value) {
			super();
			this.id = id;
			this.value = value;
		}
		
		public TokenId getTokenId() {
			return Token.TokenId.IDENTIFIER; 
		}
		public String value() {
			return value;
		}		
		
		public String toString() {
			return value;
		}
	}
	
}
