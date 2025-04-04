import { NextResponse } from "next/server";
import jwt from "jsonwebtoken";
import fs from "fs/promises";
import path from "path";

const SECRET_KEY =  "cloudlsupersecrettopsecuritykey";

export async function POST(req: Request) {
  try {
    interface User {
      email: string;
      password: string;
    }

    const { email, password } = await req.json();

    if (!email || !password) {
      return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
    }

    // Read users from JSON file
    const usersFilePath = path.join(process.cwd(), "supersecureusers.json");
    const usersData = await fs.readFile(usersFilePath, "utf-8");

    // Check if user exists
    const users: User[] = JSON.parse(usersData);
    const user = users.find((u: User) => u.email === email && u.password === password);    
    if (!user) {
      return NextResponse.json({ error: "Invalid credentials" }, { status: 401 });
    }

    // Generate JWT token
    const token = jwt.sign({ email }, SECRET_KEY, { expiresIn: "1h" });

    return NextResponse.json({ message: "Login successful", token }, { status: 200 });
  } catch (error) {
    console.error("Login Error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
