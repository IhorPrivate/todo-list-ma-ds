/** Returns the trimmed text value of a form field, or an empty string when absent. */
export function textField(formData: FormData, name: string): string {
  const value = formData.get(name);
  return typeof value === 'string' ? value.trim() : '';
}
