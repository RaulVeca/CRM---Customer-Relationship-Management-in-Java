import type { Metadata } from "next";
import { Plus_Jakarta_Sans, Geist_Mono } from "next/font/google";
import Chatbot from "@/components/Chatbot";
import ReportIssue from "@/components/ReportIssue";
import SiteShell from "@/components/SiteShell";
import TranslationProvider from "@/components/TranslationProvider";
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
        <SiteShell>{children}</SiteShell>
        <Chatbot />
        <ReportIssue />
        </TranslationProvider>
      </body>
    </html>
  );
}
