package fr.n7.stl.minijava.expression;

import fr.n7.stl.minic.ast.expression.Expression;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.minic.ast.type.Type;
import fr.n7.stl.minijava.ast.type.ClassType;
import fr.n7.stl.minijava.ast.type.declaration.AttributeDeclaration;
import fr.n7.stl.minijava.ast.type.declaration.ClassDeclaration;
import fr.n7.stl.util.Logger;

public abstract class AbstractAttribute<ObjectKind extends Expression> implements Expression {

	protected ObjectKind object;
	protected String name;
	protected AttributeDeclaration attribute;

	/** Set when the receiver was syntactically a bare identifier; lets completeResolve try
	 *  class-name lookup first to detect static attribute access (e.g. {@code A.x}). */
	protected String receiverIdentifier;

	/** True when this access targets a static attribute resolved via a class name. */
	protected boolean staticAccess;

	public AbstractAttribute(ObjectKind _object, String _name) {
		this.object = _object;
		this.name = _name;
	}

	public void setReceiverIdentifier(String _name) {
		this.receiverIdentifier = _name;
	}

	public AttributeDeclaration getAttribute() {
		return this.attribute;
	}

	public boolean isStaticAccess() {
		return this.staticAccess;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		return this.object.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		// Detect static access via a bare class identifier: A.x
		if (this.receiverIdentifier != null && _scope.knows(this.receiverIdentifier)) {
			Declaration rd = _scope.get(this.receiverIdentifier);
			if (rd instanceof ClassDeclaration) {
				ClassDeclaration cd = (ClassDeclaration) rd;
				Declaration d = cd.lookupMember(this.name);
				if (!(d instanceof AttributeDeclaration)) {
					Logger.error("Attribute " + this.name + " not found in class " + cd.getName());
					return false;
				}
				AttributeDeclaration ad = (AttributeDeclaration) d;
				if (!ad.isStatic()) {
					Logger.error("Attribute " + this.name + " of class " + cd.getName() + " is not static.");
					return false;
				}
				this.attribute = ad;
				this.staticAccess = true;
				return true;
			}
		}
		if (!this.object.completeResolve(_scope)) {
			return false;
		}
		Type t = this.object.getType();
		if (!(t instanceof ClassType)) {
			Logger.error("Attribute access on non-class type: " + t);
			return false;
		}
		ClassDeclaration cd = ((ClassType) t).getDeclaration();
		if (cd == null) {
			Logger.error("Class type for attribute access is not resolved.");
			return false;
		}
		Declaration d = cd.lookupMember(this.name);
		if (!(d instanceof AttributeDeclaration)) {
			Logger.error("Attribute " + this.name + " not found in class " + cd.getName());
			return false;
		}
		this.attribute = (AttributeDeclaration) d;
		return true;
	}

	@Override
	public Type getType() {
		return this.attribute.getType();
	}

	@Override
	public String toString() {
		String image = "";
		image += this.object;
		image += ".";
		image += this.name;
		return image;
	}

}
