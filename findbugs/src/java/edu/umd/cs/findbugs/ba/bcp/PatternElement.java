/*
 * Bytecode Analysis Framework
 * Copyright (C) 2003, University of Maryland
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package edu.umd.cs.daveho.ba.bcp;

import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.ConstantPoolGen;
import edu.umd.cs.daveho.ba.Edge;
import edu.umd.cs.daveho.ba.ValueNumberFrame;
import edu.umd.cs.daveho.ba.DataflowAnalysisException;

/**
 * A PatternElement is an element of a ByteCodePattern.
 * It potentially matches some number of bytecode instructions.
 */
public abstract class PatternElement {
	private static final boolean DEBUG = Boolean.getBoolean("bcp.debug");

	private PatternElement next;
	private String label;
	private int index;

	/**
	 * Get the next PatternElement.
	 */
	public PatternElement getNext() {
		return next;
	}

	/**
	 * Set the next PatternElement.
	 */
	public void setNext(PatternElement patternElement) {
		this.next = patternElement;
	}

	/**
	 * Set a label for this PatternElement.
	 * @param label the label
	 * @return this object
	 */
	public PatternElement label(String label) {
		this.label = label;
		return this;
	}

	/**
	 * Get the label of this PatternElement.
	 * @return the label, or null if the PatternElement is not labeled
	 */
	public String getLabel() { return label; }

	/**
	 * Set the index.  This is just for debugging.
	 */
	public void setIndex(int index) {
		this.index = index;
	}

	/**
	 * Look up a variable definition in given BindingSet.
	 * @param varName the name of the variable
	 * @param bindingSet the BindingSet to look in
	 * @return the Variable, or null if no Variable is bound to the name
	 */
	public static Variable lookup(String varName, BindingSet bindingSet) {
		if (bindingSet == null)
			return null;
		Binding binding = bindingSet.lookup(varName);
		return (binding != null) ? binding.getVariable() : null;
	}

	/**
	 * Return whether or not this element matches the given
	 * instruction with the given Bindings in effect.
	 * @param handle the instruction
	 * @param cpg the ConstantPoolGen from the method
	 * @param before the ValueNumberFrame representing values in the Java stack frame
	 *   just before the execution of the instruction
	 * @param after the ValueNumberFrame representing values in the Java stack frame
	 *   just after the execution of the instruction
	 * @param bindingSet the set of Bindings
	 * @return if the match is successful, returns a MatchResult with the PatternElement
	 *   and BindingSet; if the match is not successful, returns null
	 */
	public abstract MatchResult match(InstructionHandle handle, ConstantPoolGen cpg,
		ValueNumberFrame before, ValueNumberFrame after, BindingSet bindingSet) throws DataflowAnalysisException;

	/**
	 * Return whether or not it is acceptable to take the given branch.
	 * @param edge the Edge representing the branch
	 * @param source the source instruction of the branch
	 * @return true if the Edge is acceptable, false if not
	 */
	public abstract boolean acceptBranch(Edge edge, InstructionHandle source);

	/**
	 * Return the minimum number of instructions this PatternElement
	 * must match in the ByteCodePattern.
	 */
	public abstract int minOccur();

	/**
	 * Return the maximum number of instructions this PatternElement
	 * must match in the ByteCodePattern.
	 */
	public abstract int maxOccur();

	/**
	 * Add a variable definition to the given BindingSet, or if
	 * there is an existing definition, make sure it is consistent with
	 * the new definition.
	 * @param varName the name of the variable
	 * @param variable the Variable which should be added or checked for consistency
	 * @param bindingSet the existing set of bindings
	 * @return the updated BindingSet (if the variable is consistent with the
	 *   previous bindings), or null if the new variable is inconsistent with
	 *   the previous bindings
	 */
	protected static BindingSet addOrCheckDefinition(String varName, Variable variable, BindingSet bindingSet) {
		Variable existingVariable = lookup(varName, bindingSet);
		if (existingVariable == null) {
			bindingSet = new BindingSet(new Binding(varName, variable), bindingSet);
		} else {
			if (!existingVariable.sameAs(variable)) {
				if (DEBUG) System.out.println("\tConflicting variable " + varName + ": " + variable + " != " + existingVariable);
				return null;
			}
		}

		return bindingSet;
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		String className = this.getClass().getName();
		buf.append(className.substring(className.lastIndexOf('.') + 1));
		buf.append('(');
		buf.append(index);
		buf.append(')');
		return buf.toString();
	}
}

// vim:ts=4
