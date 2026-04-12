package com.example.sacu.repository

import android.util.Log
import com.example.sacu.model.ItemPedido
import com.example.sacu.model.Producto
import com.google.android.play.integrity.internal.a

public var carritoTotal = mutableListOf<ItemPedido>()

class Compra {
    public var carrito = mutableListOf<ItemPedido>()

    fun agregarProducto(producto: Producto) {
        Log.d("SACU_CARRITO", "Agregado al carrito: ${producto.nombre}")

        val n = agruparProductos(producto)

            val item = ItemPedido(
                producto_id = producto.id,
                nombre = producto.nombre,
                cantidad = n,
                precio_unitario = producto.precio
            )
            carrito.add(item)
            Log.d("SACU_CARRITO", "Items en Carrito: ${carrito.size}")
            Log.d("SACU_CARRITO", "Carrito: $carrito")
            carritoTotal.add(item)
            Log.d("SACU_CARRITO", "Carrito Total: ${carritoTotal.size}")

        Log.d("SACU_CARRITO", "ANTES: $carritoTotal")
        eliminarDups()
        Log.d("SACU_CARRITO", "DESPUES: $carritoTotal")

    }



    fun agruparProductos(producto: Producto): Int {
        var n = 0
        for (item in carritoTotal) {
            if (item.nombre == producto.nombre) {
                n = maxOf(n, item.cantidad)
            }
        }
        return n + 1
    }

    fun eliminarDups() {
        carritoTotal = carritoTotal
            .groupBy { it.nombre }
            .map { (_, lista) ->
                lista.maxByOrNull { it.cantidad }!!
            }
            .toMutableList()
    }

    fun totalAPagar(): Double {
        var total = 0.0
        for (item in carritoTotal) {
            total += item.precio_unitario * item.cantidad
        }
        return total
    }

}