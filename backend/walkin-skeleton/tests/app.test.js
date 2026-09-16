const request = require("supertest");
const app = require("../src/app");
const { _reset } = require("../src/pqrs");

describe("POST /pqrs (flujo extremo a extremo)", () => {
  beforeEach(() => {
    _reset();
  });

  test("registra una PQRS y la devuelve con estado PENDIENTE", async () => {
    const response = await request(app)
      .post("/pqrs")
      .send({ asunto: "Fuga de agua", descripcion: "Fuga en el parqueadero nivel 1." });

    expect(response.status).toBe(201);
    expect(response.body.estado).toBe("PENDIENTE");
    expect(response.body.asunto).toBe("Fuga de agua");
  });

  test("rechaza la solicitud si faltan campos obligatorios", async () => {
    const response = await request(app).post("/pqrs").send({});
    expect(response.status).toBe(400);
  });
});
