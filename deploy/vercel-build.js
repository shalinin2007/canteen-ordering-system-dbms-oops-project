const fs = require('node:fs');
const path = require('node:path');

const projectRoot = path.resolve(__dirname, '..');
const staticDirectory = path.join(projectRoot, 'src', 'main', 'resources', 'static');
const outputDirectory = path.join(projectRoot, 'dist');

if (!fs.existsSync(staticDirectory)) {
  throw new Error(`Static site directory not found: ${staticDirectory}`);
}

fs.mkdirSync(outputDirectory, { recursive: true });
fs.cpSync(staticDirectory, outputDirectory, { recursive: true });
