import type { Metadata } from "next";
import { Plus_Jakarta_Sans, Geist_Mono } from "next/font/google";
import Link from "next/link";
import Chatbot from "@/components/Chatbot";
import ReportIssue from "@/components/ReportIssue";
import ThemeToggle from "@/components/ThemeToggle";
import AuthNav from "@/components/AuthNav";
import AuthGuard from "@/components/AuthGuard";
import PrimaryNav from "@/components/PrimaryNav";
import Logo from "@/components/Logo";
import TranslationProvider from "@/components/TranslationProvider";
import LanguageSelector from "@/components/LanguageSelector";
import "./globals.css";

// Runs before paint to apply the saved/system theme and avoid a flash of the
// wrong theme (FOUC). Plain DOM API so it is independent of any framework.
const themeInitScript = `(function(){try{var t=localStorage.getItem('theme');var d=t?t==='dark':window.matchMedia('(prefers-color-scheme: dark)').matches;if(d)document.documentElement.classList.add('dark');}catch(e){}})();`;

const jakarta = Plus_Jakarta_Sans({
  variable: "--font-jakarta",
  subsets: ["latin"],
  weight: ["400", "500", "600", "700", "800"],
});
const geistMono = Geist_Mono({ variable: "--font-geist-mono", subsets: ["latin"] });

export const metadata: Metadata = {
  title: "TrainingIT — Level up your IT career",
  description:
    "Professional IT training. Instructor-led courses, hands-on labs and 1:1 mentoring for individuals and corporate teams.",
};

export default function RootLayout({
  children,
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html
      lang="en"
      suppressHydrationWarning
      className={`${jakarta.variable} ${geistMono.variable} h-full overflow-x-clip antialiased`}
    >
      <body className="min-h-full flex flex-col overflow-x-clip bg-canvas text-ink transition-colors dark:bg-zinc-950 dark:text-zinc-100">
        <script dangerouslySetInnerHTML={{ __html: themeInitScript }} />
        <TranslationProvider>
        <header className="sticky top-0 z-40 border-b border-black/5 bg-canvas/80 backdrop-blur-md transition-colors dark:border-white/10 dark:bg-zinc-950/80">
          <nav className="mx-auto flex max-w-6xl items-center gap-6 px-6 py-3.5">
            <Link href="/" className="flex items-center dark:hidden" aria-label="TrainingIT home">
              <Logo size={30} tone="dark" />
            </Link>
            <Link href="/" className="hidden items-center dark:flex" aria-label="TrainingIT home">
              <Logo size={30} tone="light" />
            </Link>
            <PrimaryNav />
            <div className="ml-auto flex items-center gap-3">
              <LanguageSelector />
              <ThemeToggle />
              <AuthNav />
            </div>
          </nav>
        </header>
        <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-10">
          <AuthGuard>{children}</AuthGuard>
        </main>
        <footer className="border-t border-white/10 bg-zinc-950 text-zinc-400">
          <div className="mx-auto grid max-w-6xl gap-8 px-6 py-12 sm:grid-cols-2 lg:grid-cols-4">
            <div className="sm:col-span-2 lg:col-span-2">
              <Logo size={30} tone="light" />
              <p className="mt-4 max-w-sm text-sm leading-relaxed text-zinc-400">
                Professional IT training for individuals and corporate teams —
                instructor-led courses, hands-on labs and 1:1 mentoring that turn
                skills into careers.
              </p>
            </div>
            <div>
              <p className="text-sm font-semibold text-white">Platform</p>
              <ul className="mt-3 space-y-2 text-sm">
                <li><Link href="/" className="hover:text-white">Courses</Link></li>
                <li><Link href="/my-courses" className="hover:text-white">My courses</Link></li>
                <li><Link href="/my-sessions" className="hover:text-white">My sessions</Link></li>
              </ul>
            </div>
            <div>
              <p className="text-sm font-semibold text-white">Account</p>
              <ul className="mt-3 space-y-2 text-sm">
                <li><Link href="/login" className="hover:text-white">Log in</Link></li>
                <li><Link href="/register" className="hover:text-white">Create account</Link></li>
              </ul>
            </div>
          </div>
          <div className="border-t border-white/10">
            <div className="mx-auto flex max-w-6xl flex-col items-center justify-between gap-2 px-6 py-5 text-xs text-zinc-500 sm:flex-row">
              <p>© {new Date().getFullYear()} TrainingIT. All rights reserved.</p>
              <p>Level up your IT career.</p>
            </div>
          </div>
        </footer>
        <Chatbot />
        <ReportIssue />
        </TranslationProvider>
      </body>
    </html>
  );
}
