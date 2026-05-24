package fr.n7.stl.minic.ast.expression.accessible;

import fr.n7.stl.minic.ast.expression.assignable.AssignableExpression;
import fr.n7.stl.minic.ast.expression.assignable.VariableAssignment;
import fr.n7.stl.minic.ast.instruction.declaration.VariableDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.PointerType;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.TAMFactory;
import fr.n7.stl.util.Logger;

public class AddressAccess implements AccessibleExpression {

	protected AssignableExpression assignable;

	public AddressAccess(AssignableExpression _assignable) {
		this.assignable = _assignable;
	}

	@Override
	public String toString() {
		return "& " + this.assignable.toString();
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.assignable.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.assignable.completeResolve(_scope);
	}

	@Override
	public Type getType() {
		return new PointerType(this.assignable.getType());
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment _result = _factory.createFragment();
		if (this.assignable instanceof VariableAssignment) {
			VariableDeclaration _decl = ((VariableAssignment) this.assignable).getDeclaration();
			_result.add(_factory.createLoadA(_decl.getRegister(), _decl.getOffset()));
		} else {
			Logger.error("Address access only supported for simple variables.");
		}
		return _result;
	}

}
