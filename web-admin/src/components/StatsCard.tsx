import clsx from 'clsx';
import { ReactNode } from 'react';

export type StatsCardProps = {
  title: string;
  value: string;
  trend?: string;
  trendDirection?: 'up' | 'down' | 'neutral';
  icon?: ReactNode;
};

const trendClasses = {
  up: 'text-emerald-500',
  down: 'text-rose-500',
  neutral: 'text-neutral-500',
};

export function StatsCard({ title, value, trend, trendDirection = 'neutral', icon }: StatsCardProps) {
  return (
    <article className="flex items-center justify-between rounded-lg bg-white p-4 shadow-sm dark:bg-neutral-900">
      <div>
        <p className="text-sm font-medium text-neutral-500 dark:text-neutral-200">{title}</p>
        <p className="mt-2 text-2xl font-semibold text-brand-primary dark:text-brand-secondary">{value}</p>
        {trend && <p className={clsx('mt-1 text-xs font-medium', trendClasses[trendDirection])}>{trend}</p>}
      </div>
      {icon && <div className="rounded-full bg-brand-primary/10 p-3 text-brand-primary dark:bg-brand-secondary/20">{icon}</div>}
    </article>
  );
}
