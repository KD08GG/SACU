package com.example.sacu.repository

import android.util.Log
import com.example.sacu.model.ItemPedido
import com.example.sacu.model.Producto

var carritoTotal = mutableListOf<ItemPedido>()

class Compra {
    var carrito = mutableListOf<ItemPedido>()

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
        carritoTotal.add(item)

        eliminarDups()
    }

    private fun agruparProductos(producto: Producto): Int {
        var n = 0
        for (item in carritoTotal) {
            if (item.nombre == producto.nombre) {
                n = maxOf(n, item.cantidad)
            }
        }
        return n + 1
    }

    private fun eliminarDups() {
        carritoTotal = carritoTotal
            .groupBy { it.nombre }
            .map { (_, lista) ->
                lista.maxByOrNull { it.cantidad }!!
            }
            .toMutableList()
    }

    fun quitarProducto(producto: Producto) {
        val cantidadActual = carritoTotal
            .filter { it.nombre == producto.nombre }
            .maxOfOrNull { it.cantidad } ?: return

        if (cantidadActual <= 1) {
            carritoTotal.removeAll { it.nombre == producto.nombre }
            carrito.removeAll { it.nombre == producto.nombre }
            Log.d("SACU_CARRITO", "Eliminado del carrito: ${producto.nombre}")
        } else {
            val nuevaCantidad = cantidadActual - 1
            val item = ItemPedido(
                producto_id = producto.id,
                nombre = producto.nombre,
                cantidad = nuevaCantidad,
                precio_unitario = producto.precio
            )
            carritoTotal.add(item)
            carrito.add(item)
            eliminarDups()
            Log.d("SACU_CARRITO", "Restado del carrito: ${producto.nombre} → $nuevaCantidad")
        }
    }

    fun totalAPagar(): Double {
        var total = 0.0
        for (item in carritoTotal) {
            total += item.precio_unitario * item.cantidad
        }
        return total
    }

    fun limpiarCarrito() {
        carrito.clear()
        carritoTotal.clear()
        Log.d("SACU_CARRITO", "Carrito vaciado exitosamente")
    }
}