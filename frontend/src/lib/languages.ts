// Languages offered by the in-page translator. `code` is stored in localStorage,
// `native` is shown in the UI, and `name` (English name) is what we send to the
// translation model. The list is broad but not exhaustive — the selector also
// lets the user type ANY language, so this is just the convenient shortlist.

export interface Language {
  code: string;
  native: string;
  name: string;
  rtl?: boolean;
}

export const DEFAULT_LANG: Language = { code: "en", native: "English", name: "English" };

export const LANGUAGES: Language[] = [
  DEFAULT_LANG,
  { code: "es", native: "Español", name: "Spanish" },
  { code: "fr", native: "Français", name: "French" },
  { code: "de", native: "Deutsch", name: "German" },
  { code: "it", native: "Italiano", name: "Italian" },
  { code: "pt", native: "Português", name: "Portuguese" },
  { code: "ro", native: "Română", name: "Romanian" },
  { code: "nl", native: "Nederlands", name: "Dutch" },
  { code: "pl", native: "Polski", name: "Polish" },
  { code: "sv", native: "Svenska", name: "Swedish" },
  { code: "da", native: "Dansk", name: "Danish" },
  { code: "no", native: "Norsk", name: "Norwegian" },
  { code: "fi", native: "Suomi", name: "Finnish" },
  { code: "cs", native: "Čeština", name: "Czech" },
  { code: "el", native: "Ελληνικά", name: "Greek" },
  { code: "hu", native: "Magyar", name: "Hungarian" },
  { code: "tr", native: "Türkçe", name: "Turkish" },
  { code: "ru", native: "Русский", name: "Russian" },
  { code: "uk", native: "Українська", name: "Ukrainian" },
  { code: "ar", native: "العربية", name: "Arabic", rtl: true },
  { code: "he", native: "עברית", name: "Hebrew", rtl: true },
  { code: "fa", native: "فارسی", name: "Persian", rtl: true },
  { code: "hi", native: "हिन्दी", name: "Hindi" },
  { code: "bn", native: "বাংলা", name: "Bengali" },
  { code: "ur", native: "اردو", name: "Urdu", rtl: true },
  { code: "zh", native: "中文", name: "Chinese (Simplified)" },
  { code: "zh-TW", native: "繁體中文", name: "Chinese (Traditional)" },
  { code: "ja", native: "日本語", name: "Japanese" },
  { code: "ko", native: "한국어", name: "Korean" },
  { code: "vi", native: "Tiếng Việt", name: "Vietnamese" },
  { code: "th", native: "ไทย", name: "Thai" },
  { code: "id", native: "Bahasa Indonesia", name: "Indonesian" },
  { code: "ms", native: "Bahasa Melayu", name: "Malay" },
  { code: "sw", native: "Kiswahili", name: "Swahili" },
];

const RTL_CODES = new Set(LANGUAGES.filter((l) => l.rtl).map((l) => l.code));

export function isRtl(code: string): boolean {
  return RTL_CODES.has(code);
}

export function findLanguage(code: string): Language | undefined {
  return LANGUAGES.find((l) => l.code === code);
}
