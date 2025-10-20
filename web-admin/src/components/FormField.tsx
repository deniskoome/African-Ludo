import { ReactNode } from 'react';

export type FormFieldProps = {
  id: string;
  label: string;
  hint?: string;
  children: ReactNode;
};

export function FormField({ id, label, hint, children }: FormFieldProps) {
  return (
    <label htmlFor={id} className="flex flex-col gap-1 text-sm">
      <span className="font-medium text-neutral-700 dark:text-neutral-100">{label}</span>
      {children}
      {hint && <span className="text-xs text-neutral-500 dark:text-neutral-400">{hint}</span>}
    </label>
  );
}
