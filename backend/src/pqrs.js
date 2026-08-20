let pqrsList = [];
let nextId = 1;

function crearPQRS({ asunto, descripcion }) {
  if (!asunto || !descripcion) {
    throw new Error("asunto y descripcion son obligatorios");
  }

  const nuevaPQRS = {
    id: nextId++,
    asunto,
    descripcion,
    estado: "PENDIENTE",
    creadoEn: new Date().toISOString(),
  };

  pqrsList.push(nuevaPQRS);
  return nuevaPQRS;
}

function listarPQRS() {
  return pqrsList;
}

// Solo para pruebas: reinicia el estado en memoria entre tests.
function _reset() {
  pqrsList = [];
  nextId = 1;
}

module.exports = { crearPQRS, listarPQRS, _reset };
