"use client";

import { useState } from "react";
import { EyeIcon, EyeOffIcon } from "@/components/icons";

/**
 * A password field: the characters show as bullets by default, with an eye icon
 * on the right that toggles between hiding and revealing the text so a contact
 * can double-check what they typed. Used on both the login and register forms.
 */
export default function PasswordInput({
  value,
  onChange,
  placeholder,
  autoFocus,
  inputClassName,
}: {
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  autoFocus?: boolean;
  /** Class list for the underlying input (so each form keeps its own styling). */
  inputClassName: string;
}) {
  const [show, setShow] = useState(false);
  return (
    <div className="relative">
      <input
        type={show ? "text" : "password"}
        value={value}
        autoFocus={autoFocus}
        onChange={(e) => onChange(e.target.value)}
        placeholder={placeholder}
        // Extra right padding leaves room for the eye toggle.
        className={`${inputClassName} pr-10`}
      />
      <button
        type="button"
        onClick={() => setShow((s) => !s)}
        aria-label={show ? "Hide password" : "Show password"}
        className="absolute inset-y-0 right-0 flex items-center pr-3 text-zinc-400 transition-colors hover:text-zinc-600 dark:hover:text-zinc-200"
      >
        {show ? <EyeOffIcon /> : <EyeIcon />}
      </button>
    </div>
  );
}
