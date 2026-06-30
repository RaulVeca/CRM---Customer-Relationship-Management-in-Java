import { redirect } from "next/navigation";

/**
 * The admin area no longer has a Dashboard window — landing on /admin sends the
 * admin to the first available tab.
 */
export default function AdminIndexPage() {
  redirect("/admin/contacts");
}
