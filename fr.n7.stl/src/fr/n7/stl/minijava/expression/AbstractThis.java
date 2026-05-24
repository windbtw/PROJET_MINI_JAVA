package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.util.Logger;

public abstract class AbstractThis<ObjectKind extends Expression> implements Expression {

	/** Synthetic "this" parameter resolved from the enclosing method scope. */
	protected ParameterDeclaration declaration;

	public AbstractThis() {
	}

	public ParameterDeclaration getDeclaration() {
		return this.declaration;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		Declaration d = _scope.get("this");
		if (d instanceof ParameterDeclaration) {
			this.declaration = (ParameterDeclaration) d;
			return true;
		}
		Logger.error("'this' used outside of a method body.");
		return false;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.declaration != null;
	}

	@Override
	public Type getType() {
		return this.declaration.getType();
	}

	@Override
	public String toString() {
		return "this";
	}
}
