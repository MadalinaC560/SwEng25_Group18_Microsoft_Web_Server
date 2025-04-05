import { NextResponse } from "next/server";
import fs from "fs/promises";
import path from "path";
import bcrypt from "bcrypt";

interface User {
  tenantEmail: string;
  email: string;
  password: string;
}

const USERS_FILE_PATH = path.join(process.cwd(), "supersecureusers.json");

export async function POST(req: Request): Promise<NextResponse> {
  try {
    const { tenantEmail, email, password }: { tenantEmail: string; email: string; password: string } = await req.json();

    if (!tenantEmail || !email || !password) {
      return NextResponse.json(
        { error: "Tenant email, user email, and password are required" },
        { status: 400 }
      );
    }

    let users: User[] = [];

    try {
      const usersData = await fs.readFile(USERS_FILE_PATH, "utf-8");
      users = usersData.trim() ? JSON.parse(usersData) : [];
    } catch (error) {
      const nodeError = error as NodeJS.ErrnoException;
      if (nodeError.code !== "ENOENT") {
        console.error("Error reading users file:", error);
        return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
      }
    }

    const userExists = users.some(
      (u) => u.email === email && u.tenantEmail === tenantEmail
    );

    if (userExists) {
      return NextResponse.json(
        { error: "User already exists for this tenant" },
        { status: 400 }
      );
    }

    const hashedPassword = await bcrypt.hash(password, 10);

    const newUser: User = {
      tenantEmail,
      email,
      password: hashedPassword
    };

    users.push(newUser);

    await fs.writeFile(USERS_FILE_PATH, JSON.stringify(users, null, 2), "utf-8");

    return NextResponse.json({ message: "User registered successfully" }, { status: 201 });
  } catch (error) {
    console.error("Register Error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
