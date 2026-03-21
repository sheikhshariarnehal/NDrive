"use client";

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Users, Files, Share2, HardDrive } from "lucide-react";
import { formatBytes } from "@/lib/admin/format";
import OverviewChart from "./overview-chart";
import { RecentActivity } from "./recent-activity";
import type { OverviewStats, TrendPoint } from "@/lib/admin/queries";

type DashboardClientProps = {
  stats: OverviewStats;
  trend: TrendPoint[];
  recentUploads: Array<{
    id: string;
    name: string;
    sizeBytes: number;
    userName: string;
    avatarUrl: string | null;
    createdAt: string;
  }>;
};

export function DashboardClient({ stats, trend, recentUploads }: DashboardClientProps) {
  const statCards = [
    {
      title: "Total Users",
      value: stats.totalUsers.toLocaleString(),
      icon: Users,
      description: "Registered users",
    },
    {
      title: "Total Files",
      value: stats.totalFiles.toLocaleString(),
      icon: Files,
      description: "Files uploaded",
    },
    {
      title: "Total Storage",
      value: formatBytes(stats.totalStorageBytes),
      icon: HardDrive,
      description: "Storage used",
    },
    {
      title: "Active Links",
      value: stats.activeLinks.toLocaleString(),
      icon: Share2,
      description: "Shared links",
    },
  ];

  return (
    <div className="space-y-4">
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        {statCards.map((card) => {
          const Icon = card.icon;
          return (
            <Card key={card.title}>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">
                  {card.title}
                </CardTitle>
                <Icon className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{card.value}</div>
                <p className="text-xs text-muted-foreground">
                  {card.description}
                </p>
              </CardContent>
            </Card>
          );
        })}
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-7">
        <Card className="col-span-4">
          <CardHeader>
            <CardTitle>Storage Growth</CardTitle>
            <CardDescription>Storage usage over time</CardDescription>
          </CardHeader>
          <CardContent className="pl-2">
            <OverviewChart data={trend} />
          </CardContent>
        </Card>

        <Card className="col-span-3">
          <CardHeader>
            <CardTitle>Recent Activity</CardTitle>
            <CardDescription>Latest file uploads</CardDescription>
          </CardHeader>
          <CardContent>
            <RecentActivity uploads={recentUploads} />
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
