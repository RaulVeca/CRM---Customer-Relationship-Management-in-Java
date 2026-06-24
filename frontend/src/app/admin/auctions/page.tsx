"use client";

import { useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { Auction, Bid, PublicCompany } from "@/lib/types";

function money(n: number | null) {
  if (n == null) return "—";
  return new Intl.NumberFormat("ro-RO", { style: "currency", currency: "RON", maximumFractionDigits: 0 }).format(n);
}

export default function AuctionsPage() {
  const [auctions, setAuctions] = useState<Auction[]>([]);
  const [companies, setCompanies] = useState<PublicCompany[]>([]);
  const [selected, setSelected] = useState<Auction | null>(null);
  const [bids, setBids] = useState<Bid[]>([]);
  const [error, setError] = useState<string | null>(null);

  const [newAuction, setNewAuction] = useState({ title: "", startingPrice: "" });
  const [bidForm, setBidForm] = useState({ companyId: "", amount: "" });

  function loadAuctions() {
    api.get<Auction[]>("/api/auctions").then(setAuctions).catch((e) => setError(e.message));
  }

  useEffect(() => {
    loadAuctions();
    api.get<PublicCompany[]>("/api/public/companies").then(setCompanies);
  }, []);

  function openAuction(a: Auction) {
    setSelected(a);
    setError(null);
    api.get<Bid[]>(`/api/auctions/${a.id}/bids`).then(setBids);
  }

  async function createAuction(e: React.FormEvent) {
    e.preventDefault();
    try {
      await api.post<Auction>("/api/auctions", {
        title: newAuction.title,
        startingPrice: Number(newAuction.startingPrice) || 0,
      });
      setNewAuction({ title: "", startingPrice: "" });
      setError(null);
      loadAuctions();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function placeBid(e: React.FormEvent) {
    e.preventDefault();
    if (!selected) return;
    try {
      await api.post<Bid>(`/api/auctions/${selected.id}/bids`, {
        companyId: Number(bidForm.companyId),
        amount: Number(bidForm.amount),
      });
      setBidForm({ companyId: "", amount: "" });
      setError(null);
      openAuction(selected);
      loadAuctions();
    } catch (err) {
      setError((err as Error).message);
    }
  }

  async function closeAuction() {
    if (!selected) return;
    const updated = await api.patch<Auction>(`/api/auctions/${selected.id}/close`);
    setSelected(updated);
    loadAuctions();
  }

  const companyName = (id: number | null) => companies.find((c) => c.id === id)?.companyName ?? `#${id}`;

  return (
    <div className="space-y-6">
      <div className="grid gap-6 lg:grid-cols-3">
        {/* Auction list */}
        <div className="space-y-3">
          <h2 className="text-lg font-semibold">Course auctions</h2>
          {auctions.map((a) => (
            <button
              key={a.id}
              onClick={() => openAuction(a)}
              className={`w-full rounded-xl border p-4 text-left shadow-sm ${selected?.id === a.id ? "border-indigo-400 bg-indigo-50" : "border-slate-200 bg-white"}`}
            >
              <div className="flex items-center justify-between">
                <span className="font-medium">{a.title}</span>
                <span className={`rounded-full px-2 py-0.5 text-xs ${a.status === "OPEN" ? "bg-emerald-100 text-emerald-700" : "bg-slate-200 text-slate-600"}`}>
                  {a.status}
                </span>
              </div>
              <p className="mt-1 text-xs text-slate-500">Starting at {money(a.startingPrice)}</p>
              {a.status === "AWARDED" && (
                <p className="mt-1 text-xs text-indigo-600">Won by {companyName(a.winnerCompanyId)} · {money(a.winningAmount)}</p>
              )}
            </button>
          ))}
          {auctions.length === 0 && <p className="text-slate-500">No auctions yet.</p>}

          <form onSubmit={createAuction} className="space-y-2 rounded-xl border border-dashed border-slate-300 p-4">
            <p className="text-sm font-medium">New auction</p>
            <input className="w-full rounded-md border border-slate-300 px-2 py-1.5 text-sm" placeholder="Title"
              value={newAuction.title} onChange={(e) => setNewAuction({ ...newAuction, title: e.target.value })} required />
            <input className="w-full rounded-md border border-slate-300 px-2 py-1.5 text-sm" placeholder="Starting price" type="number"
              value={newAuction.startingPrice} onChange={(e) => setNewAuction({ ...newAuction, startingPrice: e.target.value })} required />
            <button className="w-full rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white">Create</button>
          </form>
        </div>

        {/* Selected auction detail */}
        <div className="lg:col-span-2">
          {!selected ? (
            <p className="text-slate-500">Select an auction to view bids.</p>
          ) : (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-lg font-semibold">{selected.title}</h3>
                {selected.status === "OPEN" && (
                  <button onClick={closeAuction} className="rounded-md bg-slate-800 px-3 py-1.5 text-sm font-medium text-white">
                    Close & award
                  </button>
                )}
              </div>

              {error && <p className="rounded-md bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>}

              <div className="overflow-hidden rounded-xl border border-slate-200 bg-white shadow-sm">
                <table className="w-full text-sm">
                  <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                    <tr><th className="px-4 py-2">#</th><th className="px-4 py-2">Company</th><th className="px-4 py-2 text-right">Amount</th></tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {bids.map((b, idx) => (
                      <tr key={b.id} className={idx === 0 ? "bg-emerald-50" : ""}>
                        <td className="px-4 py-2 text-slate-400">{idx === 0 ? "🏆" : idx + 1}</td>
                        <td className="px-4 py-2 font-medium">{b.companyName ?? companyName(b.companyId)}</td>
                        <td className="px-4 py-2 text-right font-semibold">{money(b.amount)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
                {bids.length === 0 && <p className="p-4 text-slate-500">No bids yet.</p>}
              </div>

              {selected.status === "OPEN" && (
                <form onSubmit={placeBid} className="flex flex-wrap items-end gap-2 rounded-xl border border-slate-200 bg-white p-4 shadow-sm">
                  <div>
                    <label className="block text-xs text-slate-500">Company</label>
                    <select className="rounded-md border border-slate-300 px-2 py-1.5 text-sm" value={bidForm.companyId}
                      onChange={(e) => setBidForm({ ...bidForm, companyId: e.target.value })} required>
                      <option value="">Select…</option>
                      {companies.map((c) => <option key={c.id} value={c.id}>{c.companyName}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-xs text-slate-500">Amount</label>
                    <input className="w-32 rounded-md border border-slate-300 px-2 py-1.5 text-sm" type="number"
                      value={bidForm.amount} onChange={(e) => setBidForm({ ...bidForm, amount: e.target.value })} required />
                  </div>
                  <button className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white">Place bid</button>
                  <p className="w-full text-xs text-slate-400">Only companies with an opportunity in the NEGOTIATION stage may bid.</p>
                </form>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
