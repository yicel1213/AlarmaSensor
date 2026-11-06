package com.example.alarmasensor

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.UUID

class SensorActivity : AppCompatActivity() {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var socket: BluetoothSocket? = null


    private val UUID_MODULE: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fun)

        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Solicitar permisos de Bluetooth en Android 12 o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN),
                1
            )
        }

        // Botón Conectar Bluetooth (Busca automáticamente el HC-05 vinculado)
        findViewById<Button>(R.id.btnConectar).setOnClickListener {
            conectarBluetooth(tvEstado)
        }

        // Botón Desconectar Bluetooth
        findViewById<Button>(R.id.btnDesconectar).setOnClickListener {
            try {
                socket?.close()
                tvEstado.text = "Estado: Desconectado"
                Toast.makeText(this, "Desconectado correctamente", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                tvEstado.text = "Estado: Error al desconectar"
            }
        }

        // Botón Prender Alarma
        findViewById<Button>(R.id.btnPrender).setOnClickListener {
            enviarComando("1", "Estado: Alarma Encendida")
        }

        // Botón Apagar Alarma
        findViewById<Button>(R.id.btnApagar).setOnClickListener {
            enviarComando("0", "Estado: Alarma Apagada")
        }
    }

    private fun conectarBluetooth(tvEstado: TextView) {
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Este dispositivo no soporta Bluetooth", Toast.LENGTH_SHORT).show()
            return
        }

        if (!bluetoothAdapter!!.isEnabled) {
            Toast.makeText(this, "Por favor enciende el Bluetooth del celular", Toast.LENGTH_SHORT).show()
            return
        }

        tvEstado.text = "Estado: Buscando dispositivo..."

        Thread {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return@Thread
                }

                // Obtiene la lista de dispositivos vinculados en el teléfono
                val dispositivosVinculados: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices

                if (dispositivosVinculados.isNullOrEmpty()) {
                    runOnUiThread {
                        tvEstado.text = "Estado: Sin emparejar"
                        Toast.makeText(this, "No hay dispositivos Bluetooth vinculados en el celular.", Toast.LENGTH_LONG).show()
                    }
                    return@Thread
                }

                // Busca automáticamente el HC-05 / HC-06
                val dispositivoTarget = dispositivosVinculados.find {
                    it.name.contains("HC-05", ignoreCase = true) || it.name.contains("HC-06", ignoreCase = true)
                } ?: dispositivosVinculados.first()

                runOnUiThread {
                    tvEstado.text = "Estado: Conectando a ${dispositivoTarget.name}..."
                }

                socket = dispositivoTarget.createRfcommSocketToServiceRecord(UUID_MODULE)
                bluetoothAdapter?.cancelDiscovery()
                socket?.connect()

                runOnUiThread {
                    tvEstado.text = "Estado: Conectado"
                    Toast.makeText(this, "¡Conexión exitosa a ${dispositivoTarget.name}!", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    tvEstado.text = "Estado: Error de Conexión"
                    Toast.makeText(this, "No se pudo establecer la conexión.", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun enviarComando(comando: String, mensajeEstado: String) {
        val tvEstado = findViewById<TextView>(R.id.tvEstado)
        try {
            if (socket != null && socket!!.isConnected) {
                socket!!.outputStream.write(comando.toByteArray())
                tvEstado.text = mensajeEstado
            } else {
                Toast.makeText(this, "No estás conectado a ningún Bluetooth", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Error al enviar el comando", Toast.LENGTH_SHORT).show()
        }
    }
}