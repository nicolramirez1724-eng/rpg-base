-- =============================================================================
-- Base de Datos: rpg_arena_db
-- Proyecto: Arena Segura - Login, Bestiario y Ranking con JDBC + MVC
-- =============================================================================

CREATE DATABASE IF NOT EXISTS rpg_arena_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rpg_arena_db;

-- 1. Tabla: jugadores
-- Requisitos: usuario unico, clave y rol (jugador o admin)
CREATE TABLE IF NOT EXISTS jugadores (
    id INT AUTO_INCREMENT PRIMARY KEY,
    usuario VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(255) NOT NULL,
    rol ENUM('jugador', 'admin') NOT NULL DEFAULT 'jugador',
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- 2. Tabla: monstruos
-- Requisitos: nombre unico, vida y ataque
CREATE TABLE IF NOT EXISTS monstruos (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    vida INT NOT NULL,
    ataque INT NOT NULL,
    defensa INT NOT NULL DEFAULT 5
) ENGINE=InnoDB;

-- 3. Tabla: victorias
-- Requisitos: relacion con jugador y monstruo, mas la fecha
CREATE TABLE IF NOT EXISTS victorias (
    id INT AUTO_INCREMENT PRIMARY KEY,
    jugador_id INT NOT NULL,
    monstruo_id INT NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_victorias_jugador FOREIGN KEY (jugador_id) REFERENCES jugadores(id) ON DELETE CASCADE,
    CONSTRAINT fk_victorias_monstruo FOREIGN KEY (monstruo_id) REFERENCES monstruos(id) ON DELETE RESTRICT
) ENGINE=InnoDB;

-- =============================================================================
-- Datos iniciales de prueba (Seed Data)
-- Claves en texto plano y hashes SHA-256 soportados por la logica de autenticacion
-- Clave de admin: 'admin123' -> SHA-256: 240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9
-- Clave de guerrero1: 'clave123' -> SHA-256: 4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2
-- =============================================================================

INSERT INTO jugadores (usuario, clave, rol) VALUES
('admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'admin'),
('Thor', '4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2', 'jugador'),
('Valkiria', '4813494d137e1631bba301d5acab6e7bb7aa74ce1185d456565ef51d737677b2', 'jugador')
ON DUPLICATE KEY UPDATE usuario=VALUES(usuario);

INSERT INTO monstruos (nombre, vida, ataque, defensa) VALUES
('Goblin', 50, 8, 2),
('Arana Gigante', 60, 10, 3),
('Ogro', 100, 14, 6),
('Dragon Joven', 160, 20, 10)
ON DUPLICATE KEY UPDATE nombre=VALUES(nombre);

-- Victorias de ejemplo para verificar rankingPorJugador() y victoriasDeJugador()
INSERT INTO victorias (jugador_id, monstruo_id, fecha) VALUES
(1, 1, NOW() - INTERVAL 3 DAY),
(1, 2, NOW() - INTERVAL 2 DAY),
(2, 1, NOW() - INTERVAL 1 DAY),
(2, 3, NOW())
ON DUPLICATE KEY UPDATE id=id;
