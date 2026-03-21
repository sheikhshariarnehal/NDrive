"use client";

import {
  BarChart3,
  Files,
  Cloud,
  LayoutDashboard,
  LogOut,
  Settings,
  Share2,
  Users,
  ShieldAlert,
  DatabaseZap
} from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";

import {
  Sidebar,
  SidebarContent,
  SidebarFooter,
  SidebarHeader,
  SidebarRail,
  SidebarMenu,
  SidebarMenuItem,
  SidebarMenuButton,
  SidebarGroup,
  SidebarGroupLabel,
  SidebarGroupContent,
} from "@/components/ui/sidebar";

const NAV_ITEMS = [
  {
    title: "Overview",
    url: "/",
    icon: LayoutDashboard,
  },
  {
    title: "Users",
    url: "/users",
    icon: Users,
  },
  {
    title: "Files",
    url: "/files",
    icon: Files,
  },
  {
    title: "Storage",
    url: "/storage",
    icon: DatabaseZap,
  },
  {
    title: "Shares",
    url: "/shares",
    icon: Share2,
  },
  {
    title: "Metrics",
    url: "/system",
    icon: BarChart3,
  },
];

export function AppSidebar() {
  const pathname = usePathname();

  return (
    <Sidebar collapsible="icon" className="border-r border-border/50 bg-card">
      <SidebarHeader className="flex items-center justify-center py-4">
        <div className="flex w-full items-center gap-2.5 px-2 font-bold text-base tracking-tight text-foreground">
          <div className="flex items-center justify-center p-1.5 rounded-md bg-primary/10">
            <Cloud className="w-5 h-5 text-primary" />
          </div>
          <span className="truncate group-data-[collapsible=icon]:hidden">CloudVault</span>
        </div>
      </SidebarHeader>
      <SidebarContent className="px-2.5">
        <SidebarGroup>
          <SidebarGroupLabel className="mb-1.5 px-2 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">Platform</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu className="space-y-1">
              {NAV_ITEMS.map((item) => {
                const isActive = pathname === item.url;
                return (
                  <SidebarMenuItem key={item.title}>
                    <SidebarMenuButton
                      render={<Link href={item.url} />}
                      isActive={isActive}
                      tooltip={item.title}
                      className="h-9 gap-2.5 rounded-md px-3 transition-all"
                    >
                      <item.icon className="w-4 h-4 shrink-0" />
                      <span className="truncate">{item.title}</span>
                    </SidebarMenuButton>
                  </SidebarMenuItem>
                );
              })}
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>

        <SidebarGroup className="mt-3">
          <SidebarGroupLabel className="mb-1.5 px-2 text-[10px] font-semibold uppercase tracking-wider text-muted-foreground">System</SidebarGroupLabel>
          <SidebarGroupContent>
            <SidebarMenu className="space-y-1">
                <SidebarMenuItem>
                  <SidebarMenuButton
                    render={<Link href="/settings" />}
                    tooltip="Access Control"
                    className="h-9 gap-2.5 rounded-md px-3 text-muted-foreground transition-all hover:bg-muted/80 hover:text-foreground"
                  >
                    <ShieldAlert className="w-4 h-4 shrink-0" />
                    <span className="truncate flex-1">Access Control</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
                <SidebarMenuItem>
                  <SidebarMenuButton
                    render={<Link href="/settings" />}
                    tooltip="Configuration"
                    className="h-9 gap-2.5 rounded-md px-3 text-muted-foreground transition-all hover:bg-muted/80 hover:text-foreground"
                  >
                    <Settings className="w-4 h-4 shrink-0" />
                    <span className="truncate flex-1">Configuration</span>
                  </SidebarMenuButton>
                </SidebarMenuItem>
            </SidebarMenu>
          </SidebarGroupContent>
        </SidebarGroup>
      </SidebarContent>
      <SidebarFooter className="mt-auto shrink-0 p-3 pt-2 pb-[max(1.25rem,env(safe-area-inset-bottom))] md:pb-10">
        <SidebarMenu>
          <SidebarMenuItem>
            <SidebarMenuButton render={<Link href="/logout" />} tooltip="Logout" className="h-9 gap-2.5 rounded-md text-muted-foreground transition-all hover:bg-destructive/10 hover:text-destructive">
                <LogOut className="w-4 h-4 shrink-0" />
                <span className="font-medium">Sign Out</span>
            </SidebarMenuButton>
          </SidebarMenuItem>
        </SidebarMenu>
      </SidebarFooter>
      <SidebarRail />
    </Sidebar>
  );
}
