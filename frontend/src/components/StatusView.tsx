import { AlertCircle, LoaderCircle, SearchX } from 'lucide-react';

export function LoadingView({ label = 'Finding the best stays…' }: { label?: string }) {
  return <div className="status-view"><LoaderCircle className="spin" /><p>{label}</p></div>;
}

export function ErrorView({ message }: { message: string }) {
  return <div className="status-view error"><AlertCircle /><p>{message}</p></div>;
}

export function EmptyView() {
  return (
    <div className="status-view empty">
      <SearchX />
      <h3>No stays matched those filters</h3>
      <p>Try a different city, budget or remove one of the amenities.</p>
    </div>
  );
}
