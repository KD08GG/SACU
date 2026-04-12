import firebase_admin
from firebase_admin import credentials, firestore

# Conectar con Firebase
cred = credentials.Certificate("serviceAccountKey.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Prueba: leer productos de Firestore
productos = db.collection("productos").get()

print(f"Conexión exitosa. Productos encontrados: {len(productos)}")
for doc in productos:
    data = doc.to_dict()
    print(f"  - {data.get('nombre')} / ${data.get('precio')}")