package br.unb.cic.oberon.tc

import java.nio.file.{Files, Paths}

import br.unb.cic.oberon.ast.{AddExpression, AssignmentStmt, BoolValue, BooleanType, ForStmt, IfElseStmt, IntValue, IntegerType, ReadIntStmt, SequenceStmt, Undef, VarExpression, WhileStmt, WriteStmt, CaseStmt, RangeCase,SimpleCase}
import br.unb.cic.oberon.parser.OberonParser.ReadIntStmtContext
import br.unb.cic.oberon.parser.ScalaParser
import org.scalatest.funsuite.AnyFunSuite

class TypeCheckerTestSuite  extends AnyFunSuite {

  test("Test read statement type checker") {
    val visitor = new TypeChecker()
    val read01 = ReadIntStmt("x")
    val read02 = ReadIntStmt("y")

    visitor.env.setGlobalVariable("x", IntegerType)

    assert(read01.accept(visitor) == List())
    assert(read02.accept(visitor).size == 1)
  }

  test("Test write statement type checker") {
    val visitor = new TypeChecker()
    val write01 = WriteStmt(IntValue(5))
    val write02 = WriteStmt(AddExpression(IntValue(5), BoolValue(false)))

    assert(write01.accept(visitor) == List())
    assert(write02.accept(visitor).size == 1)
  }

  test("Test assignment statement type checker") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(10)) // invalid stmt
    val stmt03 = AssignmentStmt("x", AddExpression(IntValue(5), BoolValue(false))) // invalid stmt

    visitor.env.setGlobalVariable("x", IntegerType)

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor).size == 1)
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test a sequence of statements type checker") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(10)) // invalid stmt
    val stmt03 = AssignmentStmt("x", AddExpression(IntValue(5), BoolValue(false))) // invalid stmt
    val stmt04 = WriteStmt(VarExpression("x"))
    val stmt05 = WriteStmt(VarExpression("y"))

    visitor.env.setGlobalVariable("x", IntegerType)

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor).size == 1)
    assert(stmt03.accept(visitor).size == 1)

    val seq1 = SequenceStmt(List(stmt01, stmt04))
    val seq2 = SequenceStmt(List(stmt01, stmt05))

    assert(seq1.accept(visitor).size == 0)
    assert(seq2.accept(visitor).size == 1)
  }

  test("Test if-else statement type checker (with invalid condition)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))

    visitor.env.setGlobalVariable("x", IntegerType)

    val stmt02 = IfElseStmt(IntValue(10), stmt01, None)
    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor).size == 1)
  }

  test("Test if-else statement type checker (with invalid then-stmt)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))

    val stmt02 = IfElseStmt(BoolValue(true), stmt01, None)
    assert(stmt01.accept(visitor).size == 1)
    assert(stmt02.accept(visitor).size == 1)
  }

  test("Test if-else statement type checker (with invalid then-stmt and else-stmt)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(10))
    val stmt03 = IfElseStmt(BoolValue(true), stmt01, Some(stmt02))

    assert(stmt01.accept(visitor).size == 1)
    assert(stmt02.accept(visitor).size == 1)
    assert(stmt03.accept(visitor).size == 2)
  }

  test("Test if-else statement type checker") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(10))

    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val stmt03 = IfElseStmt(BoolValue(true), stmt01, Some(stmt02))

    assert(stmt01.accept(visitor).size == 0)
    assert(stmt02.accept(visitor).size == 0)
    assert(stmt03.accept(visitor).size == 0)
  }

  test("Test while statement type checker (with invalid condition)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))

    visitor.env.setGlobalVariable("x", IntegerType)

    val stmt02 = WhileStmt(IntValue(10), stmt01)
    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor).size == 1)
  }

  test("Test while statement type checker (with invalid stmt)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))

    val stmt02 = WhileStmt(BoolValue(true), stmt01)
    assert(stmt01.accept(visitor).size == 1)
    assert(stmt02.accept(visitor).size == 1)
  }

  test("Test while statement type checker") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))

    visitor.env.setGlobalVariable("x", IntegerType)

    val stmt02 = WhileStmt(BoolValue(true), stmt01)
    assert(stmt01.accept(visitor).size == 0)
    assert(stmt02.accept(visitor).size == 0)
  }

  test("Test for statement type checker (with invalid init)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(10))

    visitor.env.setGlobalVariable("y", IntegerType)

    val stmt03 = ForStmt(stmt01, BoolValue(true), stmt02)
    assert(stmt01.accept(visitor).size == 1)
    assert(stmt02.accept(visitor).size == 0)
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test for statement type checker (with invalid condition)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(10))

    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val stmt03 = ForStmt(stmt01,IntValue(10), stmt02)
    assert(stmt01.accept(visitor).size == 0)
    assert(stmt02.accept(visitor).size == 0)
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test for statement type checker (with invalid stmt)") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(100))

    visitor.env.setGlobalVariable("x", IntegerType)

    val stmt03 = ForStmt(stmt01, BoolValue(true), stmt02)
    assert(stmt01.accept(visitor).size == 0)
    assert(stmt02.accept(visitor).size == 1)
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test for statement type checker") {
    val visitor = new TypeChecker()
    val stmt01 = AssignmentStmt("x", IntValue(0))
    val stmt02 = AssignmentStmt("y", IntValue(10))

    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val stmt03 = ForStmt(stmt01, BoolValue(true), stmt02)
    assert(stmt01.accept(visitor).size == 0)
    assert(stmt02.accept(visitor).size == 0)
    assert(stmt03.accept(visitor).size == 0)
  }

  test("Test switch-case statement type checker RangeCase (invalid case01 min expression) ") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(15))
    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = RangeCase(BoolValue(false), IntValue(20), stmt01)
    val case02 = RangeCase(IntValue(21), IntValue(30), stmt02)

    val cases = List(case01, case02)

    val stmt03 = CaseStmt(IntValue(11), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test switch-case statement type checker RangeCase (invalid case02 min expression) ") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(15))
    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = RangeCase(IntValue(21), IntValue(20), stmt01)
    val case02 = RangeCase(BoolValue(false), IntValue(30), stmt02)

    val cases = List(case01, case02)

    val stmt03 = CaseStmt(IntValue(11), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test switch-case statement type checker RangeCase (invalid case01 and case02 min expression) ") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(15))
    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = RangeCase(BoolValue(false), IntValue(20), stmt01)
    val case02 = RangeCase(BoolValue(false), IntValue(30), stmt02)

    val cases = List(case01, case02)

    val stmt03 = CaseStmt(IntValue(11), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt03.accept(visitor).size == 2)
  }

  test("Test switch-case statement type checker RangeCase (invalid case01 and case02 max expression) ") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(15))
    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = RangeCase(IntValue(20),BoolValue(false), stmt01)
    val case02 = RangeCase(IntValue(30),BoolValue(false), stmt02)

    val cases = List(case01, case02)

    val stmt03 = CaseStmt(IntValue(11), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt03.accept(visitor).size == 2)
  }

  test("Test switch-case statement type checker RangeCase (invalid CaseStmt exp) ") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(15))
    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = RangeCase(IntValue(20),IntValue(30), stmt01)
    val case02 = RangeCase(IntValue(30),IntValue(40), stmt02)

    val cases = List(case01, case02)

    val stmt03 = CaseStmt(Undef(), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt03.accept(visitor).size == 1)
  }

  test("Test switch-case statement type checker SimpleCase (invalid CaseStmt condition)") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    visitor.env.setGlobalVariable("x", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = SimpleCase(BoolValue(true), stmt01)
    val case02 = SimpleCase(BoolValue(false), stmt01)
    val cases = List(case01, case02)

    val stmt02 = CaseStmt(BoolValue(true), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt02.accept(visitor).size ==  1)
  }

  test("Test switch-case statement type checker SimpleCase (invalid case02 condition)") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    visitor.env.setGlobalVariable("x", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = SimpleCase(IntValue(10), stmt01)
    val case02 = SimpleCase(BoolValue(false), stmt01)
    val cases = List(case01, case02)

    val stmt02 = CaseStmt(IntValue(10), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt02.accept(visitor).size ==  1)
  }

  test("Test switch-case statement type checker SimpleCase (invalid case01 and case02 condition)") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    visitor.env.setGlobalVariable("x", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = SimpleCase(Undef(), stmt01)
    val case02 = SimpleCase(Undef(), stmt01)
    val cases = List(case01, case02)

    val stmt02 = CaseStmt(IntValue(10), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt02.accept(visitor).size ==  2)
  }

  test("Test switch-case statement type checker RangeCase") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    val stmt02 = AssignmentStmt("y", IntValue(15))
    visitor.env.setGlobalVariable("x", IntegerType)
    visitor.env.setGlobalVariable("y", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = RangeCase(IntValue(10), IntValue(20), stmt01)
    val case02 = RangeCase(IntValue(21), IntValue(30), stmt02)

    val cases = List(case01, case02)

    val stmt03 = CaseStmt(IntValue(11), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt03.accept(visitor) == List())
  }

  test("Test switch-case statement type checker SimpleCase") {
    val visitor = new TypeChecker()

    val stmt01 = AssignmentStmt("x", IntValue(10))
    visitor.env.setGlobalVariable("x", IntegerType)

    val caseElse = AssignmentStmt("x", IntValue(20))


    val case01 = SimpleCase(IntValue(10), stmt01)
    val case02 = SimpleCase(IntValue(20), stmt01)
    val cases = List(case01, case02)

    val stmt02 = CaseStmt(IntValue(10), cases, Some(caseElse))

    assert(stmt01.accept(visitor) == List())
    assert(caseElse.accept(visitor) == List())
    assert(stmt02.accept(visitor) == List())
  }

  /*
   * the following test cases read an oberon module with the
   * factorial procedure.
   */
  test("Test invalid procedure declaration") {
    val path = Paths.get(getClass.getClassLoader.getResource("procedures/procedure04.oberon").getFile)

    assert(path != null)

    val content = String.join("\n", Files.readAllLines(path))
    val module = ScalaParser.parse(content)

    assert(module.name == "SimpleModule")

    assert(module.procedures.size == 2)
    assert(module.stmt.isDefined)


  }
}