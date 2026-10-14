package com.example.data.model

enum class ThemeMode(val title: String, val description: String) {
    SYSTEM("System Default", "Matches your Android system display settings"),
    LIGHT("Light Mode", "Crisp, clean high-contrast daytime aesthetic"),
    DARK("Dark Mode", "Deep midnight navy for distraction-free focus")
}
