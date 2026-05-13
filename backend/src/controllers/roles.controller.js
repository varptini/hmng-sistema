const { query } = require('../db/pool');

const getAll = async (req, res, next) => {
  try {
    const result = await query('SELECT id, nombre, descripcion FROM roles ORDER BY id');
    res.json(result.rows);
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll };
