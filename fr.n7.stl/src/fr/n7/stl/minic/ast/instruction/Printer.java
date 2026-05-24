/**
 * 
 */
package fr.n7.stl.minic.ast.instruction;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Library;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Implementation of the Abstract Syntax Tree node for a printer instruction.
 * @author Marc Pantel
 *
 */
public class Printer implements Instruction {

	protected Expression parameter;

	public Printer(Expression _value) {
		this.parameter = _value;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "print " + this.parameter + ";\n";
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.parameter.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.parameter.completeResolve(_scope);
	}

	@Override
	public boolean checkType() {
		AtomicType _type = (AtomicType) this.parameter.getType();
		if (_type == AtomicType.IntegerType || _type == AtomicType.BooleanType
				|| _type == AtomicType.CharacterType || _type == AtomicType.StringType) {
			return true;
		}
		Logger.error("Cannot print value of type " + _type);
		return false;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		return 0;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = this.parameter.getCode(_factory);
		AtomicType _type = (AtomicType) this.parameter.getType();
		if (_type == AtomicType.IntegerType) {
			_result.add(Library.IOut);
		} else if (_type == AtomicType.BooleanType) {
			_result.add(Library.BOut);
		} else if (_type == AtomicType.CharacterType) {
			_result.add(Library.COut);
		} else if (_type == AtomicType.StringType) {
			_result.add(Library.SOut);
		}
		return _result;
	}

}
