package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.instruction.declaration.ParameterDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.util.Logger;

public abstract class AbstractSuper<ObjectKind extends Expression> implements Expression {

	protected ParameterDeclaration thisDeclaration;

	protected ClassDeclaration parentClass;

	public AbstractSuper() {
	}

	public ParameterDeclaration getThisDeclaration() {
		return this.thisDeclaration;
	}

	public ClassDeclaration getParentClass() {
		return this.parentClass;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		Declaration d = _scope.get("this");
		if (!(d instanceof ParameterDeclaration)) {
			Logger.error("'super' used outside of a method body.");
			return false;
		}
		this.thisDeclaration = (ParameterDeclaration) d;
		return true;
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		Type t = this.thisDeclaration.getType();
		if (!(t instanceof ClassType)) {
			Logger.error("'this' has non-class type, cannot resolve 'super'.");
			return false;
		}
		ClassDeclaration current = ((ClassType) t).getDeclaration();
		if (current == null || current.getParent() == null) {
			Logger.error("'super' used in class without a parent.");
			return false;
		}
		this.parentClass = current.getParent();
		return true;
	}

	@Override
	public Type getType() {
		return new ClassType(this.parentClass);
	}

	@Override
	public String toString() {
		return "super";
	}

}
