const { crearPQRS, _reset } = require("../src/pqrs");

describe("crearPQRS", () => {
  beforeEach(() => {
    _reset();
  });

  test("crea una PQRS con estado inicial PENDIENTE", () => {
    const pqrs = crearPQRS({
      asunto: "Ruido en zona común",
      descripcion: "Hay ruido excesivo los fines de semana en el salón social.",
    });

    expect(pqrs.id).toBeDefined();
    expect(pqrs.asunto).toBe("Ruido en zona común");
    expect(pqrs.estado).toBe("PENDIENTE");
  });

  test("lanza error si falta el asunto o la descripción", () => {
    expect(() => crearPQRS({ descripcion: "Sin asunto" })).toThrow();
    expect(() => crearPQRS({ asunto: "Sin descripción" })).toThrow();
  });
});
