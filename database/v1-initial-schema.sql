-- ==========================================
-- 1. CREACIÓN DE TIPOS ENUMERADOS (ENUMS)
-- ==========================================
CREATE TYPE tipo_documento_enum AS ENUM ('CC', 'CE', 'NIT', 'PASAPORTE');
CREATE TYPE estado_paquete_enum AS ENUM ('RECIBIDO', 'NOTIFICADO', 'ENTREGADO');
CREATE TYPE estado_pqrs_enum AS ENUM ('PENDIENTE', 'EN_PROCESO', 'RESUELTA');
CREATE TYPE estado_reserva_enum AS ENUM ('PENDIENTE', 'CONFIRMADA', 'RECHAZADA', 'REPROGRAMADA');

-- ==========================================
-- 2. CREACIÓN DE TABLAS MAESTRAS
-- ==========================================
CREATE TABLE roles (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL
);

CREATE TABLE zonas_comunes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(255) NOT NULL,
    capacidad_maxima INT NOT NULL
);

CREATE TABLE usuarios (
    cedula VARCHAR(50) PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    tipo_documento tipo_documento_enum NOT NULL,
    credenciales VARCHAR(255) NOT NULL,
    rol_id INT REFERENCES roles(id) ON DELETE RESTRICT
);

-- ==========================================
-- 3. CREACIÓN DE TABLAS TRANSACCIONALES
-- ==========================================
CREATE TABLE visitantes (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cedula VARCHAR(50) NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    tipo_documento tipo_documento_enum NOT NULL,
    fecha_prevista TIMESTAMP,
    fecha_ingreso TIMESTAMP NOT NULL,
    cedula_usuarios VARCHAR(50) REFERENCES usuarios(cedula) ON DELETE CASCADE
);

CREATE TABLE correspondencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    descripcion TEXT,
    fecha_recepcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado estado_paquete_enum NOT NULL,
    nombre_destinatario VARCHAR(255) NOT NULL,
    cedula_usuarios VARCHAR(50) REFERENCES usuarios(cedula) ON DELETE CASCADE
);

CREATE TABLE pqrs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    asunto VARCHAR(255) NOT NULL,
    descripcion TEXT NOT NULL,
    estado estado_pqrs_enum NOT NULL,
    respuesta TEXT NOT NULL,
    cedula_usuarios VARCHAR(50) REFERENCES usuarios(cedula) ON DELETE CASCADE
);

CREATE TABLE reservas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    fecha_reserva TIMESTAMP NOT NULL,
    estado estado_reserva_enum NOT NULL,
    zonas_comunes_id UUID NOT NULL REFERENCES zonas_comunes(id) ON DELETE CASCADE,
    cedula_usuarios VARCHAR(50) NOT NULL REFERENCES usuarios(cedula) ON DELETE CASCADE,
    
    -- Restricción UNIQUE para prevención de concurrencia
    CONSTRAINT unique_reserva_zona_fecha UNIQUE (zonas_comunes_id, fecha_reserva)
);
