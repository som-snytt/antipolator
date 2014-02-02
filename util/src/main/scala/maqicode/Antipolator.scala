
package maqicode

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.Context
import scala.collection.mutable.ListBuffer

/** Antipolator is the anti-interpolator.
 *
 *  import maqicode.antipolator._
 *  a"""foo + bar"""
 *  s"$foo$bar"
 *
 *  a"""foo +"and"+ bar"""
 *  s"$foo and $bar"
 *
 *  a"""${foo} + bar"""
 */
package object antipolator {
  implicit class Antipolator(sc: StringContext) {
    def a(args: Any*): String = macro AntipolatorMacro.impl
  }
}

class AntipolatorMacro(val c: Context) {

  import c.universe._
  import definitions._

  private[this] def *?! = throw new IllegalStateException 

  def impl(args: c.Expr[Any]*): c.Expr[String] = c.prefix.tree match {
    case q"$_($_(..$parts))" => c.Expr(stringify(parts, args map (_.tree)))
    case other => c.error(c.prefix.tree.pos, s"Unexpected prefix ${showRaw(other)}"); *?!
  }

  def stringify(parts: Seq[Tree], args: Seq[Tree]) = {
    def defval(value: Tree, tpe: Type): (ValDef, Ident) = {
      val freshName = TermName(c.freshName("arg$"))
      def mkValDef  = {
        val v = ValDef(Modifiers(), freshName, TypeTree(tpe) setPos value.pos.focus, value)
        v setPos value.pos
        v
      }
      (mkValDef, Ident(freshName))
    }
    // isn't boxing just your favorite thing ever?
    def boxed(t: Tree): Tree = {
      def boxed(tpe: Type): Tree = tpe match {
        case AnyRef     => t
        case IntTpe     => q"scala.runtime.BoxesRunTime.boxToInteger($t)"
        case ShortTpe   => q"scala.runtime.BoxesRunTime.boxToShort($t)"
        case LongTpe    => q"scala.runtime.BoxesRunTime.boxToLong($t)"
        case CharTpe    => q"scala.runtime.BoxesRunTime.boxToCharacter($t)"
        case BooleanTpe => q"scala.runtime.BoxesRunTime.boxToBoolean($t)"
        case FloatTpe   => q"scala.runtime.BoxesRunTime.boxToFloat($t)"
        case DoubleTpe  => q"scala.runtime.BoxesRunTime.boxToDouble($t)"
        case ByteTpe    => q"scala.runtime.BoxesRunTime.boxToByte($t)"
        case UnitTpe    => q"scala.runtime.BoxedUnit.UNIT"
        case ConstantType(k) => boxed(k.tpe)
        case _          => t
      }
      boxed(t.tpe)
    }
    // transform applications of plus to strcat
    def additively(t: Tree) = {
      val tf = new Transformer {
        override def transform(x: Tree) = x match {
          case q"$p.+($q)" =>
            def boxy(pq: Tree): Tree = boxed(c typecheck transform(pq))
            q"""new java.util.Formatter().format("%s%s", ${boxy(p)}, ${boxy(q)}).toString"""
          case y => y
        }
      }
      tf transform t
    }
    def untuple(pa: (Tree, Tree)) = Seq(pa._1, pa._2)
    val partify = parts map {
      case Literal(Constant(x: String)) => q"(${additively(c parse x)}).toString"
    }
    val referee = args map (boxed)
    val recon   = (partify.zipAll(referee, EmptyTree, EmptyTree) flatMap untuple).init
    val (evals, ids) = recon.map(defval(_, AnyRefTpe)).unzip
    val fmt = Literal(Constant("%s" * ids.size))
    q"""{
      ..$evals
      new java.util.Formatter().format($fmt, ..$ids).toString
    }"""
  }
}
