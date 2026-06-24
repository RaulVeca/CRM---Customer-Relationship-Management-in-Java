import type { Metadata } from "next";
import { Geist, Geist_Mono } from "next/font/google";
import Link from "next/link";
import Chatbot from "@/components/Chatbot";
import "./globals.css";

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
      className={`${geistSans.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full flex flex-col bg-slate-50 text-slate-900">
        <header className="border-b border-slate-200 bg-white">
          <nav className="mx-auto flex max-w-6xl items-center gap-6 px-6 py-4">
            <Link href="/" className="font-bold text-indigo-600">
              TrainingIT
            </Link>
            <Link href="/" className="text-sm text-slate-600 hover:text-slate-900">
              Courses
            </Link>
            <Link href="/companies" className="text-sm text-slate-600 hover:text-slate-900">
              Companies
            </Link>
            <div className="ml-auto flex gap-4">
              <Link
                href="/admin"
                className="rounded-md bg-indigo-600 px-3 py-1.5 text-sm font-medium text-white hover:bg-indigo-700"
              >
                Admin
              </Link>
            </div>
          </nav>
        </header>
        <main className="mx-auto w-full max-w-6xl flex-1 px-6 py-8">{children}</main>
        <footer className="border-t border-slate-200 bg-white py-4 text-center text-xs text-slate-400">
          TrainingIT CRM — bachelor thesis project
        </footer>
        <Chatbot />
      </body>
    </html>
  );
}
