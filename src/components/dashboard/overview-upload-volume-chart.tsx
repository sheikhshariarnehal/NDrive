"use client";

import { Bar, CartesianGrid, ComposedChart, Line, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";

type UploadVolumePoint = {
  date: string;
  uploads: number;
  bytes: number;
};

type OverviewUploadVolumeChartProps = {
  data: UploadVolumePoint[];
};

const GB = 1024 * 1024 * 1024;

export default function OverviewUploadVolumeChart({ data }: OverviewUploadVolumeChartProps) {
  const chartData = data.map((item) => ({
    label: item.date.slice(5),
    uploads: item.uploads,
    gb: Number((item.bytes / GB).toFixed(2)),
  }));

  return (
    <div className="h-[240px] w-full sm:h-[280px] lg:h-[320px]">
      <ResponsiveContainer width="100%" height="100%">
        <ComposedChart data={chartData} margin={{ top: 8, right: 12, left: -8, bottom: 0 }}>
          <CartesianGrid strokeDasharray="3 3" stroke="rgba(136,136,136,0.2)" vertical={false} />
          <XAxis dataKey="label" tickLine={false} axisLine={false} minTickGap={18} tick={{ fontSize: 12, fill: "#888888" }} />
          <YAxis yAxisId="left" tickLine={false} axisLine={false} tick={{ fontSize: 12, fill: "#888888" }} width={36} />
          <YAxis
            yAxisId="right"
            orientation="right"
            tickLine={false}
            axisLine={false}
            tick={{ fontSize: 12, fill: "#888888" }}
            width={44}
            tickFormatter={(value) => `${value}G`}
          />
          <Tooltip
            contentStyle={{ backgroundColor: "rgba(0,0,0,0.8)", border: "none", borderRadius: "8px", color: "white" }}
            itemStyle={{ color: "white" }}
          />
          <Bar yAxisId="left" dataKey="uploads" fill="hsl(var(--chart-2))" radius={[4, 4, 0, 0]} maxBarSize={20} />
          <Line yAxisId="right" type="monotone" dataKey="gb" stroke="hsl(var(--chart-4))" strokeWidth={2} dot={false} />
        </ComposedChart>
      </ResponsiveContainer>
    </div>
  );
}
