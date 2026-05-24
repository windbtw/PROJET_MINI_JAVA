package fr.n7.stl.minijava.ast.type.declaration;

import java.util.List;

import fr.n7.stl.minic.ast.Block;
import fr.n7.stl.minic.ast.instruction.Instruction;
import fr.n7.stl.minic.ast.instruction.declaration.FunctionDeclaration;
import fr.n7.stl.minic.ast.scope.Declaration;
import fr.n7.stl.minic.ast.scope.HierarchicalScope;
import fr.n7.stl.tam.ast.Fragment;
import fr.n7.stl.tam.ast.Register;
import fr.n7.stl.tam.ast.TAMFactory;

public class MainDeclaration implements Instruction {

	protected String name;

	protected List<Declaration> declarations;

	protected Block main;

	public MainDeclaration(String _name, List<Declaration> _declarations, Block _main) {
		this.name = _name;
		this.declarations = _declarations;
		this.main = _main;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (Declaration d : this.declarations) {
			ok &= ((Instruction) d).collectAndPartialResolve(_scope);
		}
		ok &= this.main.collectAndPartialResolve(_scope);
		return ok;
	}

	@Override
	public boolean collectAndPartialResolve(HierarchicalScope<Declaration> _scope, FunctionDeclaration _container) {
		return this.collectAndPartialResolve(_scope);
	}

	@Override
	public boolean completeResolve(HierarchicalScope<Declaration> _scope) {
		boolean ok = true;
		for (Declaration d : this.declarations) {
			ok &= ((Instruction) d).completeResolve(_scope);
		}
		ok &= this.main.completeResolve(_scope);
		return ok;
	}

	@Override
	public boolean checkType() {
		boolean ok = true;
		for (Declaration d : this.declarations) {
			ok &= ((Instruction) d).checkType();
		}
		ok &= this.main.checkType();
		return ok;
	}

	@Override
	public int allocateMemory(Register _register, int _offset) {
		int size = 0;
		for (Declaration d : this.declarations) {
			size += ((Instruction) d).allocateMemory(Register.SB, _offset + size);
		}
		// Shift main's local frame past the reserved SB area (vtable slots + globals).
		this.main.allocateMemory(Register.LB, _offset + size);
		return size;
	}

	@Override
	public Fragment getCode(TAMFactory _factory) {
		Fragment result = _factory.createFragment();
		result.append(this.main.getCode(_factory));
		result.add(_factory.createHalt());
		for (Declaration d : this.declarations) {
			result.append(((Instruction) d).getCode(_factory));
		}
		return result;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public String toString() {
		String image = "";
		image += "public class " + this.name + " ";
		image += "{\n";
		image += "\n";
		for (Declaration uneDeclaration : this.declarations) {
			image += uneDeclaration;
			image += "\n";
		}
		image += "\tpublic static void main( String[] args) ";
		image += this.main;
		image += "\n";
		image += "}\n";
		return image;
	}

}
