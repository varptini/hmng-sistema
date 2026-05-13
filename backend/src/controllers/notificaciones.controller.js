const { query } = require('../db/pool');

const getAll = async (req, res, next) => {
  try {
    const [notifs, noLeidas] = await Promise.all([
      query(
        `SELECT id, tipo, mensaje, referencia_id, leida, created_at
         FROM notificaciones WHERE usuario_id = $1 ORDER BY created_at DESC LIMIT 30`,
        [req.user.id]
      ),
      query(
        'SELECT COUNT(*) FROM notificaciones WHERE usuario_id = $1 AND leida = false',
        [req.user.id]
      )
    ]);

    res.json({ data: notifs.rows, no_leidas: parseInt(noLeidas.rows[0].count) });
  } catch (err) {
    next(err);
  }
};

const marcarLeidas = async (req, res, next) => {
  try {
    await query('UPDATE notificaciones SET leida = true WHERE usuario_id = $1', [req.user.id]);
    res.json({ message: 'Notificaciones marcadas como leídas' });
  } catch (err) {
    next(err);
  }
};

module.exports = { getAll, marcarLeidas };
