package com.example.alarmasensor

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class BaseDatos(context: Context) : SQLiteOpenHelper(context, "usuarios.db", null, 1) {

    // PASO 2: Se ejecuta SOLO la primera vez. Aquí creamos la tabla.
    //         PRIMARY KEY impide que se repita el nombre de usuario.
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE usuarios (usuario TEXT PRIMARY KEY, password TEXT)")
    }

    // Obligatorio, pero no lo usamos en este ejemplo.
    override fun onUpgrade(db: SQLiteDatabase, vieja: Int, nueva: Int) {}

    // PASO 3: Guardar un usuario (INSERT).
    //         insert() devuelve -1 si falla (por ejemplo, si el usuario ya existe).
    fun registrar(usuario: String, password: String): Boolean {
        val valores = ContentValues()
        valores.put("usuario", usuario)
        valores.put("password", hash(password))   // nunca guardamos la contraseña real
        return writableDatabase.insert("usuarios", null, valores) != -1L
    }

    // PASO 4: Comprobar el login (SELECT).
    //         El "?" se reemplaza por los valores de forma segura (evita inyección SQL).
    fun login(usuario: String, password: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT * FROM usuarios WHERE usuario = ? AND password = ?",
            arrayOf(usuario, hash(password))
        )
        val existe = cursor.count > 0   // ¿encontró alguna fila?
        cursor.close()
        return existe
    }

    // PASO 5: Convierte la contraseña en un "hash" SHA-256 (un código del que
    //         no se puede volver atrás). Así, si roban la base, no ven las contraseñas.
    private fun hash(texto: String): String =
        MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
            .joinToString("") { "%02x".format(it) }
}
