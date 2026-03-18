"use client"

<<<<<<< Updated upstream
import * as React from "react"
import { Separator as SeparatorPrimitive } from "radix-ui"
=======
import { Separator as SeparatorPrimitive } from "@base-ui/react/separator"

>>>>>>> Stashed changes
import { cn } from "@/lib/utils"

function Separator({
  className,
  orientation = "horizontal",
<<<<<<< Updated upstream
  decorative = true,
  ...props
}: React.ComponentProps<typeof SeparatorPrimitive.Root>) {
  return (
    <SeparatorPrimitive.Root
      data-slot="separator"
      decorative={decorative}
      orientation={orientation}
      className={cn(
        "bg-border shrink-0",
        orientation === "horizontal" ? "h-px w-full" : "h-full w-px",
        className,
=======
  ...props
}: SeparatorPrimitive.Props) {
  return (
    <SeparatorPrimitive
      data-slot="separator"
      orientation={orientation}
      className={cn(
        "shrink-0 bg-border data-horizontal:h-px data-horizontal:w-full data-vertical:w-px data-vertical:self-stretch",
        className
>>>>>>> Stashed changes
      )}
      {...props}
    />
  )
}

export { Separator }
