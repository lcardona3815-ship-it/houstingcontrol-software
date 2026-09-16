
const express = require("express");
const { crearPQRS, listarPQRS } = require("./pqrs");

const app = express();
app.use(express.json());

// Flujo trivial de extremo a extremo: Frontend → Backend → (BD) → Backend → Frontend
// Caso: "Registrar una PQRS"
app.post("/pqrs", (req, res) => {
  try {
    const { asunto, descripcion } = req.body;
    const pqrs = crearPQRS({ asunto, descripcion });
    res.status(201).json(pqrs);
  } catch (err) {
    res.status(400).json({ error: err.message });
  }
});

app.get("/pqrs", (req, res) => {
  res.status(200).json(listarPQRS());
});

app.get("/health", (req, res) => {
  res.status(200).json({ status: "ok" });
});

const PORT = process.env.PORT || 3000;

if (require.main === module) {
  app.listen(PORT, () => {
    console.log(`Servidor corriendo en http://localhost:${PORT}`);
  });
}

module.exports = app;
