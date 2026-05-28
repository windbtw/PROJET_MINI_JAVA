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
public class Iteration implements Instruction {

	protected Expression condition;
	protected Block body;

	public Iteration(Expression _condition, Block _body) {
		this.condition = _condition;
		this.body = _body;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "while (" + this.condition + " )" + this.body;
	}
	
	/* (non-Javadoc)
	 * @see fr.n7.stl.block.ast.instruction.Instruction#collect(fr.n7.stl.block.ast.scope.Scope)
	 */
	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.condition.collectAndPartialResolve(_scope)
			& this.body.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.condition.collectAndPartialResolve(_scope)
			& this.body.collectAndPartialResolve(_scope, _container);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.condition.completeResolve(_scope)
			& this.body.completeResolve(_scope);
	}

	@Override
	public boolean checkType() {
		boolean _ok = true;
		if (!this.condition.getType().equalsTo(AtomicType.BooleanType)) {
			Logger.error("Condition of while must be boolean, got " + this.condition.getType());
			_ok = false;
		}
		return _ok & this.body.checkType();
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		this.body.allocateMemory(_register, _offset);
		return 0;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		String _startLabel = "while" + _factory.createLabelNumber();
		String _endLabel = "endwhile" + _factory.createLabelNumber();
		Fragment _result = this.condition.getCode(_factory);
		_result.addPrefix(_startLabel);
		_result.add(_factory.createJumpIf(_endLabel, 0));
		_result.append(this.body.getCode(_factory));
		_result.add(_factory.createJump(_startLabel));
		_result.addSuffix(_endLabel);
		return _result;
	}

}
