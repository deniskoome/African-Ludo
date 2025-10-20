import { useMemo, useState } from 'react';
import { Bars3Icon, BellIcon, MoonIcon, SunIcon } from '@heroicons/react/24/outline';
import { players, gateways } from '../data/mockData';
import { StatsCard } from '../components/StatsCard';
import { DataTable, TableColumn } from '../components/DataTable';
import { LudoModal } from '../components/LudoModal';
import { ToggleSwitch } from '../components/ToggleSwitch';
import { FormField } from '../components/FormField';
import { useForm } from 'react-hook-form';

const stats = [
  { title: 'Players Online', value: '1,248', trend: '+6% vs yesterday', trendDirection: 'up' as const },
  { title: 'Wallet Total', value: 'KES 2.4M', trend: '+3% vs last week', trendDirection: 'up' as const },
  { title: 'Active Matches', value: '312', trend: 'Matchmaking stable', trendDirection: 'neutral' as const },
  { title: 'Support Tickets', value: '18 open', trend: '-4% vs last week', trendDirection: 'down' as const },
];

type TournamentForm = {
  name: string;
  entryFee: number;
  reward: string;
  startAt: string;
};

const playerColumns: Array<TableColumn<typeof players[number]>> = [
  { key: 'name', label: 'Player' },
  { key: 'email', label: 'Email' },
  {
    key: 'status',
    label: 'Status',
    render: (player) => (
      <span
        className="inline-flex items-center gap-2 rounded-full bg-brand-secondary/10 px-3 py-1 text-xs font-medium text-brand-secondary"
      >
        <span className={
          player.status === 'online'
            ? 'h-2 w-2 rounded-full bg-emerald-400'
            : player.status === 'in-match'
            ? 'h-2 w-2 rounded-full bg-amber-400'
            : 'h-2 w-2 rounded-full bg-neutral-400'
        } />
        {player.status.replace('-', ' ')}
      </span>
    ),
  },
  { key: 'wallet', label: 'Wallet' },
  { key: 'country', label: 'Country' },
];

export default function AppLayout() {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [darkMode, setDarkMode] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [gatewayState, setGatewayState] = useState(gateways);
  const { register, handleSubmit, reset } = useForm<TournamentForm>();

  const classes = useMemo(
    () =>
      `min-h-screen ${darkMode ? 'dark bg-brand-surface-dark text-neutral-50' : 'bg-brand-surface text-neutral-900'} transition-colors`,
    [darkMode]
  );

  const onCreateTournament = handleSubmit((data) => {
    console.info('Creating tournament', data);
    reset();
    setModalOpen(false);
  });

  return (
    <div className={classes}>
      <div className="flex min-h-screen">
        <aside
          className={`${
            sidebarOpen ? 'translate-x-0' : '-translate-x-full'
          } fixed inset-y-0 z-40 w-64 transform border-r border-neutral-200 bg-white p-6 transition-transform duration-200 ease-in-out dark:border-neutral-800 dark:bg-neutral-950 md:static md:translate-x-0`}
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-xs font-semibold uppercase tracking-wide text-brand-secondary">Ludo UI Kit</p>
              <h1 className="text-lg font-bold text-brand-primary">African Ludo Admin</h1>
            </div>
            <button
              onClick={() => setSidebarOpen(false)}
              className="md:hidden"
              aria-label="Close navigation"
            >
              ✕
            </button>
          </div>
          <nav className="mt-10 space-y-2 text-sm font-medium">
            {['Overview', 'Players', 'Wallet', 'Tournaments', 'Support'].map((item) => (
              <a
                key={item}
                href="#"
                className="block rounded-md px-3 py-2 text-neutral-600 transition hover:bg-brand-primary/10 hover:text-brand-primary dark:text-neutral-200 dark:hover:bg-brand-secondary/20"
              >
                {item}
              </a>
            ))}
          </nav>
          <div className="mt-10 space-y-4">
            {gatewayState.map((gateway) => (
              <ToggleSwitch
                key={gateway.id}
                enabled={gateway.enabled}
                label={`${gateway.name} — ${gateway.description}`}
                onChange={(enabled) =>
                  setGatewayState((current) =>
                    current.map((item) => (item.id === gateway.id ? { ...item, enabled } : item))
                  )
                }
              />
            ))}
          </div>
        </aside>

        <div className="flex-1">
          <header className="sticky top-0 z-30 border-b border-neutral-200 bg-white/90 backdrop-blur dark:border-neutral-800 dark:bg-neutral-950/80">
            <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-4">
              <div className="flex items-center gap-3">
                <button onClick={() => setSidebarOpen(true)} className="md:hidden" aria-label="Open navigation">
                  <Bars3Icon className="h-6 w-6 text-brand-primary" />
                </button>
                <h2 className="text-lg font-semibold text-brand-primary dark:text-brand-secondary">Operations Dashboard</h2>
              </div>
              <div className="flex items-center gap-3">
                <button className="rounded-full bg-brand-primary/10 p-2 text-brand-primary dark:bg-brand-secondary/20 dark:text-brand-secondary">
                  <BellIcon className="h-5 w-5" />
                </button>
                <button
                  onClick={() => setDarkMode((value) => !value)}
                  className="rounded-full bg-brand-primary/10 p-2 text-brand-primary dark:bg-brand-secondary/20 dark:text-brand-secondary"
                  aria-label="Toggle theme"
                >
                  {darkMode ? <SunIcon className="h-5 w-5" /> : <MoonIcon className="h-5 w-5" />}
                </button>
                <div className="h-10 w-10 rounded-full bg-brand-secondary/20" aria-hidden />
              </div>
            </div>
          </header>

          <main className="mx-auto max-w-6xl space-y-8 px-4 py-8">
            <section className="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
              {stats.map((card) => (
                <StatsCard key={card.title} {...card} />
              ))}
            </section>

            <section className="grid gap-6 lg:grid-cols-[2fr,1fr]">
              <DataTable data={players} columns={playerColumns} />
              <div className="space-y-6">
                <section className="rounded-lg bg-white p-6 shadow-sm dark:bg-neutral-900">
                  <h3 className="text-lg font-semibold text-brand-primary dark:text-brand-secondary">Launch new tournament</h3>
                  <p className="mt-1 text-sm text-neutral-500 dark:text-neutral-300">
                    Create match events and notify ready players in one click.
                  </p>
                  <button
                    onClick={() => setModalOpen(true)}
                    className="mt-4 w-full rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-primary/90 dark:bg-brand-secondary dark:text-brand-secondary-foreground"
                  >
                    Create tournament
                  </button>
                </section>

                <section className="rounded-lg bg-white p-6 shadow-sm dark:bg-neutral-900">
                  <h3 className="text-lg font-semibold text-brand-primary dark:text-brand-secondary">Payment health</h3>
                  <p className="mt-1 text-sm text-neutral-500 dark:text-neutral-300">
                    Keep a pulse on average wallet top ups and withdrawal times.
                  </p>
                  <dl className="mt-4 space-y-3 text-sm text-neutral-600 dark:text-neutral-200">
                    <div className="flex items-center justify-between">
                      <dt>Avg. top up value</dt>
                      <dd className="font-semibold">KES 840</dd>
                    </div>
                    <div className="flex items-center justify-between">
                      <dt>Avg. withdrawal time</dt>
                      <dd className="font-semibold">2h 35m</dd>
                    </div>
                    <div className="flex items-center justify-between">
                      <dt>Chargeback rate</dt>
                      <dd className="font-semibold text-emerald-500">0.4%</dd>
                    </div>
                  </dl>
                </section>
              </div>
            </section>
          </main>
        </div>
      </div>

      <LudoModal open={modalOpen} onClose={() => setModalOpen(false)} title="New tournament" description="Configure the match entry fee, prize pool and start time.">
        <form id="ludo-modal-form" className="space-y-4" onSubmit={onCreateTournament}>
          <FormField id="tournament-name" label="Tournament name">
            <input
              id="tournament-name"
              {...register('name', { required: true })}
              className="w-full rounded-md border border-neutral-200 px-3 py-2 text-sm text-neutral-700 focus:border-brand-primary focus:outline-none focus:ring-2 focus:ring-brand-primary/40 dark:border-neutral-700 dark:bg-neutral-950 dark:text-neutral-100"
              placeholder="Weekend Clash"
            />
          </FormField>
          <FormField id="entry-fee" label="Entry fee" hint="Set a fair entry fee for target region">
            <input
              id="entry-fee"
              type="number"
              {...register('entryFee', { valueAsNumber: true })}
              className="w-full rounded-md border border-neutral-200 px-3 py-2 text-sm text-neutral-700 focus:border-brand-primary focus:outline-none focus:ring-2 focus:ring-brand-primary/40 dark:border-neutral-700 dark:bg-neutral-950 dark:text-neutral-100"
              placeholder="200"
            />
          </FormField>
          <FormField id="reward" label="Winner reward">
            <input
              id="reward"
              {...register('reward')}
              className="w-full rounded-md border border-neutral-200 px-3 py-2 text-sm text-neutral-700 focus:border-brand-primary focus:outline-none focus:ring-2 focus:ring-brand-primary/40 dark:border-neutral-700 dark:bg-neutral-950 dark:text-neutral-100"
              placeholder="KES 5,000"
            />
          </FormField>
          <FormField id="startAt" label="Kick-off time">
            <input
              id="startAt"
              type="datetime-local"
              {...register('startAt')}
              className="w-full rounded-md border border-neutral-200 px-3 py-2 text-sm text-neutral-700 focus:border-brand-primary focus:outline-none focus:ring-2 focus:ring-brand-primary/40 dark:border-neutral-700 dark:bg-neutral-950 dark:text-neutral-100"
            />
          </FormField>
        </form>
      </LudoModal>
    </div>
  );
}
