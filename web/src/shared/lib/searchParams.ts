import { useCallback, useMemo, useSyncExternalStore } from 'react';

const NAVIGATION_EVENT = 'app:navigate';

function subscribe(onChange: () => void): () => void {
  window.addEventListener('popstate', onChange);
  window.addEventListener(NAVIGATION_EVENT, onChange);
  return () => {
    window.removeEventListener('popstate', onChange);
    window.removeEventListener(NAVIGATION_EVENT, onChange);
  };
}

function currentSearch(): string {
  return window.location.search;
}

/** Reads the URL search params and pushes a new history entry when they change. */
export function useSearchParams(): [URLSearchParams, (next: URLSearchParams) => void] {
  const search = useSyncExternalStore(subscribe, currentSearch, () => '');
  const params = useMemo(() => new URLSearchParams(search), [search]);
  const navigate = useCallback((next: URLSearchParams) => {
    const query = next.toString();
    window.history.pushState(null, '', query === '' ? window.location.pathname : `?${query}`);
    window.dispatchEvent(new Event(NAVIGATION_EVENT));
  }, []);
  return [params, navigate];
}
