#!/usr/bin/env bash
# Evidencia reproducible de HU-04 (Registrador de correspondencia) - Housing Control.
# Ejecutar desde cualquier carpeta del repo, en Git Bash:
#   bash scripts/evidencia-hu04.sh              # corre todo y cierra la aplicacion
#   bash scripts/evidencia-hu04.sh --mantener   # deja la app arriba para abrir /h2-console
# Resultado: docs/evidencia-hu04-demo.txt
set -u

cd "$(dirname "$0")/../backend/walkin-skeleton/software" || exit 1
SALIDA="../../../docs/evidencia-hu04-demo.txt"
BASE="http://localhost:8080"
API="$BASE/api/correspondencias"
CEDULA="1010000002"          # residente del perfil h2 (src/main/resources/data-h2.sql)
TMP_MVN="$(mktemp)"; TMP_APP="$(mktemp)"
PID=""; FALLAS=0

limpiar() { [ -n "$PID" ] && kill "$PID" 2>/dev/null; rm -f "$TMP_MVN" "$TMP_APP"; }
trap limpiar EXIT

: > "$SALIDA"
log() { echo "$@" | tee -a "$SALIDA"; }
verificar() {   # verificar "que se comprueba" esperado obtenido
  if [ "$2" = "$3" ]; then log "  OK     $1 (esperado $2)"; else log "  FALLA  $1 (esperado $2, obtenido $3)"; FALLAS=$((FALLAS+1)); fi
}
llamar() {      # llamar METODO URL [cuerpo]  -> deja CODIGO y CUERPO
  local R
  if [ $# -ge 3 ]; then
    R="$(curl -s -w '\n%{http_code}' -X "$1" "$2" -H 'Content-Type: application/json' -d "$3")"
  else
    R="$(curl -s -w '\n%{http_code}' -X "$1" "$2")"
  fi
  CODIGO="$(echo "$R" | tail -1)"; CUERPO="$(echo "$R" | sed '$d')"
}

log "# Evidencia HU-04 - $(date '+%Y-%m-%d %H:%M')"
log ""
log "## 1. Pruebas  (./mvnw -B verify)"
./mvnw -B verify > "$TMP_MVN" 2>&1
CODIGO_MVN=$?
grep -E "Tests run:.*Fail|BUILD" "$TMP_MVN" | tee -a "$SALIDA"
if [ $CODIGO_MVN -ne 0 ]; then log "verify FALLO (codigo $CODIGO_MVN). Revisa: ./mvnw verify"; exit 1; fi
log ""
log "Clases de HU-04:"
grep -h "Tests run" target/surefire-reports/*Correspondencia*.txt | tee -a "$SALIDA"

log ""
log "## 2. Arranque con el perfil h2 (sin Docker)"
JAR="$(ls target/software-*.jar 2>/dev/null | grep -v '\.original$' | head -1)"
if [ -z "$JAR" ]; then log "No encontre el .jar en target/"; exit 1; fi
java -jar "$JAR" --spring.profiles.active=h2 > "$TMP_APP" 2>&1 &
PID=$!
LISTA=0
for _ in $(seq 1 90); do
  C="$(curl -s -o /dev/null -w '%{http_code}' "$BASE/h2-console/")"
  if [ "$C" = "200" ] || [ "$C" = "302" ]; then LISTA=1; break; fi
  sleep 1
done
if [ $LISTA -ne 1 ]; then log "La aplicacion no arranco en 90 s (puerto 8080 ocupado?). Ultimas lineas:"; tail -15 "$TMP_APP" | tee -a "$SALIDA"; exit 1; fi
log "Aplicacion arriba. Consola H2 responde en $BASE/h2-console"

log ""
log "## 3. Flujo de HU-04"
log "\$ POST /api/correspondencias   {descripcion, cedula_usuarios=$CEDULA}"
llamar POST "$API" "{\"descripcion\":\"paquete mediano\",\"cedula_usuarios\":\"$CEDULA\"}"
log "$CUERPO"; verificar "registrar" 201 "$CODIGO"
ID="$(echo "$CUERPO" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)"
if [ -z "$ID" ]; then log "No pude leer el id de la respuesta."; exit 1; fi
verificar "estado inicial" '"estado":"RECIBIDO"' "$(echo "$CUERPO" | grep -o '"estado":"[A-Z]*"')"

log "\$ PATCH /api/correspondencias/$ID/notificar"
llamar PATCH "$API/$ID/notificar"; log "$CUERPO"; verificar "notificar" 200 "$CODIGO"
verificar "estado" '"estado":"NOTIFICADO"' "$(echo "$CUERPO" | grep -o '"estado":"[A-Z]*"')"

log "\$ PATCH /api/correspondencias/$ID/entregar"
llamar PATCH "$API/$ID/entregar"; log "$CUERPO"; verificar "entregar" 200 "$CODIGO"
verificar "estado" '"estado":"ENTREGADO"' "$(echo "$CUERPO" | grep -o '"estado":"[A-Z]*"')"

log ""
log "## 4. Errores que prometen los criterios"
llamar PATCH "$API/$ID/entregar";  verificar "entregar un paquete ya entregado" 409 "$CODIGO"
llamar PATCH "$API/$ID/notificar"; verificar "notificar un paquete ya entregado" 409 "$CODIGO"
llamar PATCH "$API/00000000-0000-0000-0000-000000000000/notificar"; verificar "paquete inexistente" 404 "$CODIGO"
llamar POST "$API" "{\"descripcion\":\"\",\"cedula_usuarios\":\"$CEDULA\"}";  verificar "sin descripcion" 400 "$CODIGO"
llamar POST "$API" "{\"descripcion\":\"sobre\",\"cedula_usuarios\":\"\"}";   verificar "sin destinatario" 400 "$CODIGO"
llamar POST "$API" "{\"descripcion\":\"sobre\",\"cedula_usuarios\":\"0000\"}"; verificar "destinatario que no existe" 400 "$CODIGO"

log ""
log "## 5. Base de datos"
log "Abre $BASE/h2-console"
log "  JDBC URL: jdbc:h2:mem:housing;MODE=PostgreSQL;DB_CLOSE_DELAY=-1   User: sa   Password: (vacio)"
log "  Consulta: SELECT * FROM CORRESPONDENCIAS;"
log "Toma la captura de esa consulta y guardala en docs/ junto a este archivo."

log ""
if [ $FALLAS -eq 0 ]; then log "RESULTADO: todas las verificaciones OK"; else log "RESULTADO: $FALLAS verificacion(es) con FALLA"; fi

if [ "${1:-}" = "--mantener" ]; then
  log ""; log "La aplicacion sigue corriendo. Ctrl+C para cerrarla."
  wait "$PID"
fi
[ $FALLAS -eq 0 ]
