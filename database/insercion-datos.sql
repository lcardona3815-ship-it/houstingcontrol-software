-- ==========================================
-- 1. POBLAMIENTO DE TABLAS MAESTRAS
-- ==========================================

-- ROLES (20 registros)
INSERT INTO roles (nombre) VALUES 
('Administrador Principal'), ('Sub-Administrador'), ('Residente Propietario'), ('Residente Arrendatario'),
('Guarda de Seguridad'), ('Supervisor de Seguridad'), ('Mantenimiento'), ('Admin');

-- ZONAS COMUNES (20 registros con UUID estático para relación foránea)
INSERT INTO zonas_comunes (nombre, capacidad_maxima) VALUES 
('Salón Social Torre B', 80),
('Piscina', 30),
('Gimnasio', 20),
('Zona BBQ', 12),
('Cancha Múltiple', 22),
('Parque Infantil', 25),
('Salón de Juegos', 20),
('Sala de Cine', 15),
('Coworking', 18);

-- USUARIOS (20 registros asignando diferentes roles)
INSERT INTO usuarios (cedula, nombre, tipo_documento, credenciales, rol_id) VALUES 
('1010000001', 'Carlos Ramírez', 'CC', 'hash_pw_1', 1),
('1010000002', 'María Fernanda López', 'CC', 'hash_pw_2', 3),
('1010000003', 'Andrés Felipe Gómez', 'CC', 'hash_pw_3', 3),
('1010000004', 'Diana Marcela Torres', 'CC', 'hash_pw_4', 4),
('1010000005', 'Jorge Enrique Castro', 'CC', 'hash_pw_5', 3),
('1010000006', 'Laura Sofía Méndez', 'CC', 'hash_pw_6', 4),
('1010000007', 'William Duarte', 'CE', 'hash_pw_7', 4),
('1010000008', 'Pedro Pablo León', 'CC', 'hash_pw_8', 5),
('1010000009', 'Consorcio Seguridad Ltda', 'NIT', 'hash_pw_9', 7),
('1010000010', 'Johanna Ruiz', 'CC', 'hash_pw_10', 3),
('1010000011', 'Esteban Marín', 'CC', 'hash_pw_11', 4),
('1010000012', 'John Smith', 'PASAPORTE', 'hash_pw_12', 4),
('1010000013', 'Valeria Guzmán', 'CC', 'hash_pw_13', 3),
('1010000014', 'Ricardo Pinto', 'CC', 'hash_pw_14', 3),
('1010000015', 'Soluciones de Aseo SAS', 'NIT', 'hash_pw_15', 3),
('1010000016', 'Ana Victoria Ríos', 'CC', 'hash_pw_16', 3),
('1010000017', 'Sergio Mendoza', 'CC', 'hash_pw_17', 4),
('1010000018', 'Catalina Buitrago', 'CC', 'hash_pw_18', 3),
('1010000019', 'Felipe Ortiz', 'CC', 'hash_pw_19', 3),
('1010000020', 'Sebastian Buitrago', 'CC', 'hash_pw_20', 8);

-- ==========================================
-- 2. POBLAMIENTO DE TABLAS TRANSACCIONALES
-- ==========================================

-- VISITANTES (20 registros asociados a los usuarios)
INSERT INTO visitantes (cedula, nombre, tipo_documento, fecha_prevista, fecha_ingreso, cedula_usuarios) VALUES 
('9000000001', 'Héctor Salamanca', 'CC', '2026-09-10 14:00:00', '2026-09-10 14:05:00', '1010000002'),
('9000000002', 'Carmen Rojas', 'CC', '2026-09-10 15:30:00', '2026-09-10 15:28:00', '1010000002'),
('9000000003', 'Luis Fernando Restrepo', 'CC', NULL, '2026-09-11 09:15:00', '1010000003'),
('9000000004', 'Domiciliario Rappi', 'CC', NULL, '2026-09-11 12:45:00', '1010000004'),
('9000000005', 'Servicio de Internet', 'NIT', '2026-09-12 08:00:00', '2026-09-12 08:10:00', '1010000005'),
('9000000006', 'Marta Liliana Díaz', 'CC', '2026-09-12 16:00:00', '2026-09-12 16:02:00', '1010000006'),
('9000000007', 'Emily Watson', 'PASAPORTE', '2026-09-13 10:00:00', '2026-09-13 09:55:00', '1010000007'),
('9000000008', 'Domiciliario MercadoLibre', 'CC', NULL, '2026-09-13 14:20:00', '1010000010'),
('9000000009', 'Roberto Escobar', 'CC', '2026-09-14 19:00:00', '2026-09-14 19:15:00', '1010000011'),
('9000000010', 'Técnico Lavadora', 'CC', '2026-09-15 11:00:00', '2026-09-15 11:05:00', '1010000013'),
('9000000011', 'Sofía Vergara', 'CC', NULL, '2026-09-15 18:30:00', '1010000016'),
('9000000012', 'Instalador de Gas', 'CC', '2026-09-16 07:30:00', '2026-09-16 07:45:00', '1010000017'),
('9000000013', 'Domiciliario Farmacia', 'CC', NULL, '2026-09-16 20:00:00', '1010000018'),
('9000000014', 'Santiago Acosta', 'CC', '2026-09-17 15:00:00', '2026-09-17 15:10:00', '1010000019'),
('9000000015', 'Familia Pineda', 'CC', '2026-09-18 12:00:00', '2026-09-18 12:00:00', '1010000020'),
('9000000016', 'Juan Carlos Varela', 'CC', NULL, '2026-09-19 14:40:00', '1010000003'),
('9000000017', 'Repartidor Amazon', 'CC', NULL, '2026-09-19 16:20:00', '1010000004'),
('9000000018', 'Sandra Milena Ruiz', 'CC', '2026-09-20 09:00:00', '2026-09-20 09:05:00', '1010000010'),
('9000000019', 'Profesor Clases Particulares', 'CC', '2026-09-21 17:00:00', '2026-09-21 16:55:00', '1010000013'),
('9000000020', 'Amigo de la Universidad', 'CC', NULL, '2026-09-21 19:30:00', '1010000018');

-- CORRESPONDENCIAS (20 registros)
INSERT INTO correspondencias (descripcion, fecha_recepcion, estado, nombre_destinatario, cedula_usuarios) VALUES 
('Caja pequeña MercadoLibre', '2026-09-01 10:15:00', 'ENTREGADO', 'María Fernanda López', '1010000002'),
('Sobre de banco', '2026-09-02 11:20:00', 'ENTREGADO', 'Andrés Felipe Gómez', '1010000003'),
('Paquete grande Amazon', '2026-09-03 14:05:00', 'ENTREGADO', 'Diana Marcela Torres', '1010000004'),
('Recibo de luz', '2026-09-04 09:00:00', 'ENTREGADO', 'Jorge Enrique Castro', '1010000005'),
('Caja de zapatos', '2026-09-05 16:45:00', 'ENTREGADO', 'Laura Sofía Méndez', '1010000006'),
('Sobre confidencial', '2026-09-06 08:30:00', 'ENTREGADO', 'William Duarte', '1010000007'),
('Paquete Aliexpress', '2026-09-07 13:15:00', 'NOTIFICADO', 'Johanna Ruiz', '1010000010'),
('Revista mensual', '2026-09-08 10:00:00', 'NOTIFICADO', 'Esteban Marín', '1010000011'),
('Caja mediana de ropa', '2026-09-09 11:30:00', 'RECIBIDO', 'John Smith', '1010000012'),
('Documentos legales', '2026-09-09 15:20:00', 'ENTREGADO', 'Valeria Guzmán', '1010000013'),
('Factura de gas', '2026-09-10 09:10:00', 'RECIBIDO', 'Ana Victoria Ríos', '1010000016'),
('Paquete de libros', '2026-09-10 14:50:00', 'NOTIFICADO', 'Sergio Mendoza', '1010000017'),
('Caja pequeña electrónica', '2026-09-11 11:05:00', 'RECIBIDO', 'Catalina Buitrago', '1010000018'),
('Sobre manila', '2026-09-11 16:30:00', 'RECIBIDO', 'Felipe Ortiz', '1010000019'),
('Paquete de cosméticos', '2026-09-12 10:15:00', 'ENTREGADO', 'Juliana Pineda', '1010000020'),
('Regalo de cumpleaños', '2026-09-12 13:40:00', 'NOTIFICADO', 'María Fernanda López', '1010000002'),
('Repuestos de computador', '2026-09-13 09:25:00', 'RECIBIDO', 'Andrés Felipe Gómez', '1010000003'),
('Medicamentos', '2026-09-13 11:55:00', 'ENTREGADO', 'Jorge Enrique Castro', '1010000005'),
('Herramientas', '2026-09-14 14:10:00', 'NOTIFICADO', 'Valeria Guzmán', '1010000013'),
('Caja de mercado', '2026-09-14 17:00:00', 'RECIBIDO', 'Felipe Ortiz', '1010000019');

-- PQRS (20 registros)
INSERT INTO pqrs (asunto, descripcion, estado, respuesta, cedula_usuarios) VALUES 
('Ruido excesivo Torre B', 'Música a alto volumen después de las 10 PM.', 'RESUELTA', 'Se notificó al residente mediante llamado de atención.', '1010000004'),
('Fallo en ascensor', 'El ascensor 2 de la Torre A se queda atascado.', 'EN_PROCESO', 'Técnico programado para revisión mañana.', '1010000002'),
('Humedad en parqueadero', 'Filtración de agua sobre mi vehículo.', 'PENDIENTE', '', '1010000005'),
('Control de mascotas', 'Perro suelto en zonas verdes sin correa.', 'RESUELTA', 'Se envió circular recordando el manual de convivencia.', '1010000016'),
('Lámpara fundida', 'Pasillo piso 5 Torre B oscuro.', 'RESUELTA', 'Bombillo reemplazado por mantenimiento.', '1010000011'),
('Consulta saldo', 'Duda sobre el cobro de la administración de este mes.', 'RESUELTA', 'Se envió el estado de cuenta al correo.', '1010000017'),
('Olor a gas', 'Fuerte olor a gas cerca al shut de basuras.', 'RESUELTA', 'Bomberos descartaron fuga, se lavó el shut.', '1010000006'),
('Uso de piscina', 'Personas ingresando sin gorro de baño.', 'EN_PROCESO', 'Se reforzará vigilancia por parte del piscinero.', '1010000020'),
('Reserva cancelada', 'Mi reserva del BBQ fue cancelada sin aviso.', 'RESUELTA', 'Se canceló por mantenimiento preventivo, se reprogramó.', '1010000013'),
('Daño en talanquera', 'La talanquera de salida golpeó mi carro.', 'PENDIENTE', '', '1010000003'),
('Felicitación guarda', 'El guarda del turno de la noche es muy amable.', 'RESUELTA', 'Se remitió la felicitación a la empresa de seguridad.', '1010000018'),
('Basura en pasillos', 'Vecinos dejando bolsas de basura fuera del shut.', 'EN_PROCESO', 'Se están revisando cámaras para aplicar sanción.', '1010000010'),
('Recibo de paquete', 'Entregaron mi paquete a otro apartamento.', 'RESUELTA', 'Se recuperó el paquete y se entregó correctamente.', '1010000007'),
('Horario gimnasio', 'Solicitud para abrir el gimnasio a las 4 AM.', 'PENDIENTE', '', '1010000019'),
('Pintura fachada', '¿Cuándo terminan de pintar la torre A?', 'RESUELTA', 'El cronograma finaliza el próximo viernes.', '1010000002'),
('Cobro parqueadero', 'Cobro doble de parqueadero de visitantes.', 'EN_PROCESO', 'En validación con el área contable.', '1010000012'),
('Infiltración en techo', 'Gotera en el último piso por lluvias.', 'PENDIENTE', '', '1010000017'),
('Solicitud de paz y salvo', 'Necesito el certificado para la inmobiliaria.', 'RESUELTA', 'Documento adjunto en PDF enviado por correo.', '1010000011'),
('Mantenimiento jardines', 'El pasto está muy alto en la entrada.', 'RESUELTA', 'Jardinero ya realizó el corte.', '1010000005'),
('Problemas citófono', 'Mi citófono no timbra.', 'EN_PROCESO', 'Revisión técnica programada.', '1010000013');

-- RESERVAS (20 registros, cuidando el CONSTRAINT UNIQUE por zona y fecha)
INSERT INTO reservas (fecha_reserva, estado, zonas_comunes_id, cedula_usuarios) VALUES 
('2026-10-01 14:00:00', 'CONFIRMADA', '5920b73a-35d1-44b7-8037-b17e3dc8d68b', '1010000002'),
('2026-10-02 18:00:00', 'CONFIRMADA', 'a2961315-524b-47c0-9e51-6f9613a06d71', '1010000003');

commit ;