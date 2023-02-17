package tech.harrynull.orm

import org.ktorm.expression.FunctionExpression
import org.ktorm.schema.ColumnDeclaring
import org.ktorm.schema.VarcharSqlType

fun ColumnDeclaring<String>.toLowerCase(): FunctionExpression<String> {
    // lower(str)
    return FunctionExpression(
        functionName = "lower",
        arguments = listOf(this.asExpression()),
        sqlType = VarcharSqlType
    )
}
