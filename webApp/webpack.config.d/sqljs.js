// The SQLite worker (sql.js) loads /sql-wasm.wasm: copy it next to the app bundle.
const CopyWebpackPlugin = require('copy-webpack-plugin');
config.plugins = config.plugins || [];
config.plugins.push(new CopyWebpackPlugin({
    patterns: [{ from: require.resolve('sql.js/dist/sql-wasm.wasm'), to: '.' }]
}));
// sql.js references Node modules it never uses in the browser.
config.resolve = config.resolve || {};
config.resolve.fallback = Object.assign({}, config.resolve.fallback, { fs: false, path: false, crypto: false });
