import { Dialog, Transition } from '@headlessui/react';
import { Fragment, ReactNode } from 'react';

export type LudoModalProps = {
  open: boolean;
  title: string;
  description?: string;
  onClose: () => void;
  children: ReactNode;
};

export function LudoModal({ open, title, description, onClose, children }: LudoModalProps) {
  return (
    <Transition show={open} as={Fragment}>
      <Dialog onClose={onClose} className="relative z-50">
        <Transition.Child
          as={Fragment}
          enter="ease-out duration-200"
          enterFrom="opacity-0"
          enterTo="opacity-100"
          leave="ease-in duration-150"
          leaveFrom="opacity-100"
          leaveTo="opacity-0"
        >
          <div className="fixed inset-0 bg-black/40" />
        </Transition.Child>
        <div className="fixed inset-0 overflow-y-auto">
          <div className="flex min-h-full items-center justify-center p-4">
            <Transition.Child
              as={Fragment}
              enter="ease-out duration-200"
              enterFrom="opacity-0 scale-95"
              enterTo="opacity-100 scale-100"
              leave="ease-in duration-150"
              leaveFrom="opacity-100 scale-100"
              leaveTo="opacity-0 scale-95"
            >
              <Dialog.Panel className="w-full max-w-lg transform overflow-hidden rounded-lg bg-white p-6 text-left align-middle shadow-xl transition-all dark:bg-neutral-900">
                <Dialog.Title className="text-lg font-semibold text-brand-primary dark:text-brand-secondary">{title}</Dialog.Title>
                {description && <Dialog.Description className="mt-1 text-sm text-neutral-600 dark:text-neutral-200">{description}</Dialog.Description>}
                <div className="mt-4 space-y-4">{children}</div>
                <div className="mt-6 flex justify-end gap-2">
                  <button
                    type="button"
                    onClick={onClose}
                    className="rounded-md border border-neutral-200 px-4 py-2 text-sm font-medium text-neutral-600 transition hover:border-brand-primary hover:text-brand-primary dark:border-neutral-700 dark:text-neutral-100"
                  >
                    Cancel
                  </button>
                  <button
                    type="submit"
                    form="ludo-modal-form"
                    className="rounded-md bg-brand-primary px-4 py-2 text-sm font-semibold text-white transition hover:bg-brand-primary/90 dark:bg-brand-secondary dark:text-brand-secondary-foreground"
                  >
                    Save
                  </button>
                </div>
              </Dialog.Panel>
            </Transition.Child>
          </div>
        </div>
      </Dialog>
    </Transition>
  );
}
