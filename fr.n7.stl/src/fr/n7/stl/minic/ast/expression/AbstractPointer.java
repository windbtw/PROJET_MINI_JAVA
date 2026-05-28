package fr.n7.stl.minic.ast.expression;

import fr.n7.stl.minic.ast.SemanticsUndefinedException;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.PointerType;
import fr.n7.stl.minic.ast.type.Type;

public abstract class AbstractPointer<PointerKind extends Expression> implements Expression {

	protected PointerKind pointer;

	public AbstractPointer(PointerKind _pointer) {
		this.pointer = _pointer;
	}

	@Override
	public String toString() {
		return "(*" + this.pointer + ")";
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.pointer.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		return this.pointer.completeResolve(_scope);
	}

	public Type getType() {
		return ((PointerType) this.pointer.getType()).getPointedType();
	}

}
