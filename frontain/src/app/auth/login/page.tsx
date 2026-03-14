"use client";

import { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
import { createClient } from "@/lib/supabase/client";
import { getGuestSessionId } from "@/lib/guest-session";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { GoogleAuthButton } from "@/components/auth/google-auth-button";
import { Mail, Eye, EyeOff, Send, Cloud, Shield } from "lucide-react";
import NextImage from "next/image";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isMagicLink, setIsMagicLink] = useState(false);
  const router = useRouter();
  const supabase = createClient();

  // Show error if redirected back from OAuth callback failure
  useEffect(() => {
    const params = new URLSearchParams(window.location.search);
    if (params.get("error")) {
      setError("Sign-in failed. Please try again.");
    }
  }, []);

  const handleEmailLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError("");
    setIsLoading(true);

    try {
      if (isMagicLink) {
        const { error } = await supabase.auth.signInWithOtp({ email });
        if (error) throw error;
        setError("Check your email for the magic link!");
        return;
      }

      const { error } = await supabase.auth.signInWithPassword({
        email,
        password,
      });
      if (error) throw error;
      router.push("/drive");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Login failed");
    } finally {
      setIsLoading(false);
    }
  };

  const handleGuestMode = () => {
    getGuestSessionId();
    router.push("/drive");
  };

  return (
    <div className="min-h-dvh flex bg-background sm:bg-muted/40 text-foreground">
      {/* Left Column - Branding (Hidden on mobile) */}
      <div className="hidden lg:flex lg:w-1/2 flex-col justify-between p-10 xl:p-12 bg-gray-900 text-white relative overflow-hidden">
        {/* Abstract background shapes */}
        <div className="absolute top-[-10%] right-[-5%] w-96 h-96 bg-blue-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob"></div>
        <div className="absolute bottom-[-10%] left-[-10%] w-96 h-96 bg-purple-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-2000"></div>
        <div className="absolute top-[20%] left-[20%] w-72 h-72 bg-indigo-500 rounded-full mix-blend-multiply filter blur-3xl opacity-20 animate-blob animation-delay-4000"></div>
        
        <div className="relative z-10 flex items-center gap-3">
          <div className="w-10 h-10 bg-white rounded-xl flex items-center justify-center shadow-lg shadow-white/10">
            <NextImage src="/logo.webp" alt="NDrive" width={24} height={24} />
          </div>
          <div>
            <span className="text-2xl font-bold tracking-tight block">NDrive</span>
          </div>
        </div>

        <div className="relative z-10 space-y-5 max-w-lg">
          <h2 className="text-4xl font-semibold leading-tight tracking-tight">
            Next generation cloud storage over Telegram.
          </h2>
          <p className="text-gray-400 text-lg">
            Store unlimited files for free. Secure, fast, and easy to use. Powered by MTProto backend and React frontend.
          </p>
          
          <div className="grid grid-cols-2 gap-4 pt-6">
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-lg bg-gray-800/50 flex flex-col items-center justify-center border border-gray-700/50">
                <Send className="w-5 h-5 text-blue-400" />
              </div>
              <h3 className="font-medium text-gray-200">Telegram Storage</h3>
              <p className="text-sm text-gray-500">Files stored reliably in Telegram servers</p>
            </div>
            <div className="space-y-2">
              <div className="w-10 h-10 rounded-lg bg-gray-800/50 flex flex-col items-center justify-center border border-gray-700/50">
                <Shield className="w-5 h-5 text-emerald-400" />
              </div>
              <h3 className="font-medium text-gray-200">End-to-End Secure</h3>
              <p className="text-sm text-gray-500">Your files are private and encrypted</p>
            </div>
          </div>
        </div>

        <div className="relative z-10 text-sm text-gray-500">
          &copy; {new Date().getFullYear()} NDrive by <a href="https://ntechbd.app" target="_blank" rel="noopener noreferrer" className="font-semibold text-gray-300 hover:text-blue-300 transition-colors underline decoration-blue-400/50 hover:decoration-blue-300">Ntechbd Solutions</a>. All rights reserved.
        </div>
      </div>

      {/* Right Column - Form */}
      <div className="flex-1 flex flex-col justify-center py-6 px-4 sm:py-8 sm:px-6 lg:px-10 xl:px-12 bg-background sm:bg-transparent lg:bg-background relative overflow-y-auto">
        <div className="mx-auto w-full max-w-sm sm:max-w-md lg:max-w-[520px] xl:max-w-[560px] space-y-6 sm:bg-card sm:shadow-xl sm:shadow-black/5 sm:border sm:border-border sm:rounded-[2rem] sm:p-8 lg:bg-transparent lg:shadow-none lg:border-0 lg:rounded-none lg:p-0 my-auto">
          
          {/* Mobile/Tablet Logo */}
          <div className="flex lg:hidden items-center justify-center gap-2 mb-2 sm:mb-6">
            <div className="w-10 h-10 bg-gray-900 rounded-xl flex items-center justify-center shadow-md">
              <NextImage src="/logo.webp" alt="NDrive" width={24} height={24} />
            </div>
          </div>

          <div className="space-y-2 text-center lg:text-left">
            <h1 className="text-2xl font-semibold tracking-tight text-foreground">
              Welcome back
            </h1>
            <p className="text-sm text-muted-foreground">
              Your files are securely stored on your personal Telegram account
            </p>
          </div>

          <div className="space-y-6">
            <GoogleAuthButton mode="login" />

            <div className="relative">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-border"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="bg-background px-4 text-muted-foreground text-xs uppercase tracking-wider font-medium">
                  Or continue with email
                </span>
              </div>
            </div>

            {error && (
              <div className={`p-3 rounded-xl text-sm border font-medium ${
                error.toLowerCase().includes("check")
                  ? "bg-emerald-50 border-emerald-200 text-emerald-700"
                  : "bg-red-50 border-red-200 text-red-600"
              }`}>
                {error}
              </div>
            )}

            <form onSubmit={handleEmailLogin} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email" className="text-sm font-medium text-foreground">Email address</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="name@example.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                  autoComplete="email"
                  className="h-11 px-4 shadow-sm border-input focus:border-ring focus:ring-ring transition-colors"
                />
              </div>

              {!isMagicLink && (
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <Label htmlFor="password" className="text-sm font-medium text-foreground">Password</Label>
                    <Link
                      href="/auth/forgot-password"
                      className="text-sm font-medium text-muted-foreground hover:text-foreground transition-colors"
                    >
                      Forgot password?
                    </Link>
                  </div>
                  <div className="relative">
                    <Input
                      id="password"
                      type={showPassword ? "text" : "password"}
                      placeholder="••••••••"
                      value={password}
                      onChange={(e) => setPassword(e.target.value)}
                      required
                      autoComplete="current-password"
                      className="h-11 px-4 pr-11 shadow-sm border-input focus:border-ring focus:ring-ring transition-colors"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground p-1.5 rounded-md hover:bg-accent transition-colors"
                      tabIndex={-1}
                    >
                      {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                    </button>
                  </div>
                </div>
              )}

              <Button
                type="submit"
                className="w-full h-11 bg-primary hover:bg-primary/90 text-primary-foreground font-medium transition-all shadow-sm active:scale-[0.98]"
                disabled={isLoading}
              >
                {isLoading ? "Signing in..." : isMagicLink ? "Send Magic Link" : "Sign in"}
              </Button>
            </form>

            <div className="flex flex-col gap-3">
              <Button
                type="button"
                variant="outline"
                className="w-full h-11 border-border text-muted-foreground hover:bg-accent hover:text-accent-foreground font-medium transition-colors"
                onClick={handleGuestMode}
              >
                Continue as Guest
              </Button>
            </div>
          </div>
          
          <p className="text-center text-sm text-muted-foreground mt-8">
            Don&apos;t have an account?{" "}
            <Link href="/auth/signup" className="font-semibold text-foreground hover:underline">
              Sign up
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
