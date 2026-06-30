import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // Pin the workspace root to this folder. There is an unrelated package-lock.json
  // at the repo root (for a `docx` tool), which otherwise makes Turbopack infer
  // the wrong root and emit a multiple-lockfiles warning.
  turbopack: {
    root: __dirname,
  },
};

export default nextConfig;
