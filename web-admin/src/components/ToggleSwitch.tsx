import clsx from 'clsx';
import { Switch } from '@headlessui/react';

type ToggleSwitchProps = {
  enabled: boolean;
  onChange: (value: boolean) => void;
  label: string;
};

export function ToggleSwitch({ enabled, onChange, label }: ToggleSwitchProps) {
  return (
    <Switch.Group as="div" className="flex items-center justify-between gap-4 rounded-lg border border-neutral-200 bg-white p-4 dark:border-neutral-800 dark:bg-neutral-900">
      <Switch.Label className="text-sm font-medium text-neutral-600 dark:text-neutral-200">{label}</Switch.Label>
      <Switch
        checked={enabled}
        onChange={onChange}
        className={clsx(
          'relative inline-flex h-6 w-11 items-center rounded-full transition',
          enabled ? 'bg-brand-primary' : 'bg-neutral-300 dark:bg-neutral-700'
        )}
      >
        <span
          className={clsx(
            'inline-block h-4 w-4 transform rounded-full bg-white transition',
            enabled ? 'translate-x-6' : 'translate-x-1'
          )}
        />
      </Switch>
    </Switch.Group>
  );
}
