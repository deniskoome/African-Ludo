// Mask a string so that the admin UI can display partial identifiers without leaking secrets.
export function mask(value, visible = 4) {
  if (!value) {
    return '';
  }
  const last = value.slice(-visible);
  return `${'*'.repeat(Math.max(0, value.length - visible))}${last}`;
}
