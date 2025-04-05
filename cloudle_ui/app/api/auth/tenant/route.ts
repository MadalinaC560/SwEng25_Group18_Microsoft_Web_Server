import { NextResponse } from "next/server";
import path from "path";
import fs from "fs/promises";

interface Tenant {
  orgName: string;
  orgEmail: string;
}

export async function POST(req: Request) {
  try {
    const { orgName, orgEmail } = await req.json();

    if (!orgName || !orgEmail) {
      return NextResponse.json(
        { error: "Organization name and email are required" },
        { status: 400 }
      );
    }

    const filePath = path.join(process.cwd(), "supersecuretenants.json");

    let tenants: Tenant[] = [];
    try {
      const data = await fs.readFile(filePath, "utf-8");
      tenants = JSON.parse(data);
    } catch {
      // File might not exist yet — ignore
    }

    const existingTenant = tenants.find((t) => t.orgEmail === orgEmail);
    if (existingTenant) {
      return NextResponse.json({ error: "Tenant already exists" }, { status: 400 });
    }

    tenants.push({ orgName, orgEmail });
    await fs.writeFile(filePath, JSON.stringify(tenants, null, 2));

    return NextResponse.json({ message: "Tenant created successfully" });
  } catch (err) {
    console.error("Tenant creation error:", err);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
