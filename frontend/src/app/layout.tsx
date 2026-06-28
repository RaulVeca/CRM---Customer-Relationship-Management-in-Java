import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import Chatbot from "@/components/Chatbot";
import ThemeToggle from "@/components/ThemeToggle";
import AuthNav from "@/components/AuthNav";
import AuthGuard from "@/components/AuthGuard";
import PrimaryNav from "@/components/PrimaryNav";
import "./globals.css";

// Runs before paint to apply the saved/system theme and avoid a flash of the
// wrong theme (FOUC). Plain DOM API so it is independent of any framework.
const themeInitScript = `(function(){try{var t=localStorage.getItem('theme');var d=t?t==='dark':window.matchMedia('(prefers-color-scheme: dark)').matches;if(d)document.documentElement.classList.add('dark');}catch(e){}})();`;

const geistSans = Geist({ variable: "--font-geist-sans", subsets: ["latin"] });
const geistMono = Geist({ variable: "--font-geist-mono", subsets: ["latin"] });

export const metadata: Metadata = {
  title: "TrainingIT CRM",
  description: "CRM & course marketplace for an IT training company",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html
      lang="en"
      suppressHydrationWarning
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col bg-slate-50 text-slate-900 transition-colors dark:bg-slate-950 dark:text-slate-100">
        <script dangerouslySetInnerHTML={{ __html: themeInitScript }} />
        <header className="border-b border-slate-200 bg-white transition-colors dark:border-slate-800 dark:bg-slate-900">
          <nav className="mx-auto flex max-w-6xl items-center gap-6 px-6 py-4">
            <Link href="/" className="font-bold text-indigo-600 dark:text-indigo-400">
              TrainingIT
            </Link>
            <PrimaryNav />
            <div className="ml-auto flex items-center gap-4">
              <ThemeToggle />
              <AuthNav />
            </div>
          </nav>
        </header>
        <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-8">
          <AuthGuard>{children}</AuthGuard>
        </main>
        <footer className="border-t border-slate-200 bg-white py-4 text-center text-xs text-slate-400 transition-colors dark:border-slate-800 dark:bg-slate-900 dark:text-slate-500">
          TrainingIT CRM — bachelor thesis project
        </footer>
        <Chatbot />
      </body>
    </html>
  );
}
