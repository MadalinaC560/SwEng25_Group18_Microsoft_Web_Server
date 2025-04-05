import { NextResponse } from "next/server";
import jwt from "jsonwebtoken";
import path from "path";
import fs from "fs/promises";
import bcrypt from "bcrypt";


const SECRET_KEY = "cloudlsupersecrettopsecuritykey";

interface User {
  tenantEmail: string;
  email: string;
  password: string;
}

export async function POST(req: Request) {
  try {
    const { tenantEmail, userEmail, password } = await req.json();

    if (!tenantEmail || !userEmail || !password) {
      return NextResponse.json(
        { error: "Tenant email, user email, and password are required" },
        { status: 400 }
      );
    }

    // Check if tenant exists
    const tenantFile = path.join(process.cwd(), "supersecuretenants.json");
    let tenants: any[] = [];

    try {
      const tenantData = await fs.readFile(tenantFile, "utf-8");
      tenants = JSON.parse(tenantData);
    } catch {
      return NextResponse.json({ error: "Tenant data unavailable" }, { status: 500 });
    }

    const tenantExists = tenants.some((t) => t.orgEmail === tenantEmail);
    if (!tenantExists) {
      return NextResponse.json({ error: "Invalid tenant" }, { status: 401 });
    }

    // Check user credentials
    const usersFile = path.join(process.cwd(), "supersecureusers.json");
    const usersData = await fs.readFile(usersFile, "utf-8");
    const users: User[] = JSON.parse(usersData);

    const user = users.find(
      (u) => u.tenantEmail === tenantEmail && u.email === userEmail
    );
    
    if (!user) {
      return NextResponse.json(
        { error: "User does not belong to tenant or invalid credentials" },
        { status: 401 }
      );
    }
    
    // ✅ Check hashed password
    const passwordMatch = await bcrypt.compare(password, user.password);
    if (!passwordMatch) {
      return NextResponse.json(
        { error: "Invalid password" },
        { status: 401 }
      );
    }
    

    const token = jwt.sign(
      { tenantEmail: user.tenantEmail, email: user.email },
      SECRET_KEY,
      { expiresIn: "1h" }
    );

    return NextResponse.json({ message: "Login successful", token }, { status: 200 });
  } catch (error) {
    console.error("Login error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
