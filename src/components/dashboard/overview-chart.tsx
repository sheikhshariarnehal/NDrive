"use client";

import { useEffect, useState } from "react";
import { Bar, BarChart, ResponsiveContainer, XAxis, YAxis, Tooltip } from "recharts";

type OverviewChartPoint = {
  date: string;
  total: number;
};

type OverviewChartProps = {
  data: OverviewChartPoint[];
};

export default function OverviewChart({ data }: OverviewChartProps) {
  const [isMobile, setIsMobile] = useState(false);

  useEffect(() => {
    const mediaQuery = window.matchMedia("(max-width: 640px)");

    const syncBreakpoint = (event: MediaQueryList | MediaQueryListEvent) => {
      setIsMobile(event.matches);
    };

    syncBreakpoint(mediaQuery);
    mediaQuery.addEventListener("change", syncBreakpoint);

    return () => {
      mediaQuery.removeEventListener("change", syncBreakpoint);
    };
  }, []);

  const chartData = data.map((item) => ({
    name: item.date.slice(5),
    total: item.total,
  }));

  return (
    <div className="h-[220px] w-full sm:h-[280px] lg:h-[350px]">
      <ResponsiveContainer width="100%" height="100%">
        <BarChart
          data={chartData}
          margin={{ top: 8, right: isMobile ? 8 : 16, left: isMobile ? -14 : 0, bottom: 0 }}
          barCategoryGap={isMobile ? "28%" : "20%"}
        >
        <XAxis
          dataKey="name"
          stroke="#888888"
          fontSize={12}
          tickLine={false}
          axisLine={false}
          interval="preserveStartEnd"
          minTickGap={isMobile ? 22 : 14}
          tickMargin={8}
        />
        <YAxis
          stroke="#888888"
          fontSize={12}
          tickLine={false}
          axisLine={false}
          tickFormatter={(value) => `${value}`}
          width={isMobile ? 34 : 42}
        />
        <Tooltip
           contentStyle={{ backgroundColor: "rgba(0,0,0,0.8)", border: "none", borderRadius: "8px", color: "white" }}
           itemStyle={{ color: "white" }}
        />
        <Bar
          dataKey="total"
          fill="currentColor"
          radius={[4, 4, 0, 0]}
          className="fill-primary"
          maxBarSize={isMobile ? 14 : 24}
        />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
