import { useMemo, useState } from 'react';

export type TableColumn<T> = {
  key: keyof T;
  label: string;
  render?: (item: T) => React.ReactNode;
};

export type DataTableProps<T> = {
  data: T[];
  columns: Array<TableColumn<T>>;
  pageSize?: number;
};

export function DataTable<T>({ data, columns, pageSize = 5 }: DataTableProps<T>) {
  const [page, setPage] = useState(0);
  const [search, setSearch] = useState('');

  const filtered = useMemo(() => {
    if (!search) return data;
    return data.filter((row) =>
      Object.values(row).some((value) => value?.toString().toLowerCase().includes(search.toLowerCase()))
    );
  }, [data, search]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize));
  const start = page * pageSize;
  const rows = filtered.slice(start, start + pageSize);

  return (
    <section className="rounded-lg bg-white p-6 shadow-sm dark:bg-neutral-900">
      <div className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
        <h2 className="text-lg font-semibold text-brand-primary dark:text-brand-secondary">Recent Players</h2>
        <input
          value={search}
          onChange={(event) => {
            setSearch(event.target.value);
            setPage(0);
          }}
          className="w-full rounded-md border border-neutral-200 bg-white px-3 py-2 text-sm text-neutral-700 outline-none focus:border-brand-primary focus:ring-2 focus:ring-brand-primary/40 dark:border-neutral-700 dark:bg-neutral-950 dark:text-neutral-100"
          placeholder="Search by name, email or status"
        />
      </div>
      <div className="mt-4 overflow-x-auto">
        <table className="min-w-full divide-y divide-neutral-200 text-sm dark:divide-neutral-800">
          <thead className="bg-neutral-50 dark:bg-neutral-900/60">
            <tr>
              {columns.map((column) => (
                <th key={String(column.key)} className="px-4 py-2 text-left font-medium text-neutral-500 dark:text-neutral-300">
                  {column.label}
                </th>
              ))}
            </tr>
          </thead>
          <tbody className="divide-y divide-neutral-200 dark:divide-neutral-800">
            {rows.map((item, index) => (
              <tr key={index} className="hover:bg-neutral-50 dark:hover:bg-neutral-900">
                {columns.map((column) => (
                  <td key={String(column.key)} className="px-4 py-2 text-neutral-700 dark:text-neutral-100">
                    {column.render ? column.render(item) : (item[column.key] as React.ReactNode)}
                  </td>
                ))}
              </tr>
            ))}
            {rows.length === 0 && (
              <tr>
                <td className="px-4 py-6 text-center text-neutral-500" colSpan={columns.length}>
                  No results found.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>
      <div className="mt-4 flex items-center justify-between text-xs text-neutral-500 dark:text-neutral-300">
        <span>
          Showing {Math.min(filtered.length, start + 1)}-{Math.min(filtered.length, start + rows.length)} of {filtered.length}
        </span>
        <div className="flex items-center gap-2">
          <button
            onClick={() => setPage((current) => Math.max(0, current - 1))}
            className="rounded-md border border-neutral-200 px-3 py-1 font-medium text-neutral-600 transition hover:border-brand-primary hover:text-brand-primary disabled:cursor-not-allowed disabled:opacity-40 dark:border-neutral-700 dark:text-neutral-200"
            disabled={page === 0}
          >
            Previous
          </button>
          <button
            onClick={() => setPage((current) => Math.min(totalPages - 1, current + 1))}
            className="rounded-md border border-neutral-200 px-3 py-1 font-medium text-neutral-600 transition hover:border-brand-primary hover:text-brand-primary disabled:cursor-not-allowed disabled:opacity-40 dark:border-neutral-700 dark:text-neutral-200"
            disabled={page >= totalPages - 1}
          >
            Next
          </button>
        </div>
      </div>
    </section>
  );
}
