-- Datos de demostracion SOLO para el perfil h2 (HU-04). No se usan con PostgreSQL ni en las pruebas.
-- Son un subconjunto de database/insercion-datos.sql (mismas cedulas y nombres).
INSERT INTO roles (nombre) VALUES ('Residente Propietario'), ('Guarda de Seguridad');

INSERT INTO usuarios (cedula, nombre, tipo_documento, credenciales, rol_id) VALUES
('1010000002', 'María Fernanda López', 'CC', 'hash_pw_2', 1),
('1010000003', 'Andrés Felipe Gómez', 'CC', 'hash_pw_3', 1),
('1010000008', 'Pedro Pablo León', 'CC', 'hash_pw_8', 2);
