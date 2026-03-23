"use client";

import {
  createContext,
  useContext,
  useEffect,
  useState,
  useRef,
  type ReactNode,
} from "react";
import { createClient } from "@/lib/supabase/client";
import type { User } from "@supabase/supabase-js";
import { getGuestSessionId } from "@/lib/guest-session";

interface AuthContextType {
  user: User | null;
  guestSessionId: string | null;
  isGuest: boolean;
  isLoading: boolean;
  isTelegramConnected: boolean;
  isTelegramStatusLoading: boolean;
  telegramPhone: string | null;
  signOut: () => Promise<void>;
  refreshTelegramStatus: () => void;
}

const AuthContext = createContext<AuthContextType>({
  user: null,
  guestSessionId: null,
  isGuest: false,
  isLoading: true,
  isTelegramConnected: false,
  isTelegramStatusLoading: true,
  telegramPhone: null,
  signOut: async () => {},
  refreshTelegramStatus: () => {},
});

/**
 * Ensures the authenticated user has a corresponding public.users profile.
 * The DB trigger handles this on signup, but this is a safety net for:
 * - Users created before the trigger existed
 * - Edge cases where the trigger might fail
 */
async function ensureUserProfile(supabase: ReturnType<typeof createClient>, user: User) {
  try {
    const displayName =
      user.user_metadata?.display_name ||
      user.user_metadata?.full_name ||
      user.email?.split("@")[0] ||
      "User";

    const avatarUrl =
      user.user_metadata?.avatar_url ||
      user.user_metadata?.picture ||
      null;

    // Upsert keeps profile rows in sync for users created before/after trigger,
    // and refreshes missing avatar/name data from the auth provider metadata.
    const { error } = await supabase.from("users").upsert(
      {
        id: user.id,
        email: user.email,
        display_name: displayName,
        avatar_url: avatarUrl,
      },
      { onConflict: "id" }
    );

    if (error) {
      console.warn("ensureUserProfile: upsert failed", error.message);
    }
  } catch {
    // Profile sync failed silently — don't block auth flow
    console.warn("ensureUserProfile: could not sync profile");
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [guestSessionId, setGuestSessionId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isTelegramStatusLoading, setIsTelegramStatusLoading] = useState(true);
  const [isTelegramConnected, setIsTelegramConnected] = useState(false);
  const [telegramPhone, setTelegramPhone] = useState<string | null>(null);
  const supabase = createClient();
  const statusReqRef = useRef<Promise<void> | null>(null);

  const fetchTelegramStatus = async () => {
    if (statusReqRef.current) return statusReqRef.current;

    setIsTelegramStatusLoading(true);
    statusReqRef.current = (async () => {
      try {
        const res = await fetch("/api/telegram/status");
        if (res.ok) {
          const data = await res.json();
          setIsTelegramConnected(!!data.connected);
          setTelegramPhone(data.phone || null);
        } else {
          setIsTelegramConnected(false);
          setTelegramPhone(null);
        }
      } catch {
        // Non-fatal — status check failure doesn't block auth
        setIsTelegramConnected(false);
        setTelegramPhone(null);
      } finally {
        setIsTelegramStatusLoading(false);
        statusReqRef.current = null;
      }
    })();
    return statusReqRef.current;
  };

  const refreshTelegramStatus = () => {
    fetchTelegramStatus();
  };

  useEffect(() => {
    const restoreSession = async () => {
      try {
        // First try local session (fast, from cookies)
        const {
          data: { session },
        } = await supabase.auth.getSession();

        let currentUser = session?.user ?? null;

        // If no local session, try getUser() which validates server-side
        // and refreshes expired tokens using the refresh token cookie
        if (!currentUser) {
          const {
            data: { user: refreshedUser },
          } = await supabase.auth.getUser();
          currentUser = refreshedUser;
        }

        setUser(currentUser);

        if (currentUser) {
          // Ensure the user has a public.users profile (fire-and-forget — don't block auth loading)
          ensureUserProfile(supabase, currentUser);
          // Fetch Telegram connection status (fire-and-forget)
          fetchTelegramStatus();
        } else {
          // Create guest session for unauthenticated users
          setGuestSessionId(getGuestSessionId());
          setIsTelegramStatusLoading(false);
        }
      } catch (error) {
        console.error("Auth session error:", error);
        // Fall back to guest mode on error
        setGuestSessionId(getGuestSessionId());
      } finally {
        setIsLoading(false);
      }
    };

    restoreSession();

    const {
      data: { subscription },
    } = supabase.auth.onAuthStateChange(async (_event, session) => {
      const currentUser = session?.user ?? null;
      setUser(currentUser);

      if (currentUser) {
        setGuestSessionId(null);
        // Ensure profile on any auth state change (fire-and-forget — don't block renders)
        ensureUserProfile(supabase, currentUser);
        fetchTelegramStatus();
      } else {
        setGuestSessionId(getGuestSessionId());
        setIsTelegramStatusLoading(false);
      }
    });

    return () => subscription.unsubscribe();
  }, [supabase.auth]);

  const signOut = async () => {
    await supabase.auth.signOut();
    setUser(null);
    setIsTelegramConnected(false);
    setIsTelegramStatusLoading(false);
    setTelegramPhone(null);
  };

  const isGuest = !user && !!guestSessionId;

  return (
    <AuthContext.Provider
      value={{ user, guestSessionId, isGuest, isLoading, isTelegramConnected, isTelegramStatusLoading, telegramPhone, signOut, refreshTelegramStatus }}
    >
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
