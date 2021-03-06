package edu.utah.ece.async.ibiosim.synthesis.VerilogCompiler;

import org.junit.Assert;
import org.junit.Test;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.text.parser.ParseException;

/**
 * Test decomposition method 
 * @author Tramy Nguyen
 */
public class Decomposition_Test { 

	
	@Test
	public void Test_AND() throws ParseException, VerilogCompilerException {
		ASTNode logicFunction = ASTNode.parseFormula("and(a,b)");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!a) || (!b))", decompNode.toString());
	}
	
	@Test
	public void Test_OR() throws ParseException, VerilogCompilerException {
		ASTNode logicFunction = ASTNode.parseFormula("or(a,b)");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(a || b))", decompNode.toString());
	}
	
	@Test
	public void Test_NOT() throws ParseException, VerilogCompilerException {
		ASTNode logicFunction = ASTNode.parseFormula("not(a)");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!a", decompNode.toString());
	}
	
	@Test
	public void Test_NOR() throws ParseException, VerilogCompilerException {
		ASTNode logicFunction = ASTNode.parseFormula("not(or(a,b))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(!(a || b)))", decompNode.toString());
	}
	
	@Test
	public void Test1_2LvlGates() throws ParseException, VerilogCompilerException {
		ASTNode logicFunction = ASTNode.parseFormula("and(a,not(b))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!a) || (!(!b)))", decompNode.toString());
	}
	
	@Test
	public void Test2_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("not(and(a,b))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!((!a) || (!b)))", decompNode.toString());
	}
	
	@Test
	public void Test3_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(a,or(b,c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(a || (!(!(b || c)))))", decompNode.toString());
	}
	
	@Test
	public void Test4_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(a,and(b,c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!a) || (!(!((!b) || (!c)))))", decompNode.toString());
	}
	
	@Test
	public void Test5_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("not(not(b))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!b)", decompNode.toString());
	}
	
	@Test
	public void Test6_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(a,or(b,c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!a) || (!(!(!(b || c)))))", decompNode.toString());
	}
	
	@Test
	public void Test7_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(a,and(b,c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(a || (!((!b) || (!c)))))", decompNode.toString());
	}
	
	@Test
	public void Test8_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(a,not(b))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(a || (!b)))", decompNode.toString());
	}
	
	@Test
	public void Test9_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(not(a),or(b,c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!(!a)) || (!(!(!(b || c)))))", decompNode.toString());
	}
	
	@Test
	public void Test10_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(not(a),not(b))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!(!a)) || (!(!b)))", decompNode.toString());
	}
	
	@Test
	public void Test11_2LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(or(a,b),or(b,c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!(!(!(a || b)))) || (!(!(!(b || c)))))", decompNode.toString());
	}
	
	@Test
	public void Test1_3LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("not(or(a,and(b,c)))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(!(a || (!((!b) || (!c))))))", decompNode.toString());
	}
	
	@Test
	public void Test2_3LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("not(and(a,or(b,c)))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!((!a) || (!(!(!(b || c))))))", decompNode.toString());
	}
	
	@Test
	public void Test3_3LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(or(a,not(b)),c)");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!((!(!(!(a || (!b))))) || (!c))", decompNode.toString());
	}
	
	@Test
	public void Test_4LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(or(b,c),and(not(or(a,b)),not(c)))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!((!(!(b || c))) || (!((!(!(!(!(a || b))))) || (!(!c))))))", decompNode.toString());
	}
	
	@Test
	public void Test_5LvlGates() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(a,or(a,not(and(not(b),c))))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(a || (!(!(a || (!(!((!(!b)) || (!c)))))))))", decompNode.toString());
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test1_3inputs() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("and(a,b,c)");
		VerilogDecomposer.decompose(logicFunction);
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test2_3inputs() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(a,b,c)");
		VerilogDecomposer.decompose(logicFunction);
	}
	
	
	@Test
	public void Test3_3inputs() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("not(or(or(a,b),c))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!(!((!(!(a || b))) || c)))", decompNode.toString());
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test1_InvalidLogicFunction() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("xor(a,b)");
		VerilogDecomposer.decompose(logicFunction);
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test2_InvalidLogicFunction() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("eq(a,b)");
		VerilogDecomposer.decompose(logicFunction);
	}
	
	@Test(expected = VerilogCompilerException.class)
	public void Test3_InvalidLogicFunction() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("neq(a,b)");
		VerilogDecomposer.decompose(logicFunction);
	}
	
	@Test
	public void Test_longExpression() throws ParseException, VerilogCompilerException{
		ASTNode logicFunction = ASTNode.parseFormula("or(and(bit1,not(ez_instance__state)),or(and(bit0,and(not(parity0),ez_instance__state)),and(or(bit1,bit0),parity1)))");
		ASTNode decompNode = VerilogDecomposer.decompose(logicFunction);
		Assert.assertEquals("!(!((!((!bit1) || (!(!ez_instance__state)))) || (!(!((!((!bit0) || (!(!((!(!parity0)) || (!ez_instance__state)))))) || (!((!(!(!(bit1 || bit0)))) || (!parity1))))))))", decompNode.toString());
	}

}