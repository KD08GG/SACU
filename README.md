# SACU — Sistema de Automatización de Cafetería Universitaria

> Digitalización de pedidos y pagos para la cafetería de la UDLAP. Reduce tiempos de espera, elimina filas y automatiza el proceso de cobro mediante una app móvil Android conectada a Firebase en tiempo real.

---

## Demo

| Login | Home | Carrito | Panel Cocina |
|-------|------|---------|--------------|
| Autenticación con matrícula UDLAP | Menú en tiempo real + fila virtual | Selección de productos | Panel de gestión de pedidos |

---

## Descripción del Proyecto

SACU es un sistema de automatización para la cafetería universitaria de la UDLAP compuesto por dos módulos:

- **App móvil Android** — permite a estudiantes, profesores y personal administrativo consultar el menú, realizar pedidos y pagar desde su celular sin hacer fila.
- **Panel de cocina (Desktop)** — aplicación Python que muestra los pedidos entrantes en tiempo real, permite al personal marcar pedidos como listos y gestionar el menú.

Ambos módulos están conectados a **Firebase** como backend en la nube, lo que garantiza sincronización en tiempo real sin necesidad de un servidor propio.

---

## Arquitectura

```
App Android (Kotlin)
       ↕
Firebase Auth + Firestore + Functions
       ↕
Mercado Pago API (pasarela de pagos)
       ↕
Panel de Cocina (Python + CustomTkinter)
```

---

## Stack Tecnológico

### App Móvil
| Tecnología | Uso |
|-----------|-----|
| Kotlin | Lenguaje principal |
| Android Studio | Entorno de desarrollo |
| Firebase Auth | Autenticación con matrícula |
| Cloud Firestore | Base de datos en tiempo real |
| Firebase Functions | Lógica de negocio y webhooks |
| Mercado Pago API | Pasarela de pagos |

### Panel de Cocina
| Tecnología | Uso |
|-----------|-----|
| Python 3.12 | Lenguaje principal |
| CustomTkinter | Interfaz gráfica |
| firebase-admin SDK | Conexión a Firestore |

---

## Estructura del Proyecto

```
SACU/
├── app/                          # App móvil Android
│   └── src/main/java/com/example/sacu/
│       ├── model/                # Data classes (Producto, Pedido, Usuario...)
│       ├── repository/           # FirestoreRepository — operaciones con BD
│       ├── adapter/              # RecyclerView adapters
│       ├── MainActivity.kt       # Login
│       ├── Home.kt               # Menú principal + fila virtual
│       ├── Carrito.kt            # Carrito de compras
│       ├── Notificaciones.kt     # Alertas en tiempo real
│       └── Perfil.kt             # Perfil del usuario
│
├── panel-cocina/                 # Panel de gestión (Python)
│   ├── main.py                   # Aplicación principal
│   └── requirements.txt          # Dependencias
│
└── .gitignore
```

---

## Estructura de la Base de Datos (Firestore)

```
usuarios/
  {uid}/
    nombre, matricula, tipo, fecha_creacion

usuarios_autorizados/
  {matricula}/
    nombre, tipo, activo

productos/
  {productoId}/
    nombre, descripcion, precio, categoria, disponible

pedidos/
  {pedidoId}/
    usuario_id, estado, total, numero_fila,
    tiempo_estimado, fecha
    └── items/ (subcolección)
        cantidad, nombre, precio_unitario, producto_id

transacciones/
  {transaccionId}/
    usuario_id, pedido_id, monto, estado,
    mp_payment_id, fecha_creacion, fecha_confirmacion

notificaciones/
  {notifId}/
    usuario_id, pedido_id, mensaje, leida, fecha
```

---

## Instalación y Configuración

### App Android

**1.** Clona el repositorio:
```bash
git clone https://github.com/KD08GG/SACU.git
```

**2.** Abre la carpeta `app/` en Android Studio.

**3.** Descarga `google-services.json` desde Firebase Console → Configuración del proyecto → tu app Android y colócalo en `app/`.

**4.** Sincroniza Gradle y corre la app.

---

### Panel de Cocina

**1.** Entra a la carpeta del panel:
```bash
cd panel-cocina
```

**2.** Instala las dependencias:
```bash
pip install customtkinter firebase-admin
```

**3.** Descarga tu `serviceAccountKey.json` desde Firebase Console → Configuración del proyecto → Cuentas de servicio → Generar nueva clave privada. Colócalo en `panel-cocina/`.

**4.** Corre el panel:
```bash
python main.py
```

---

## Flujo del Sistema

```
1. Usuario abre la app e inicia sesión con su matrícula
2. Consulta el menú cargado desde Firestore
3. Agrega productos al carrito y confirma el pedido
4. Firebase Function crea una transacción en estado PENDIENTE
5. Usuario completa el pago en Mercado Pago
6. Webhook confirma el pago → pedido pasa a PENDIENTE en cocina
7. Panel de cocina recibe el pedido en tiempo real
8. Personal marca el pedido como LISTO
9. Firestore Listener notifica al usuario en la app al instante
10. Usuario recoge su pedido
```

---

## Estados del Pedido

```
PENDIENTE → EN_PREPARACION → LISTO → ENTREGADO
```
