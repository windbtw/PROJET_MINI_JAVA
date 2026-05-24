/**
 * 
 */
package fr.n7.stl.minic.ast.instruction;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.AtomicType;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

/**
 * Implementation of the Abstract Syntax Tree node for a conditional instruction.
 * @author Marc Pantel
 *
 */
public class Conditional implements Instruction {

	protected Expression condition;
	protected Block thenBranch;
	protected Block elseBranch;

	public Conditional(Expression _condition, Block _then, Block _else) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = _else;
	}

	public Conditional(Expression _condition, Block _then) {
		this.condition = _condition;
		this.thenBranch = _then;
		this.elseBranch = null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "if (" + this.condition + " )" + this.thenBranch + ((this.elseBranch != null)?(" else " + this.elseBranch):"");
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = this.condition.collectAndPartialResolve(_scope);
		_ok &= this.thenBranch.collectAndPartialResolve(_scope);
		if (this.elseBranch != null) {
			_ok &= this.elseBranch.collectAndPartialResolve(_scope);
		}
		return _ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		boolean _ok = this.condition.collectAndPartialResolve(_scope);
		_ok &= this.thenBranch.collectAndPartialResolve(_scope, _container);
		if (this.elseBranch != null) {
			_ok &= this.elseBranch.collectAndPartialResolve(_scope, _container);
		}
		return _ok;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean _ok = this.condition.completeResolve(_scope);
		_ok &= this.thenBranch.completeResolve(_scope);
		if (this.elseBranch != null) {
			_ok &= this.elseBranch.completeResolve(_scope);
		}
		return _ok;
	}

	@Override
	public boolean checkType() {
		boolean _ok = true;
		if (!this.condition.getType().equalsTo(AtomicType.BooleanType)) {
			Logger.error("Condition of if must be boolean, got " + this.condition.getType());
			_ok = false;
		}
		_ok &= this.thenBranch.checkType();
		if (this.elseBranch != null) {
			_ok &= this.elseBranch.checkType();
		}
		return _ok;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		this.thenBranch.allocateMemory(_register, _offset);
		if (this.elseBranch != null) {
			this.elseBranch.allocateMemory(_register, _offset);
		}
		return 0;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		String _endLabel = "endif" + _factory.createLabelNumber();
		Fragment _result = this.condition.getCode(_factory);
		if (this.elseBranch != null) {
			String _elseLabel = "else" + _factory.createLabelNumber();
			_result.add(_factory.createJumpIf(_elseLabel, 0));
			Fragment _thenCode = this.thenBranch.getCode(_factory);
			_thenCode.add(_factory.createJump(_endLabel));
			_thenCode.addSuffix(_elseLabel);
			_result.append(_thenCode);
			Fragment _elseCode = this.elseBranch.getCode(_factory);
			_elseCode.add(_factory.createJump(_endLabel));
			_elseCode.addSuffix(_endLabel);
			_result.append(_elseCode);
		} else {
			_result.add(_factory.createJumpIf(_endLabel, 0));
			Fragment _thenCode = this.thenBranch.getCode(_factory);
			_thenCode.add(_factory.createJump(_endLabel));
			_thenCode.addSuffix(_endLabel);
			_result.append(_thenCode);
		}
		return _result;
	}

}
