import type { LucideIcon } from 'lucide-react';

export function KpiCard({ icon: Icon, label, value, helper }: {
  icon: LucideIcon;
  label: string;
  value: string;
  helper: string;
}) {
  return (
    <article className="kpi-card">
      <div className="kpi-icon"><Icon size={20} /></div>
      <div>
        <p>{label}</p>
        <strong>{value}</strong>
        <span>{helper}</span>
      </div>
    </article>
  );
}
