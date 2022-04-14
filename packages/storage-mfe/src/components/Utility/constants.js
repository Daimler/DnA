export const SESSION_STORAGE_KEYS = {
  JWT: 'jwt',
  PAGINATION_MAX_ITEMS_PER_PAGE: 'paginationMaxItemsPerPage',
};

export const IMAGE_EXTNS = ['png', 'jpg', 'jpeg', 'bmp', 'gif', 'webp'];
export const PREVIEW_ALLOWED_EXTNS = [
  'png',
  'jpg',
  'jpeg',
  'bmp',
  'gif',
  'webp',
  'html',
  'md',
  'txt',
  'js',
  'py',
  'ts',
  'tsx',
  'jsx',
  'json',
  'scss',
  'css',
  'java',
  'yml',
  'yaml',
  'pdf',
];

// set corresponding modes based on the file extensions
export const aceEditorMode = {
  java: 'java',
  js: 'javascript',
  jsx: 'javascript',
  ts: 'typescript',
  tsx: 'typescript',
  py: 'python',
  json: 'json',
  css: 'css',
  scss: 'scss',
  txt: 'text',
  yml: 'yaml',
  yaml: 'yaml',
  html: 'html',
  htm: 'html',
  md: 'markdown',
  less: 'less',
  kt: 'kotlin',
  kts: 'kotlin',
  sql: 'sql',
  go: 'golang',
};
