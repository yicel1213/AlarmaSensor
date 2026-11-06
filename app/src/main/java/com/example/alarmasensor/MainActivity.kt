package com.example.alarmasensor

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val base = BaseDatos(this)

        val infoUsuario = findViewById<EditText>(R.id.txtUsuario)
        val infoContraseña = findViewById<EditText>(R.id.txtContraseña)

        findViewById<Button>(R.id.btnRegistrar).setOnClickListener {
            val usuario = infoUsuario.text.toString()
            val contraseña = infoContraseña.text.toString()

            if (usuario.isEmpty() || contraseña.isEmpty()) {
                mensaje("Ambos campos son obligatorios")
            } else if (base.registrar(usuario, contraseña)) {
                mensaje("Registro exitoso")
            } else {
                mensaje("Ese usuario ya existe")
            }
        }

        findViewById<Button>(R.id.btnIngresar).setOnClickListener {
            val usuario = infoUsuario.text.toString()
            val contraseña = infoContraseña.text.toString()

            if (base.login(usuario, contraseña)) {
                mensaje("¡Bienvenido a Sistema de Alarmas, $usuario!")
                startActivity(Intent(this, SensorActivity::class.java))
            } else {
                mensaje("Usuario o contraseña incorrectos")
            }
        }
    }

    private fun mensaje(texto: String) {
        Toast.makeText(this, texto, Toast.LENGTH_SHORT).show()
    }
}