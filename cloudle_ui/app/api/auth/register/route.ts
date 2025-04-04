import { NextResponse } from "next/server";
import fs from "fs/promises";
import path from "path";
import bcrypt from "bcrypt";

interface User {
  email: string;
  password: string;
}

const USERS_FILE_PATH = path.join(process.cwd(), "supersecureusers.json");

export async function POST(req: Request) {
  try {
    const { email, password } = await req.json();

    if (!email || !password) {
      return NextResponse.json({ error: "Email and password are required" }, { status: 400 });
    }

    let users: User[] = [];

    try {
      // Read existing users
      const usersData = await fs.readFile(USERS_FILE_PATH, "utf-8");
      users = usersData.trim() ? JSON.parse(usersData) : [];
    } catch (error: any) {
      if (error.code !== "ENOENT") {
        console.error("Error reading users file:", error);
        return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
      }
      // File doesn't exist, assume empty user list
      users = [];
    }

    // Check if the user already exists
    if (users.find((u) => u.email === email)) {
      return NextResponse.json({ error: "User already exists" }, { status: 400 });
    }

    // Hash the password before saving
    const hashedPassword = await bcrypt.hash(password, 10);
    const newUser: User = { email, password: hashedPassword };

    users.push(newUser);

    // Save the new user list (write atomically)
    await fs.writeFile(USERS_FILE_PATH, JSON.stringify(users, null, 2), "utf-8");

    return NextResponse.json({ message: "User registered successfully" }, { status: 201 });
  } catch (error) {
    console.error("Register Error:", error);
    return NextResponse.json({ error: "Internal Server Error" }, { status: 500 });
  }
}
