# Problema Duro 

 ## Problema Tecnico elegido

**Concurrencia/ Condiciones de carrera:** Prevencion de asignacion duplicada de zonas comunes bajo ejecucion simultanea.

## Descripcion del desafio tecnico
 
El módulo de **reserva de zonas comunes** es el punto del sistema con mayor riesgo técnico: varios residentes pueden intentar reservar la **misma zona común en el mismo horario al mismo tiempo** (por ejemplo, dos personas reservando el salón social el sábado 6-8pm desde dispositivos distintos, en el mismo instante).
Si el sistema no controla la concurrencia correctamente, es posible que ambas solicitudes se procesen como exitosas, generando una reserva duplicada — un error que en un sistema real de administración de conjuntos causaría conflictos entre residentes y pérdida de confianza en la plataforma.
El reto técnico consiste en garantizar que, ante solicitudes concurrentes, **solo una reserva sea aceptada** para un mismo espacio y horario, y que la otra reciba una respuesta clara de "horario no disponible", sin dejar el sistema en un estado inconsistente.

## Invariante del negocio 

- En ningun momento pueden coexistir dos reservas para la zona comun el mismo dia y la misma hora.
- Ante reservas simultaneas para la zona comun deseda por el usuario **una(1) debe prosperar y las restantes deben de ser rechazadas o reprogramadas para otra zona horaria disponible**

## Evidencia Especifica Exigida

Una prueba de integracion automatizada que simule al menos diez solicitudes de reserva concurrentes para el mismo espacio/horario, la prueba utilizara un CounthDownLatch y un pool de hilos (N=10) para disparar diez solicitudes concurrentes simultaeas de residentes distintos intentanmdo reservar el mismo y unico espacio disponible en ese horario y verifique que únicamente una quede confirmada en la base de datos.
La prueba pasara unicamente si el sistema demsuestra que exactamente una reserva prospero y quedo en estado confirmada, las 9 solicitudes restantes fueron rechazadas de forma segura o derivadas a un flujo de reprogramacion.   

## Estrategia Tecnica Propuesta
Evaluaremos la viabilidad de utilizar un bloque optimista mediante el control de versiones en la entidad (@version) frente a una restriccion de unicidad compuesta ('UNIQUE CONSTRAINT') en la base de datos PostgreeSQL basado en los campos (zona_comun_id, fecha_reserva, hora_reserva), 
 El objetivo es determinar cuál mecanismo ofrece el mejor rendimiento y consistencia para evitar reservas duplicadas en el mismo espacio y horario, documentando los resultados y el trade-off final en el archivo /docs/decisiones-tecnicas.md. 
