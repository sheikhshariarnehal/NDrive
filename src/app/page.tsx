import MissingEnvNotice from "@/components/admin/missing-env-notice";
import {
  getOverviewStats,
  getGrowthTrend,
  getRecentUploads,
} from "@/lib/admin/queries";
import { isMissingAdminSupabaseEnvError } from "@/lib/supabase";
import { DashboardClient } from "@/components/dashboard/dashboard-client";

export const dynamic = "force-dynamic";

export default async function Dashboard() {
  let stats;
  let trend;
  let recentUploads;

  try {
    [stats, trend, recentUploads] = await Promise.all([
      getOverviewStats(),
      getGrowthTrend(30),
      getRecentUploads(8),
    ]);
  } catch (error) {
    if (isMissingAdminSupabaseEnvError(error)) {
      return <MissingEnvNotice title="Dashboard data unavailable" />;
    }

    throw error;
  }

  return (
    <DashboardClient
      stats={stats}
      trend={trend}
      recentUploads={recentUploads}
    />
  );
}
