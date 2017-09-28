package example

import java.sql.ResultSet
import java.sql.DriverManager
import java.sql.Connection
import java.sql.PreparedStatement
import javax.sql.DataSource
import com.opencsv.CSVWriter
import com.sun.prism.shader.Mask_TextureSuper_AlphaTest_Loader
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import org.apache.commons.text.RandomStringGenerator

class JdbcOut2Csv(val con: Connection) {
    fun run() {
        con.setAutoCommit(false)

        con.prepareStatement("""
        create table items(
            id INT NOT NULL GENERATED ALWAYS AS IDENTITY
            , name varchar(256) NOT NULL
            , PRIMARY KEY(id))
        """).use{it.execute()}

        val builder = RandomStringGenerator.Builder()
                .withinRange('a'.toInt(), 'z'.toInt())
                .build();
        for (i in 1..10) {
            con.prepareStatement("insert into items (name) values (?)").use {
                it.setString(1, builder.generate(10));
                it.execute()
            }
        }


        con.prepareStatement("select * from items").use {
            it.executeQuery().use {
                outputCsv(it)
            }
        }
    }

    fun outputCsv(r: ResultSet) {
        CSVWriter(BufferedWriter(OutputStreamWriter(System.out))).use { writer ->
            writer.writeAll(r, true)
        }
    }
}

fun buildConnection(): Connection {
    val connectionUrl = "jdbc:derby:memory:myDB;create=true"
    val con = DriverManager.getConnection(connectionUrl)
    return con
}

fun main(args: Array<String>) {
    val t = JdbcOut2Csv(buildConnection())
    t.run()
}
