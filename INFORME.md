# Informe Técnico: Arena Segura (JDBC + MVC)

**Estudiante / Desarrollador:** Karen Nicol Perez  
**Proyecto:** Arena Segura - Login, Bestiario y Ranking con JDBC + MVC  
**Materia / Unidad:** Unidad 5.2 - JDBC y Arquitectura de Software  

---

## 1. Diagrama del Flujo: View -> Controller -> DAO -> Base de Datos (Login)

```
+---------------+        1. Credenciales (usuario, clave)        +-------------------+
|    LoginUI    | ---------------------------------------------> |  LoginController  |
|    (View)     |                                                |   (Controller)    |
|               | <--------------------------------------------- |                   |
+---------------+         6. Sesion activa (Jugador POJO)        +-------------------+
                                                                     |            ^
                                          2. autenticar(...)         |            | 5. Jugador
                                             (regla 3 intentos)      v            |    (POJO)
                                                                 +-------------------+
                                                                 |    JugadorDAO     |
                                                                 |      (Model)      |
                                                                 +-------------------+
                                                                     |            ^
                                          3. PreparedStatement       |            | 4. ResultSet
                                             (SQL precompilado)      v            |    (mapeado)
                                                                 +-------------------+
                                                                 |  MySQL / Database |
                                                                 |  (rpg_arena_db)   |
                                                                 +-------------------+
```

### Explicación del Ciclo:
1. **View (`LoginUI`)**: Solicita los datos de entrada por consola y los delega intactos a `LoginController`. La vista desconoce la existencia de la base de datos y de JDBC.
2. **Controller (`LoginController`)**: Aplica las reglas de negocio: valida formato, verifica si la sesión está bloqueada (máximo 3 intentos consecutivos) y, si procede, delega en `JugadorDAO`.
3. **DAO (`JugadorDAO`)**: Obtiene la conexión física con `ConexionDB` mediante `try-with-resources`, construye la consulta parametrizada con comodines `?` y precompila la sentencia con `PreparedStatement`.
4. **Base de Datos (`rpg_arena_db`)**: Ejecuta el plan de ejecución precompilado tratando los comodines estrictamente como literales de datos, neutralizando cualquier intento de inyección SQL.
5. **Mapeo a Objetos**: `JugadorDAO` extrae los datos del cursor `ResultSet`, valida la clave (contra hash SHA-256 o texto plano), instancia un POJO `Jugador` y cierra automáticamente todos los recursos JDBC (`ResultSet`, `PreparedStatement`, `Connection`).
6. **Retorno al Controlador y Vista**: El Controller actualiza el estado de la sesión (`intentosFallidos = 0`, `jugadorAutenticado = jugador`) y la View imprime la confirmación al usuario.

---

## 2. Tabla Comparativa: `Statement` vs `PreparedStatement`

Pruebas empíricas ejecutadas en el entorno local (1000 inserciones repetitivas bajo las mismas condiciones de base de datos):

| Criterio | `Statement` (Tradicional) | `PreparedStatement` (Parametrizado) |
| :--- | :--- | :--- |
| **Sintaxis y Concatenación** | Propenso a errores, ilegible (`"WHERE user = '" + u + "'"`). | Limpia, legible y desacoplada con comodines (`?`). |
| **Seguridad (Inyección SQL)** | **Críticamente Vulnerable.** `admin' OR 1=1 -- ` altera el árbol sintáctico y concede acceso sin clave. | **Inmunidad Total.** La DB recibe el árbol precompilado; el payload malicioso se evalúa solo como texto literal. |
| **Rendimiento (1000 Inserciones)** | **183 ms** (La DB analiza, valida y compila el SQL en cada inserción). | **44 ms** (Precompilado una sola vez en caché; solo viajan los parámetros binarios). |
| **Factor de Aceleración** | Base (1.0x). | **~4.1x más rápido** en operaciones repetitivas. |

---

## 3. ¿Cuándo se usa `executeQuery` y cuándo `executeUpdate`?

* **`executeQuery()`**:
  * **Cuándo se usa:** Exclusivamente en sentencias de lectura y consulta de datos (`SELECT`).
  * **Qué devuelve:** Un objeto `ResultSet`, que representa el cursor de filas devueltas por la base de datos para ser recorrido con `rs.next()`.
* **`executeUpdate()`**:
  * **Cuándo se usa:** En sentencias de manipulación o definición de datos (`INSERT`, `UPDATE`, `DELETE`, `CREATE TABLE`, etc.).
  * **Qué devuelve:** Un primitivo `int` que indica el número exacto de filas afectadas/modificadas en la tabla (o `0` para sentencias DDL).

---

## 4. Bitácora de Errores Encontrados y Solución Implementada ("Trampas")

1. **Trampa de URL en `config.txt`**: El archivo venía configurado con `rpg_db` en lugar de la base de datos requerida `rpg_arena_db`. Se corrigió la URL y se entregó `config.example.txt`.
2. **Dependencia MySQL ausente en `pom.xml`**: Se añadió `com.mysql:mysql-connector-j` (8.3.0) y soporte para H2 en pruebas.
3. **Violación de Integridad Referencial al Eliminar Monstruo**: Eliminar un monstruo con victorias provocaba una excepción no controlada (`FOREIGN KEY constraint fails`). Se implementó en `MonstruoController` una comprobación preventiva con `victoriaDAO.contarPorMonstruo(id)` antes del borrado.
4. **Regla de Bloqueo de 3 Intentos**: Se encapsuló el estado mutable en `LoginController`, bloqueando la sesión en el tercer fallo sin permitir llamadas al DAO.
5. **Condición de Carrera en la Vida del RPG**: Se protegió `Personaje.java` (`recibirDanio`, `curar`, `efectos`) con `synchronized` y se implementó `Serializable` con `serialVersionUID`.

---

## 5. Reto Extra: Hash SHA-256 y ¿Por qué sigue siendo necesario `PreparedStatement`?

Se implementó el cifrado hash SHA-256 en `SeguridadUtil.java` con `java.security.MessageDigest`. Al registrar un jugador, la contraseña nunca se guarda en texto plano, sino como un digest hexadecimal de 64 caracteres.

### ¿Por qué sigue siendo indispensable `PreparedStatement` aunque se use hash?
Aunque la contraseña esté hasheada, **la inyección SQL se realiza habitualmente sobre el campo de usuario**, como se demostró con `admin' OR 1=1 -- `. 
1. Si se concatena el nombre de usuario, el atacante inyecta `' OR 1=1 -- `, lo que **anula completamente la cláusula que verifica la contraseña** (sea hash o texto plano). La base de datos ignora la comprobación del hash porque la condición lógica ya fue forzada a verdadera (`TRUE`).
2. Además, concatenar cadenas en consultas como `INSERT`, `UPDATE` o `WHERE id = ...` expone a inyecciones de segundo orden y corrupción de sintaxis. 
`PreparedStatement` separa conceptual y físicamente la **lógica del comando SQL** de los **datos**, garantizando inmunidad estructural independientemente del algoritmo de hash utilizado.