package fr.n7.stl.minic.ast.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.expression.accessible.AccessibleExpression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.ArrayType;
import fr.n7.stl.minic.ast.type.Type;

public abstract class AbstractArray<ArrayKind extends Expression> implements Expression {

	protected ArrayKind array;
	protected AccessibleExpression index;

	public AbstractArray(ArrayKind _array, AccessibleExpression _index) {
		this.array = _array;
		this.index = _index;
	}

	@Override
	public String toString() {
		return (this.array + "[ " + this.index + " ]");
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.array.collectAndPartialResolve(_scope)
			& this.index.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.array.completeResolve(_scope)
			& this.index.completeResolve(_scope);
	}

	public Type getType() {
		return ((ArrayType) this.array.getType()).getType();
	}

}
