/*
 * Copyright (C) 2013-2016 Ruediger Lunde
 * Licensed under the GNU General Public License, Version 3
 */
package rl.photoviewer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a propositional sentence to control visibility of photos marked
 * with keywords. Keywords are used as symbols. Syntax is restricted to a
 * conjunctive normal form (CNF) which is conjunction of clauses. Clauses are
 * disjunctions of possibly negated symbols.
 * 
 * @author Ruediger Lunde
 */
public class KeywordExpression {

	/** Contains at least one clause. */
	private List<List<Literal>> clauses;

	public KeywordExpression() {
		clauses = new ArrayList<>();
		addClause();
	}

	/** Adds a new literal the first clause. */
	public void addLiteral(String symbol, boolean negated) {
		clauses.get(clauses.size()-1).add(new Literal(symbol, negated));
	}

	/** Adds a new empty clause (here denoting true!) at first position. */
	public void addClause() {
		clauses.add(new ArrayList<>());
	}

	public void deleteLastClause() {
		if (!clauses.isEmpty())
			clauses.remove(clauses.size() - 1);
		if (clauses.isEmpty())
			addClause();
	}
	
	/**
	 * Removes all clauses and adds a dummy clause denoting true at first
	 * position.
	 */
	public void clear() {
		clauses.clear();
		addClause();
	}

	/**
	 * Checks whether the provided list of photo keywords passes the filter.
	 */
	public boolean checkKeywords(List<String> photoKeywords) {
		for (List<Literal> literals : clauses) {
			if (!literals.isEmpty()) {
				boolean clauseValue = false;
				for (Literal l : literals) {
					if (l.isNegated && !photoKeywords.contains(l.symbol)
							|| !l.isNegated && photoKeywords.contains(l.symbol)) {
						clauseValue = true;
						break;
					}
				}
				if (!clauseValue)
					return false;
			}
		}
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || getClass() != o.getClass())
			return false;
		else {
			KeywordExpression ve = (KeywordExpression) o;
			return clauses.equals(ve.clauses);
		}
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (int i = 0; i < clauses.size(); i++) {
			if (i > 0)
				result.append("and\n");
			List<Literal> literals = clauses.get(i);
			result.append(" ");
			if (literals.isEmpty())
				result.append(" true");
			for (int j = 0; j < literals.size(); j++) {
				Literal l = literals.get(j);
				if (j > 0)
					result.append(" or");
				if (l.isNegated)
					result.append(" not");
				result.append(" ").append(l.symbol);
			}
			result.append("\n");
		}
		return result.toString();
	}

	// ///////////////////////////////////////////////////////////////////////
	// nested classes

	private class Literal {
		String symbol;
		boolean isNegated;

		Literal(String sym, boolean neg) {
			symbol = sym;
			isNegated = neg;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || getClass() != o.getClass())
				return false;
			else {
				Literal l = (Literal) o;
				return symbol.equals(l.symbol) && isNegated == l.isNegated;
			}
		}
	}
}
